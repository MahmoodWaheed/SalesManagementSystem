package com.mahmoud.sales.controller;

import com.mahmoud.sales.service.PersonService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.IOException;
@Controller
@Component


public class SidebarController {
    private ConfigurableApplicationContext context;


    @FXML
    public AnchorPane contentPane;
    @FXML
    private Button dashboardButton;

    @FXML
    private Button customerButton;

    @FXML
    private Button supplierButton;

    @FXML
    private Button employeeButton;

    @FXML
    private Button reportsButton;

    @FXML
    private Button itemsButton;

    @FXML
    private Button salesButton;

    @FXML
    private Button purchaseButton;

    @FXML
    private Button storeButton;

    @FXML
    private Button settingButton;

    @FXML
    private Button logoutButton;
    @Autowired
    private PersonService personService;



    // Add event handling methods if needed, for example:
//    @FXML
//    public void onDashboardButtonClick() {
//        System.out.println("Dashboard button clicked");
//        // Add the necessary logic for when the dashboard button is clicked
//    }

    // Similar methods can be added for other buttons if required
    // Method to load different scenes based on button clicks
//
//    private void setView(String fxmlFile) {
//        try {
//            // Start the Spring context
//            ApplicationContext context = SalesApplication.getApplicationContext();
//
//            // Load the FXML file for the selected view
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
//
//            // Use Spring to inject controller dependencies
//            loader.setControllerFactory(context::getBean);
//
//            // Load the FXML into a Node
//            Node node = loader.load();
//
//            // Check if contentPane is not null before clearing and adding new content
//            if (contentPane != null) {
//                // Clear the existing content and add the new view
//                contentPane.getChildren().setAll(node);
//            } else {
//                // Handle the null case (optional: log an error or show a message)
//                System.err.println("contentPane is null! Cannot set view.");
//            }
//
//        } catch (IOException e) {
//            // Provide better error handling
//            System.err.println("Failed to load view: " + fxmlFile);
//            e.printStackTrace();
//        }
//    }



    private void setView(String fxmlFile) {
        try {
            // Load the FXML file for the selected view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Node node = loader.load();

            // Get the controller for the loaded view
            Object controller = loader.getController();

//            // If the controller is an instance of PersonController, load person data
//            if (controller instanceof CustomerController) {
//                CustomerController personController = (CustomerController) controller;
//                personController.loadPersons();  // Load person data into the table
//            }

            // Clear the existing content and add the new view
            contentPane.getChildren().setAll(node);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    @FXML
//    public void onDashboardButtonClick() {
//        setView("dashboard.fxml");  // Load Dashboard view
//    }

    @FXML
    public void onCustomerButtonClick() {
        setView("Person.fxml");  // Load Customer view
    }

    @FXML
    public void onSupplierButtonClick() {
        setView("Supplier.fxml");  // Load Supplier view
    }

    // Add similar methods for other buttons (Employee, Reports, Sales, etc.)
}
