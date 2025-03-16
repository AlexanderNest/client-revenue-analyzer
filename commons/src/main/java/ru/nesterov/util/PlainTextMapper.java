package ru.nesterov.util;


import ru.nesterov.annotation.FieldAlias;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PlainTextMapper {
    private static final List<String> trueAliases = List.of("yes", "да");
    private static final List<String> falseAliases = List.of("no", "нет");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static <T> T fillFromString(String data, Class<T> classForBuild) {
        T result;
        try {
            result = classForBuild.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        String[] lines = data
                .replaceAll("</span>", "\n")
                .replaceAll("<[^>]*>", "")
                .split("\n");

        Field[] fields = result.getClass().getDeclaredFields();

        if (lines.length == 0) {
            return null;
        }

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
        Object typedValue = null;
        Class<?> type = field.getType();

        if (type == LocalDateTime.class) {
            String enrichedDate = value + " 00:00";  // на данный момент конкретное время не интересно, важна фактическая дата переноса
            typedValue = LocalDateTime.parse(enrichedDate, formatter);
        } else if (type == String.class) {
            typedValue = value;
        } else if (type == int.class || type == Integer.class) {
            typedValue = Integer.parseInt(value);
        } else if (type == boolean.class || type == Boolean.class) {
            typedValue = convertToBoolean(value);
        } else if (type == long.class || type == Long.class) {
            typedValue = Long.parseLong(value);
        } else if (type == double.class || type == Double.class) {
            typedValue = Double.parseDouble(value);
        } else if (type == float.class || type == Float.class) {
            typedValue = Float.parseFloat(value);
        }

        try {
            field.set(object, typedValue);
        } catch (IllegalAccessException  e) {
            System.err.println("Ошибка при установке значения для поля " + field.getName() + ": " + e.getMessage());
            throw new RuntimeException(e);
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
