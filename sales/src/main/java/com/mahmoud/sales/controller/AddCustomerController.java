package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.handler.PersonHandler;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class AddCustomerController implements PersonHandler {
    private PersonService personService;

    @FXML
    private TextField nameField;

    @FXML
    private TextField locationField;

    @FXML
    private TextField balanceField;

    @FXML
    public void initialize() {
        // Manually wire dependencies using SpringFXMLLoader
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
    }

    @FXML
    public void handleSaveSupplier() {
        // Get values from input fields
        String name = nameField.getText();
        String location = locationField.getText();
        String balanceText = balanceField.getText();

        // Validate input
        if (name.isEmpty() || location.isEmpty() || balanceText.isEmpty()) {
            showAlert("Validation Error", "Please fill in all fields.");
            return;
        }

        BigDecimal balance;
        try {
            balance = new BigDecimal(balanceText);
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number for balance.");
            return;
        }

        // Create new Person (Supplier)
        Person supplier = new Person();
        supplier.setName(name);
        supplier.setLocation(location);
        supplier.setOpenBalance(balance);
        supplier.setType("Customer");  // Set type to Customer

        // Save the supplier
        personService.savePerson(supplier);

        // Close the popup window
        closeWindow();
    }

    @FXML
    public void handleCancel() {
        // Close the popup without saving
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

    @Override
    public void setPerson(Person person) {

    }
}
