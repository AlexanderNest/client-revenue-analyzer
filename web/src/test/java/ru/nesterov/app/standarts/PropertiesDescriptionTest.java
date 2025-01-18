package ru.nesterov.app.standarts;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class PropertiesDescriptionTest {
    private static final List<String> propertiesRequiresDescription = List.of(
            // Если появятся обязательные настроки, которые требуют описания, можно добавить их названия тут
    );

    private final String readmeContent = loadReadmeContent();

    public PropertiesDescriptionTest() throws IOException {
    }

    @Test
    void checkThatEmptyPropertiesAreDocumented() {
        List<List<String>> string = loadPropertiesFromFile();

        for (List<String> property : string) {
            if (property.size() == 1) {
                String name = property.get(0);
                assertTrue(readmeContent.contains(name), "property " + name + " not found in README.md");
            }
        }
    }

    @Test
    void checkThatEnabledPropertiesAreDocumented() throws IOException {
        List<List<String>> string = loadPropertiesFromFile();
        String readmeContent = loadReadmeContent();

        for (List<String> property : string) {
            if (property.size() == 2) {
                String name = property.get(0);
                if (name.endsWith(".enabled")) {
                    assertTrue(readmeContent.contains(name), "property " + name + " not found in README.md");
                }
            }
        }
    }

    private String loadReadmeContent() throws IOException {
        String path = Paths.get("").toAbsolutePath().getParent() + "/README.md";
        return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
    }

    private List<List<String>> loadPropertiesFromFile() {
        try (Stream<String> lines = Files.lines(Paths.get("src/main/resources/", "application.properties"))) {
            return lines
                    .filter(Objects::nonNull)
                    .filter(line -> line.contains("="))
                    .map(String::strip)
                    .map(line -> List.of(line.split("=")))
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
