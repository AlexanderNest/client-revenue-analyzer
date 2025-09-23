package ru.nesterov.app.standards;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ControllerStandardsTest {

    @Test
    @DisplayName("Реализованные классы-контроллеры не должны иметь аннотаций swagger")
    public void testControllersDoNotHaveSwaggerAnnotations() {
        Set<Class<?>> controllerClasses = getClassesAnnotatedWith(RestController.class);

        for (Class<?> controllerClass : controllerClasses) {
            // Проверяем отсутствие аннотации @Tag на классе
            assertFalse(
                    controllerClass.isAnnotationPresent(Tag.class),
                    "Class " + controllerClass.getName() + " should not have @Tag annotation"
            );

            // Проверяем отсутствие аннотации @Operation на методах
            Method[] methods = controllerClass.getDeclaredMethods();
            for (Method method : methods) {
                assertFalse(
                        method.isAnnotationPresent(Operation.class),
                        "Method " + method.getName() + " in class " + controllerClass.getName() + " should not have @Operation annotation"
                );
            }
        }
    }

    @DisplayName("Методы нтерфейсов-контроллеров должны иметь аннотаций swagger")
    @Test
    public void testAllControllerInterfacesHaveSwaggerAnnotations() {
        Set<Class<?>> controllerInterfaces = getClassesAnnotatedWith(RequestMapping.class);

        for (Class<?> controllerInterface : controllerInterfaces) {
            // Проверяем наличие аннотации @Tag на интерфейсе
            assertTrue(
                    controllerInterface.isAnnotationPresent(Tag.class),
                    "Interface " + controllerInterface.getName() + " is missing @Tag annotation"
            );

            // Проверяем наличие аннотации @Operation на методах интерфейса
            Method[] methods = controllerInterface.getDeclaredMethods();
            for (Method method : methods) {
                assertTrue(
                        method.isAnnotationPresent(Operation.class),
                        "Method " + method.getName() + " in interface " + controllerInterface.getName() + " is missing @Operation annotation"
                );
            }
        }
    }

    @Test
    @DisplayName("Все классы-контроллеры должны имплементировать интерфейс")
    public void testAllControllersImplementInterfaces() {
        Set<Class<?>> controllerClasses = getClassesAnnotatedWith(RestController.class);

        for (Class<?> controllerClass : controllerClasses) {
            // Проверяем, что класс реализует хотя бы один интерфейс
            assertTrue(
                    controllerClass.getInterfaces().length > 0,
                    "Class " + controllerClass.getName() + " does not implement any interface"
            );
        }
    }

//    @Test
//    @DisplayName("Проверка, что все эндпоинты контроллеров содержатся в Postman коллекции. Проверка, что передаются обязательные заголовки")
//    public void testAllControllerEndpointsInPostmanCollection() throws IOException {
//        Set<EndpointInfo> postmanEndpoints = getEndpointsFromPostman();
//        Set<Class<?>> controllerInterfaces = getClassesAnnotatedWith(RequestMapping.class);
//
//        for (Class<?> controllerInterface : controllerInterfaces) {
//            Method[] methods = controllerInterface.getDeclaredMethods();
//            for (Method method : methods) {
//                EndpointInfo endpointInfo = getEndpoint(method, controllerInterface);
//                assertTrue(hasEndpointEndsWith(postmanEndpoints, endpointInfo), "Endpoint " + endpointInfo.endpoint + " with method " + endpointInfo.method + " is not present in Postman collection");
//            }
//        }
//    }

    @Test
    @DisplayName("Проверка плейсхолдеров заголовка и передачи обязательных заголовков")
    public void testHeadersInPostmanCollection() throws IOException {
        Set<EndpointInfo> postmanEndpoints = getEndpointsFromPostman();

        for (EndpointInfo endpointInfo : postmanEndpoints) {
            assertTrue(isEndpointContainsRequiredHeader(endpointInfo, "X-secret-token"), "В запросе " + endpointInfo.endpoint + " не заполнен обязательный заголовок X-secret-token");
            assertTrue(isEndpointContainsRequiredHeader(endpointInfo, "X-username"), "В запросе " + endpointInfo.endpoint + " не заполнен обязательный заголовок X-username");

            for (Header header : endpointInfo.headers) {
                assertTrue(isHeaderHasCorrectPlaceholder(header));
            }
        }
    }

    @SneakyThrows
    private Set<Class<?>> getClassesAnnotatedWith(Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages("ru.nesterov.web.controller")
                        .addScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated)
        );

        return reflections.getTypesAnnotatedWith(annotation);
    }

    private boolean isEndpointContainsRequiredHeader(EndpointInfo endpointInfo, String headerName) {
        return endpointInfo.headers.stream()
                .map(h -> h.name)
                .anyMatch(headerName::equals);
    }

    private Set<EndpointInfo> getEndpointsFromPostman() throws IOException {
        final String POSTMAN_COLLECTION_PATH = System.getProperty("user.dir");
        Path dir = Paths.get(POSTMAN_COLLECTION_PATH).getParent();
        Path postmanCollectionPath = Paths.get("postman", "google-calendar-clients-analyzer.postman_collection.json");
        Path postmanPath = dir.resolve(postmanCollectionPath);

        Set<EndpointInfo> postmanEndpoints = new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(new File(postmanPath.toString()));
        JsonNode itemsNode = rootNode.get("item");

        if (itemsNode.isArray()) {
            extractEndpoints(itemsNode, postmanEndpoints);
        }

        return postmanEndpoints;
    }

    private void extractEndpoints(JsonNode itemsNode, Set<EndpointInfo> postmanEndpoints) {
        for (JsonNode itemNode : itemsNode) {
            if (itemNode.has("item")) {
                // Рекурсивно обрабатываем вложенные элементы
                extractEndpoints(itemNode.get("item"), postmanEndpoints);
            } else {
                JsonNode requestNode = itemNode.get("request");
                String method = requestNode.get("method").asText();
                JsonNode urlNode = requestNode.get("url");

                ArrayList<String> list = new ArrayList<>();
                for (JsonNode p : urlNode.get("path")) {
                    list.add(p.toString().replace("\"", ""));
                }

                String path = String.join("/", list);

                EndpointInfo endpointInfo = new EndpointInfo();
                endpointInfo.endpoint = path;
                endpointInfo.method = method;
                endpointInfo.headers = new HashSet<>();

                // Обработка заголовков (если они есть)
                JsonNode headersNode = requestNode.get("header");
                if (headersNode != null && headersNode.isArray()) {
                    for (JsonNode headerParentNode : headersNode) {
                        JsonNode headerNode = headerParentNode.get("key");
                        JsonNode valueNode = headerParentNode.get("value");

                        if (headerNode != null) {
                            Header header = new Header();
                            header.name = headerNode.asText();
                            header.value = valueNode.asText();
                            endpointInfo.headers.add(header);
                        }
                    }
                }

                postmanEndpoints.add(endpointInfo);
            }
        }
    }

    private boolean isHeaderHasCorrectPlaceholder(Header header) {
        String headerName = header.name;
        String headerPlaceholder = header.value;

        if (headerPlaceholder.startsWith("{{") && headerPlaceholder.endsWith("}}")) {
            headerPlaceholder = headerPlaceholder.substring(2, headerPlaceholder.length() - 2);
        }

        return headerName.equals(headerPlaceholder);
    }

    private boolean hasEndpointEndsWith(Set<EndpointInfo> postmanEndpoints, EndpointInfo endpointInfo) {
        for (EndpointInfo postmanEndpoint : postmanEndpoints) {
            if (postmanEndpoint.method.equals(endpointInfo.method) && postmanEndpoint.endpoint.endsWith(endpointInfo.endpoint)) {
                return true;
            }
        }

        return false;
    }

    private EndpointInfo getEndpoint(Method method, Class<?> controllerInterface) {
        EndpointInfo endpointInfo = new EndpointInfo();

        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            endpointInfo.endpoint = getMapping.value().length > 0 ? getMapping.value()[0] : "";
            endpointInfo.method = "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            endpointInfo.endpoint = postMapping.value().length > 0 ? postMapping.value()[0] : "";
            endpointInfo.method = "POST";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping putMapping = method.getAnnotation(PutMapping.class);
            endpointInfo.endpoint = putMapping.value().length > 0 ? putMapping.value()[0] : "";
            endpointInfo.method = "PUT";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
            endpointInfo.endpoint = deleteMapping.value().length > 0 ? deleteMapping.value()[0] : "";
            endpointInfo.method = "DELETE";
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            endpointInfo.endpoint = requestMapping.value().length > 0 ? requestMapping.value()[0] : "";
            endpointInfo.method = requestMapping.method()[0].name();
        }

        String prefix = controllerInterface.getAnnotation(RequestMapping.class).value()[0];
        endpointInfo.endpoint = prefix + endpointInfo.endpoint;

        return endpointInfo;
    }

    private static class EndpointInfo {
        String method;
        String endpoint;
        Set<Header> headers;
    }

    private static class Header {
        String name;
        String value;
    }
}