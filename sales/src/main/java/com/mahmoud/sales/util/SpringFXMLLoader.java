package com.mahmoud.sales.util;

import jakarta.annotation.PostConstruct;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

    // ✅ NEW: allow other classes to access the context
    public static ApplicationContext getContext() {
        return applicationContext;
    }

    // ✅ NEW: load FXML file with Spring context injection
    public static Parent load(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SpringFXMLLoader.class.getResource(fxmlPath));
        loader.setControllerFactory(applicationContext::getBean);
        return loader.load();
    }


}
