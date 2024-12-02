package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.entity.Phone;
import com.mahmoud.sales.service.EmployeeService;
import com.mahmoud.sales.service.PersonService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MainViewController {

    @FXML
    private TableView<Person> personTable; // Updated to reflect Person
    @FXML
    private TableColumn<Person, Integer> idColumn;
    @FXML
    private TableColumn<Person, String> nameColumn;
    @FXML
    private TableColumn<Person, String> locationColumn;
    @FXML
    private TableColumn<Person, String> typeColumn;
    @FXML
    private TableColumn<Person, BigDecimal> open_balanceColumn;
    @FXML
    private TableColumn<Person, String> phoneColumn;

    @Autowired
    private EmployeeService employeeService; // Inject EmployeeService
    @Autowired
    private PersonService personService;

    @FXML
    private void initialize() {
//        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        open_balanceColumn.setCellValueFactory(new PropertyValueFactory<>("openBalance"));


//        // Phones column (this could display a concatenation of phone numbers)
//        phoneColumn.setCellValueFactory(cellData -> {
//            StringBuilder phoneNumbers = new StringBuilder();
//            for (Phone phone : cellData.getValue().getPhones()) {
//                phoneNumbers.append(phone.getNumber()).append(", "); // Assuming there's a 'getNumber()' method on Phone
//            }
//            return phoneNumbers.length() > 0 ? phoneNumbers.substring(0, phoneNumbers.length() - 2) : ""; // Remove trailing comma
//        });

        // Optionally, load data when the view initializes
        loadPersonData();
    }

    // Event handler for the "Persons" button
    @FXML
    private void handleShowPersons() {
        loadPersonData();
    }

    // Method to load employee data into the table
    @FXML
    private void loadEmployeeData() {
//        ObservableList<Employee> employees = FXCollections.observableArrayList(employeeService.findAllEmployees());
//        personTable.setItems(employees);
    }
    // Method to load employee data into the table
    @FXML
    private void loadPersonData() {
        ObservableList<Person> persons = FXCollections.observableArrayList(personService.findAllPersons());
        personTable.setItems(persons);
    }



}
