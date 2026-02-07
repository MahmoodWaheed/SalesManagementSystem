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

public class PurchaseReportsSubMenuController {
    private AnchorPane contentPane; // Reference to the main content pane

    /**
     * Set the content pane reference (called by SidebarController)
     */
    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    /**
     * Go back to the main Reports menu
     */
    @FXML
    public void onBackToReports() {
        try {
            loadView("Reports.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load Reports menu");
        }
    }

    /**
     * Open the Purchas Transaction History report
     */
    @FXML
    public void onPurchasTransactionHistory(MouseEvent event) {
        try {
            loadView("PurchasReport.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load Purchas Transaction History report");
        }
    }

    /**
     * Load a view into the main content pane
     */
    private void loadView(String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFile));
        Node node = loader.load();

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
