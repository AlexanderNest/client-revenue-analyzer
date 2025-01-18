package ru.nesterov.app.standarts;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PropertiesDescriptionTest {
    private final List<String> propertiesRequiresDescription = List.of(

    );


    @Test
    void verifyStandaloneDependentPropertiesAreMentionedInReadme() throws IOException {
        List<List<String>> string = loadPropertiesFromFile();
        String readmeContent = loadReadmeContent();

        string.forEach(property -> readmeContent.contains(property.getFirst()));
        System.out.println(string);
    }

    private String loadReadmeContent() throws IOException {
        return Files.readString(Paths.get("README.md"), StandardCharsets.UTF_8);
    }

    private List<List<String>> loadPropertiesFromFile() {
        try (Stream<String> lines = Files.lines(Paths.get("src/main/resources/", "application.properties"))) {
            return lines
                    .filter(Objects::nonNull)
                    .filter(line -> line.contains("="))
                    .map(String::strip)
                    .map(line -> List.of(line.split("=")))
                    .filter(property -> {
                        return isPropertyForDescription(property.getFirst(), property.size() == 1 ? null : property.getLast());
                    })
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isPropertyForDescription(String name, String value) {
        return StringUtils.isBlank(value)
                || propertiesRequiresDescription.contains(name)
                || name.endsWith(".enabled");

    }
}
