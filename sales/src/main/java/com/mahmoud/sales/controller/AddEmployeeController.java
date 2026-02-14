package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.entity.Phone;
import com.mahmoud.sales.service.EmployeeService;
import com.mahmoud.sales.service.PhoneService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

@Controller
public class AddEmployeeController {

    private EmployeeService employeeService;
    private PhoneService phoneService;

    @FXML private TextField nameField;
    @FXML private TextField roleField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;

    @FXML
    public void initialize() {
        this.employeeService = SpringFXMLLoader.loadController(EmployeeService.class);
        this.phoneService = SpringFXMLLoader.loadController(PhoneService.class);
    }

    @FXML
    public void handleSaveEmployee() {
        String name = safe(nameField.getText());
        String role = safe(roleField.getText());
        String email = safe(emailField.getText());
        String password = safe(passwordField.getText());
        String phoneNumber = safe(phoneField.getText());

        if (name.isBlank() || role.isBlank() || email.isBlank() || password.isBlank() || phoneNumber.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill all fields (including password & phone).");
            return;
        }

        if (phoneNumber.length() > 15) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Phone number is too long (max 15).");
            return;
        }

        if (password.length() < 4) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password must be at least 4 characters.");
            return;
        }

        try {
            Employee emp = new Employee();
            emp.setName(name);
            emp.setRole(role);
            emp.setEmail(email);
            emp.setPassword(password);

            Employee saved = employeeService.saveEmployee(emp);

            Phone phone = new Phone();
            phone.setPhoneNumber(phoneNumber);
            phone.setEmployee(saved);
            phoneService.savePhone(phone);

            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Failed to save employee: " + e.getMessage());
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

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
