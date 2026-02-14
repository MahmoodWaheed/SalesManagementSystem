package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Item;
import com.mahmoud.sales.handler.ItemHandler;
import com.mahmoud.sales.service.ItemService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class EditItemController implements ItemHandler {

    private ItemService itemService;
    private Item item;

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField balanceField;
    @FXML private TextField sellingPriceField;
    @FXML private TextField purchasingPriceField;

    @FXML
    public void initialize() {
        this.itemService = SpringFXMLLoader.loadController(ItemService.class);
    }

    @Override
    public void setItem(Item item) {
        this.item = item;
        if (item == null) return;

        nameField.setText(item.getName());
        descriptionField.setText(item.getDescription() == null ? "" : item.getDescription());
        balanceField.setText(item.getItemBalance() == null ? "" : item.getItemBalance().toString());
        sellingPriceField.setText(item.getSellingPrice() == null ? "" : item.getSellingPrice().toString());
        purchasingPriceField.setText(item.getPurchasingPrice() == null ? "" : item.getPurchasingPrice().toString());
    }

    @FXML
    private void handleSave() {
        if (item == null) return;

        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String desc = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
        String balText = balanceField.getText() == null ? "" : balanceField.getText().trim();
        String sellText = sellingPriceField.getText() == null ? "" : sellingPriceField.getText().trim();
        String buyText = purchasingPriceField.getText() == null ? "" : purchasingPriceField.getText().trim();

        if (name.isBlank() || balText.isBlank() || sellText.isBlank()) {
            showAlert("Validation Error", "Please fill in: Name, Balance, Selling Price.");
            return;
        }

        Double balance;
        try {
            balance = Double.valueOf(balText);
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Balance must be a valid number.");
            return;
        }

        BigDecimal selling;
        try {
            selling = new BigDecimal(sellText);
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Selling price must be a valid number.");
            return;
        }

        BigDecimal purchasing = null;
        if (!buyText.isBlank()) {
            try {
                purchasing = new BigDecimal(buyText);
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Purchasing price must be a valid number.");
                return;
            }
        }

        item.setName(name);
        item.setDescription(desc.isBlank() ? null : desc);
        item.setItemBalance(balance);
        item.setSellingPrice(selling);
        item.setPurchasingPrice(purchasing);

        try {
            itemService.saveItem(item);
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Save Failed", "Failed to save changes: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
