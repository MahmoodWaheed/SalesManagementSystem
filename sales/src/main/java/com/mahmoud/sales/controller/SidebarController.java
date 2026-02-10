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

    /**
     * Load a view into the content pane
     */
    private void setView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
            Node node = loader.load();

            // Get the controller if it needs the contentPane reference
            Object controller = loader.getController();

            // If it's a submenu controller, pass the contentPane reference
            if (controller instanceof CustomerReportsSubMenuController) {
                ((CustomerReportsSubMenuController) controller).setContentPane(contentPane);
            } else if (controller instanceof ReportsMenuController) {
                ((ReportsMenuController) controller).setContentPane(contentPane);
            }

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

    @FXML
    public void onCustomerButtonClick() {
        setView("Customer.fxml");
    }

    @FXML
    public void onSupplierButtonClick() {
        setView("Supplier.fxml");
    }

    @FXML
    public void onEmployeeButtonClick() {
        setView("Employee.fxml");
    }

    /**
     * UPDATED: Load the Reports Menu instead of going directly to CustomerReport
     */
    @FXML
    public void onReportsButtonClick() {
        setView("ReportsMenu.fxml");  // Load the main Reports menu
    }

    @FXML
    public void onItemsButtonClick() {
        setView("Item.fxml");
    }

    @FXML
    public void onSalesButtonClick() {
        setView("SalesForm.fxml");
    }

    @FXML
    public void onPurchaseButtonClick() {
        setView("Purchaseform.fxml");
    }

    @FXML
    public void onStoreButtonClick() {
        setView("Store.fxml");
    }

    @FXML
    public void onSettingButtonClick() {
        setView("Setting.fxml");
    }

    @FXML
    public void onLogoutButtonClick() {
        setView("Login.fxml");
    }
}
