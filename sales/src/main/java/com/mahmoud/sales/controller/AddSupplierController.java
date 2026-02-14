package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.entity.Phone;
import com.mahmoud.sales.handler.PersonHandler;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.service.PhoneService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class AddSupplierController implements PersonHandler {

    private PersonService personService;
    private PhoneService phoneService;

    @FXML private TextField nameField;
    @FXML private TextField locationField;
    @FXML private TextField balanceField;
    @FXML private TextField phoneField;

    @FXML
    public void initialize() {
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
        this.phoneService = SpringFXMLLoader.loadController(PhoneService.class);
    }

    @FXML
    public void handleSaveSupplier() {
        String name = safe(nameField.getText());
        String location = safe(locationField.getText());
        String balanceText = safe(balanceField.getText());
        String phoneNumber = safe(phoneField.getText());

        if (name.isBlank() || location.isBlank() || balanceText.isBlank() || phoneNumber.isBlank()) {
            showAlert("Validation Error", "Please fill in all fields (including phone).");
            return;
        }

        if (phoneNumber.length() > 15) {
            showAlert("Validation Error", "Phone number is too long (max 15).");
            return;
        }

        BigDecimal balance;
        try {
            balance = new BigDecimal(balanceText);
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number for balance.");
            return;
        }

        Person supplier = new Person();
        supplier.setName(name);
        supplier.setLocation(location);
        supplier.setOpenBalance(balance);
        supplier.setType("Supplier");

        try {
            Person saved = personService.savePersonAndReturn(supplier);

            Phone phone = new Phone();
            phone.setPhoneNumber(phoneNumber);
            phone.setPerson(saved);
            phoneService.savePhone(phone);

            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Save Failed", "Failed to save supplier: " + e.getMessage());
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    public void setPerson(Person person) {
        // Not used for Add popup
    }
}
