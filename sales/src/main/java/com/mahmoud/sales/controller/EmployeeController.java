package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.service.EmployeeService;
import com.mahmoud.sales.service.ItemService;
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
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, Integer> idColumn;
    @FXML
    private TableColumn<Employee, String> nameColumn;
    @FXML
    private TableColumn<Employee, String> roleColumn;
    @FXML
    private TableColumn<Employee, String> emailColumn;

    @FXML
    private TextField nameField;
    @FXML
    private TextField roleField;
    @FXML
    private TextField emailField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private ObservableList<Employee> employeeList;

    @FXML
    public void initialize() {
        // Manually wire dependencies using SpringFXMLLoader
        this.employeeService = SpringFXMLLoader.loadController(EmployeeService.class);

        // Initialize the table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Load all employees and display them
        loadEmployees();
    }

    private void loadEmployees() {
        List<Employee> employees = employeeService.findAllEmployees();
        employeeList = FXCollections.observableArrayList(employees);
        employeeTable.setItems(employeeList);
    }

    @FXML
    public void addEmployee() {
        String name = nameField.getText().trim();
        String role = roleField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || role.isEmpty() || email.isEmpty()) {
            showAlert("All fields must be filled in.");
            return;
        }

        Employee employee = new Employee();
        employee.setName(name);
        employee.setRole(role);
        employee.setEmail(email);

        employeeService.saveEmployee(employee);
        loadEmployees(); // Reload the table
        showAlert("Employee added successfully!");
    }

    @FXML
    public void deleteEmployee() {
        Employee selectedEmployee = employeeTable.getSelectionModel().getSelectedItem();
        if (selectedEmployee != null) {
            employeeService.deleteEmployee(selectedEmployee.getId());
            loadEmployees(); // Reload the table
            showAlert("Employee deleted successfully!");
        } else {
            showAlert("Please select an employee to delete.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
