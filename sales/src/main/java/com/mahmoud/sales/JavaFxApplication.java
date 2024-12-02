package com.mahmoud.sales;

import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import javafx.stage.StageStyle;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.mahmoud.sales")


public class JavaFxApplication extends Application {



    private ConfigurableApplicationContext context;

    @Override
    public void init() throws Exception {
        // Start the Spring context
        context = SalesApplication.getApplicationContext();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the MainView.fxml file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        loader.setControllerFactory(context::getBean);  // Use Spring to inject controller dependencies
        Parent root = loader.load();



        // Set up the stage
        primaryStage.setTitle("Trade Tracker - Main View");
        primaryStage.setScene(new Scene(root,800,500));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        context.close();  // Close the Spring context when the application stops
    }
}
