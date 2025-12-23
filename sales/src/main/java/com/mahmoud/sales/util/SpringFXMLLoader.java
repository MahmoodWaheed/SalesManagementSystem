package com.mahmoud.sales.util;

import jakarta.annotation.PostConstruct;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * Utility to load FXML using Spring's ApplicationContext as controller factory.
 * - Exposes the ApplicationContext (getContext())
 * - Provides load(...) helper that wires controllerFactory to applicationContext::getBean
 */
@Component
public class SpringFXMLLoader {
    private static ApplicationContext applicationContext;

    public SpringFXMLLoader(ApplicationContext applicationContext) {
        SpringFXMLLoader.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        Objects.requireNonNull(applicationContext, "Application context must not be null");
    }

    /**
     * Return the Spring-managed bean of the requested controller class.
     */
    public static <T> T loadController(Class<T> controllerClass) {
        return applicationContext.getBean(controllerClass);
    }

    /**
     * Return the ApplicationContext (static accessor).
     */
    public static ApplicationContext getContext() {
        return applicationContext;
    }

    /**
     * Load FXML located at the given classpath path (e.g. "/fxml/InvoicePreview.fxml")
     * and set the controller factory to applicationContext::getBean so controllers are injected by Spring.
     * Returns the Parent root. Throws IOException if load fails.
     */
    public static Parent load(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SpringFXMLLoader.class.getResource(fxmlPath));

        loader.setControllerFactory(applicationContext::getBean);
        return loader.load();
    }
}
