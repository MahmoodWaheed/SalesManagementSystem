package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.handler.PersonHandler;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class DeletePersonController implements PersonHandler {

    @FXML
    private Label nameLabel;

    @Autowired
    private PersonService personService;

    private Person person;  // The person to be deleted

    @FXML
    public void initialize() {
        // Manually wire dependencies using SpringFXMLLoader
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
    }

    // Method to set the person object passed from the parent controller
    @Override
    public void setPerson(Person person) {
        this.person = person;
        nameLabel.setText(person.getName());  // Display the person's name in the confirmation dialog
    }

    // Handle the delete action
    @FXML
    public void handleDelete() {
        if (person != null) {
            // Perform the delete operation
            personService.deletePerson(person.getId());

            // Show confirmation alert
            showAlert(Alert.AlertType.INFORMATION, "Success", "Person has been deleted.");

            // Close the window
            closeWindow();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No person selected for deletion.");
        }
    }

    // Handle the cancel action
    @FXML
    public void handleCancel() {
        closeWindow();
    }

    // Helper method to close the popup window
    private void closeWindow() {
        Stage stage = (Stage) nameLabel.getScene().getWindow();
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
