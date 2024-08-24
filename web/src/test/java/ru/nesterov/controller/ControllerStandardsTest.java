package ru.nesterov.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ControllerStandardsTest {

    @Test
    @DisplayName("Реализованные классы-контроллеры не должны иметь аннотаций swagger")
    public void testControllersDoNotHaveSwaggerAnnotations() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages("ru.nesterov.controller")
                .addScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated));

        // Получаем все классы с аннотацией @RestController
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(RestController.class);

        for (Class<?> controllerClass : controllerClasses) {
            // Проверяем отсутствие аннотации @Tag на классе
            assertFalse(controllerClass.isAnnotationPresent(Tag.class),
                    "Class " + controllerClass.getName() + " should not have @Tag annotation");

            // Проверяем отсутствие аннотации @Operation на методах
            Method[] methods = controllerClass.getDeclaredMethods();
            for (Method method : methods) {
                assertFalse(method.isAnnotationPresent(Operation.class),
                        "Method " + method.getName() + " in class " + controllerClass.getName() + " should not have @Operation annotation");
            }
        }
    }

    @DisplayName("Методы нтерфейсов-контроллеров должны иметь аннотаций swagger")
    @Test
    public void testAllControllerInterfacesHaveSwaggerAnnotations() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages("ru.nesterov.controller")
                .addScanners(Scanners.TypesAnnotated, Scanners.MethodsAnnotated));

        // Получаем все интерфейсы с аннотацией @RequestMapping
        Set<Class<?>> controllerInterfaces = reflections.getTypesAnnotatedWith(RequestMapping.class);

        for (Class<?> controllerInterface : controllerInterfaces) {
            // Проверяем наличие аннотации @Tag на интерфейсе
            assertTrue(controllerInterface.isAnnotationPresent(Tag.class),
                    "Interface " + controllerInterface.getName() + " is missing @Tag annotation");

            // Проверяем наличие аннотации @Operation на методах интерфейса
            Method[] methods = controllerInterface.getDeclaredMethods();
            for (Method method : methods) {
                assertTrue(method.isAnnotationPresent(Operation.class),
                        "Method " + method.getName() + " in interface " + controllerInterface.getName() + " is missing @Operation annotation");
            }
        }
    }

    @Test
    @DisplayName("Все классы-контроллеры должны имплементировать интерфейс")
    public void testAllControllersImplementInterfaces() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages("ru.nesterov.controller")
                .addScanners(Scanners.TypesAnnotated));

        // Получаем все классы с аннотацией @RestController
        Set<Class<?>> controllerClasses = reflections.getTypesAnnotatedWith(RestController.class);

        for (Class<?> controllerClass : controllerClasses) {
            // Проверяем, что класс реализует хотя бы один интерфейс
            assertTrue(controllerClass.getInterfaces().length > 0,
                    "Class " + controllerClass.getName() + " does not implement any interface");
        }
    }
}