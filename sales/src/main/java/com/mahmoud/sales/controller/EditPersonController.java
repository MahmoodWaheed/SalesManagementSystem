package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.handler.PersonHandler;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
//@Scope("prototype")  // Allows a new instance of the controller for each JavaFX view
public class EditPersonController implements PersonHandler {

    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField balanceField;

    @Autowired
    private PersonService personService;

    private Person person;  // The person being edited

    @FXML
    public void initialize() {
        // Manually wire dependencies using SpringFXMLLoader    الجزء دا على كل مشاكل الانجيكشن اللى كانت بتحصل
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
    }

    // Method to set the person object passed from the parent controller
    @Override
    public void setPerson(Person person) {
        this.person = person;

        // Load person data into the form fields
        nameField.setText(person.getName());
        locationField.setText(person.getLocation());
        typeField.setText(person.getType());
        balanceField.setText(person.getOpenBalance().toString());
    }

    // Handle the save action
    @FXML
    public void handleSave() {


        // Validate the input
        if (nameField.getText().isEmpty() || typeField.getText().isEmpty() || balanceField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please fill in all required fields.");
            return;
        }

        try {
            BigDecimal balance = new BigDecimal(balanceField.getText());

            // Update person properties
            person.setName(nameField.getText());
            person.setLocation(locationField.getText());
            person.setType(typeField.getText());
            person.setOpenBalance(balance);

            // Save the updated person
            if (personService != null) {
                personService.savePerson(person);
            } else {
                System.out.println("PersonService is null");
            }


            // Show confirmation alert
            showAlert(Alert.AlertType.INFORMATION, "Success", "Person details have been updated.");

            // Close the window
            closeWindow();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number for balance.");
        }
    }

    // Handle the cancel action
    @FXML
    public void handleCancel() {
        closeWindow();
    }

    // Helper method to close the popup window
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    // Helper method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
