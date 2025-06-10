package ru.nesterov.common.util;

import ru.nesterov.common.annotation.FieldAlias;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

public class PlainTextMapper {
    private static final List<String> trueAliases = List.of("yes", "да");
    private static final List<String> falseAliases = List.of("no");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static <T> T fillFromString(String data, Class<T> classForBuild) {
        T result;
        try {
            result = classForBuild.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        String[] lines = data.split("\n");
        Field[] fields = result.getClass().getDeclaredFields();

        for (String line : lines) {
            String[] parts = line.split(":", 2);

            if (parts.length != 2) {
                continue;
            }

            String key = parts[0].trim();
            String value = parts[1].trim();

            for (Field field : fields) {
                if (isApplicableField(field, key)) {
                    field.setAccessible(true);
                    setFieldValue(result, field, value);
                    field.setAccessible(false);
                    break;
                }
            }
        }

        return result;
    }

    private static boolean isApplicableField(Field field, String key) {
        if (field.getName().equals(key)) {
            return true;
        }

        FieldAlias fieldAlias = field.getAnnotation(FieldAlias.class);
        if (fieldAlias == null) {
            return false;
        }

        for (String alias : fieldAlias.value()) {
            if (alias.equals(key)) {
                return true;
            }
        }

        return false;
    }

    private static <T> void setFieldValue(T object, Field field, String value) {
        try {
            try {
                String enrichedDate = value + " 00:00";  // на данный момент конкретное время не интересно, важная фактическая дата переноса
                LocalDateTime dateTime = LocalDateTime.parse(enrichedDate, formatter);
                field.set(object, dateTime);
            } catch (Exception ignored) {}

            Class<?> type = field.getType();

            if (type == String.class) {
                field.set(object, value);
            }
            else if (type == int.class || type == Integer.class) {
                field.set(object, Integer.parseInt(value));
            }
            else if (type == boolean.class || type == Boolean.class) {
                field.set(object, convertToBoolean(value));
            }
            else if (type == long.class || type == Long.class) {
                field.set(object, Long.parseLong(value));
            }
            else if (type == double.class || type == Double.class) {
                field.set(object, Double.parseDouble(value));
            }
            else if (type == float.class || type == Float.class) {
                field.set(object, Float.parseFloat(value));
            }
        } catch (IllegalAccessException | NumberFormatException e) {
            System.err.println("Ошибка при установке значения для поля " + field.getName() + ": " + e.getMessage());
        }
    }

    private static boolean convertToBoolean(String value) {
        value = value.toLowerCase(Locale.ROOT);

        if (trueAliases.contains(value)) {
            return true;
        }
        if (falseAliases.contains(value)) {
            return false;
        }

        throw new IllegalArgumentException();
    }
}
