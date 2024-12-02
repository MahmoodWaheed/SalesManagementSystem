package com.mahmoud.sales;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableJpaRepositories
public class SalesApplication {



	private static ConfigurableApplicationContext applicationContext;

	public static void main(String[] args) {
		// Start the Spring Boot application
		applicationContext = new SpringApplicationBuilder(SalesApplication.class).run(args);
		// Launch JavaFX application
		Application.launch(JavaFxApplication.class, args);
	}

	public static ConfigurableApplicationContext getApplicationContext() {
		return applicationContext;
	}
}
