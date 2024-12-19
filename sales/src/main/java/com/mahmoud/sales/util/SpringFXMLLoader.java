package com.mahmoud.sales.util;

import com.mahmoud.sales.controller.PersonController;
import jakarta.annotation.PostConstruct;
import javafx.fxml.FXMLLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class SpringFXMLLoader {
    private static ApplicationContext applicationContext;

    public SpringFXMLLoader(ApplicationContext applicationContext) {
        SpringFXMLLoader.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        // Ensure application context is properly initialized
        Objects.requireNonNull(applicationContext, "Application context must not be null");
    }

    public static <T> T loadController(Class<T> controllerClass) {
        return applicationContext.getBean(controllerClass);
    }


}
