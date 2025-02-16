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
//    private void setView(String fxmlFile) {
//        try {
//            // Load the FXML file for the selected view
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
//            Node node = loader.load();
//
//            // Get the controller for the loaded view
//            Object controller = loader.getController();
//
////            // If the controller is an instance of PersonController, load person data
////            if (controller instanceof CustomerController) {
////                CustomerController personController = (CustomerController) controller;
////                personController.loadPersons();  // Load person data into the table
////            }
//
//            // Clear the existing content and add the new view
//            contentPane.getChildren().setAll(node);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void setView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Node node = loader.load();

            // Anchor the node to all sides of the contentPane
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);

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
        setView("Customer.fxml");  // Load Customer view
    }

    @FXML
    public void onSupplierButtonClick() {
        setView("Supplier.fxml");  // Load Supplier view
    }

    @FXML
    public void onEmployeeButtonClick() {
        setView("Employee.fxml");  // Load Employee view
    }

    @FXML
    public void onReportsButtonClick() {
        setView("Reports.fxml");  // Load Reports view
    }

    @FXML
    public void onItemsButtonClick() {
        setView("Item.fxml");  // Load Items view
    }

    @FXML
    public void onSalesButtonClick() {
        setView("SalesForm.fxml");  // Load Sales view
    }

    @FXML
    public void onPurchaseButtonClick() {
        setView("Purchase.fxml");  // Load Purchase view
    }

    @FXML
    public void onStoreButtonClick() {
        setView("Store.fxml");  // Load Store view
    }

    @FXML
    public void onSettingButtonClick() {
        setView("Setting.fxml");  // Load Setting view
    }

    @FXML
    public void onLogoutButtonClick() {
        setView("Login.fxml");  // Load Login view
    }


    // Add similar methods for other buttons (Employee, Reports, Sales, etc.)
}
