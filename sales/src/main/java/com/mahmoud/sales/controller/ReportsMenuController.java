package com.mahmoud.sales.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class ReportsMenuController {

    private AnchorPane contentPane; // Reference to the main content pane from sidebar

    /**
     * Set the content pane reference (called by SidebarController)
     */
    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    /**
     * Called when user clicks on Customer Reports card
     */
    @FXML
    public void onCustomerReportsClick(MouseEvent event) {
        try {
            loadView("CustomerReportsSubMenu.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load Customer Reports menu");
        }
    }

    /**
     * Called when user clicks on Supplier Reports card
     */
    @FXML
    public void onSupplierReportsClick(MouseEvent event) {
        showComingSoon("Supplier Reports");
    }

    /**
     * Called when user clicks on Treasury Reports card
     */
    @FXML
    public void onTreasuryReportsClick(MouseEvent event) {
        showComingSoon("Treasury Reports");
    }

    /**
     * Called when user clicks on Inventory Reports card
     */
    @FXML
    public void onInventoryReportsClick(MouseEvent event) {
        showComingSoon("Inventory Reports");
    }

    /**
     * Called when user clicks on Sales Reports card
     */
    @FXML
    public void onSalesReportsClick(MouseEvent event) {
        showComingSoon("Sales Reports");
    }

    /**
     * Called when user clicks on Custom Reports card
     */
    @FXML
    public void onCustomReportsClick(MouseEvent event) {
        showComingSoon("Custom Reports");
    }

    @FXML
    public void onPurchaseReportsClick(MouseEvent event) {
        try {
            loadView("PurchaseReportsSubMenu.fxml");
        } catch (Exception e) {
            showError("Failed to load Purchase Reports");
        }
    }

    /**
     * Load a view into the main content pane
     */
    private void loadView(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
        Node node = loader.load();

        // Pass contentPane reference to the submenu controller
        Object controller = loader.getController();
        if (controller instanceof CustomerReportsSubMenuController) {
            ((CustomerReportsSubMenuController) controller).setContentPane(contentPane);
        }

        if (contentPane != null) {
            // Anchor the node to all sides
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);

            contentPane.getChildren().setAll(node);
        } else {
            showError("Content pane not initialized");
        }
    }

    /**
     * Show a "Coming Soon" alert for features under development
     */
    private void showComingSoon(String feature) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Coming Soon");
        alert.setHeaderText(feature + " - Under Development");
        alert.setContentText("This feature is currently under development and will be available in a future update.\n\nStay tuned!");
        alert.showAndWait();
    }

    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}