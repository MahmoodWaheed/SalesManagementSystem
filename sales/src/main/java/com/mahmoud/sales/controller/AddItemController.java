package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Item;
import com.mahmoud.sales.service.ItemService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class AddItemController {

    private ItemService itemService;

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField balanceField;
    @FXML private TextField sellingPriceField;
    @FXML private TextField purchasingPriceField;

    // If your DB column is TEXT => max is ~65,535 bytes.
    // We'll use a safe UI limit (characters) to avoid huge inserts.
    private static final int DESCRIPTION_MAX_CHARS = 3000;

    @FXML
    public void initialize() {
        this.itemService = SpringFXMLLoader.loadController(ItemService.class);

        // Optional: live limit to prevent very long text
        descriptionField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.length() > DESCRIPTION_MAX_CHARS) {
                descriptionField.setText(newV.substring(0, DESCRIPTION_MAX_CHARS));
            }
        });
    }

    @FXML
    public void handleSaveItem() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String desc = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
        String balText = balanceField.getText() == null ? "" : balanceField.getText().trim();
        String sellText = sellingPriceField.getText() == null ? "" : sellingPriceField.getText().trim();
        String buyText = purchasingPriceField.getText() == null ? "" : purchasingPriceField.getText().trim();

        if (name.isBlank() || balText.isBlank() || sellText.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in: Name, Balance, Selling Price.");
            return;
        }

        if (desc.length() > DESCRIPTION_MAX_CHARS) {
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    "Description is too long. Max allowed is " + DESCRIPTION_MAX_CHARS + " characters.");
            return;
        }

        Double balance;
        try {
            balance = Double.valueOf(balText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number for Balance.");
            return;
        }

        BigDecimal sellingPrice;
        try {
            sellingPrice = new BigDecimal(sellText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number for Selling Price.");
            return;
        }

        BigDecimal purchasingPrice = null;
        if (!buyText.isBlank()) {
            try {
                purchasingPrice = new BigDecimal(buyText);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter a valid number for Purchasing Price.");
                return;
            }
        }

        Item item = new Item();
        item.setName(name);
        item.setDescription(desc.isBlank() ? null : desc);
        item.setItemBalance(balance);
        item.setSellingPrice(sellingPrice);
        item.setPurchasingPrice(purchasingPrice);

        try {
            itemService.saveItem(item);
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save Failed",
                    "Failed to add item.\n\nReason: " + e.getMessage() + "\n\nTip: Ensure DB column description is TEXT/LONGTEXT.");
        }
    }

    @FXML
    public void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
