package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Phone;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.service.PhoneService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class PhoneController {

    @Autowired
    private PhoneService phoneService;

    @FXML
    private TableView<Phone> phoneTable;
    @FXML
    private TableColumn<Phone, Integer> idColumn;
    @FXML
    private TableColumn<Phone, String> phoneNumberColumn;

    @FXML
    private TextField phoneNumberField;
    @FXML
    private TextField employeeIdField;
    @FXML
    private TextField personIdField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private ObservableList<Phone> phoneList;

    @FXML
    public void initialize() {
        // Manually wire dependencies using SpringFXMLLoader
        this.phoneService = SpringFXMLLoader.loadController(PhoneService.class);
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        // Load all phones and display them
        loadPhones();
    }

    private void loadPhones() {
        List<Phone> phones = phoneService.findAllPhones();
        phoneList = FXCollections.observableArrayList(phones);
        phoneTable.setItems(phoneList);
    }

    @FXML
    public void addPhone() {
        String phoneNumber = phoneNumberField.getText();
        Integer employeeId = employeeIdField.getText().isEmpty() ? null : Integer.parseInt(employeeIdField.getText());
        Integer personId = personIdField.getText().isEmpty() ? null : Integer.parseInt(personIdField.getText());

        Phone phone = new Phone();
        phone.setPhoneNumber(phoneNumber);

        // Set employee or person depending on input
        if (employeeId != null) {
            // Set Employee by ID
            // Assuming you have a method to find Employee by ID
        }

        if (personId != null) {
            // Set Person by ID
            // Assuming you have a method to find Person by ID
        }

        phoneService.savePhone(phone);
        loadPhones();  // Reload table
    }

    @FXML
    public void deletePhone() {
        Phone selectedPhone = phoneTable.getSelectionModel().getSelectedItem();
        if (selectedPhone != null) {
            phoneService.deletePhone(selectedPhone.getId());
            loadPhones();  // Reload table
        }
    }
}
