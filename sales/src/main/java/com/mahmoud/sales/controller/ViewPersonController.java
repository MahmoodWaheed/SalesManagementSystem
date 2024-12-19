package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.handler.PersonHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

@Controller
public class ViewPersonController implements PersonHandler {

    @FXML
    private TextField nameField;

    @FXML
    private TextField locationField;

    @FXML
    private TextField typeField;

    @FXML
    private TextField balanceField;

    private Person person;

    @FXML
    public void initialize() {
        // This method is intentionally left blank, since we will populate fields in setPerson
    }

    // Implementing the PersonHandler interface method
    @Override
    public void setPerson(Person person) {
        this.person = person;
        loadPersonData();
    }

    // Load person data into fields
    private void loadPersonData() {
        if (person != null) {
            nameField.setText(person.getName());
            locationField.setText(person.getLocation());
            typeField.setText(person.getType());
            balanceField.setText(person.getOpenBalance().toString());
        }
    }

    // Handle the close button action
    @FXML
    private void handleClose() {
        // Close the popup window
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
