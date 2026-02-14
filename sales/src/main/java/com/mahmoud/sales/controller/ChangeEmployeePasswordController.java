package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.handler.EmployeeHandler;
import com.mahmoud.sales.service.EmployeeService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

@Controller
public class ChangeEmployeePasswordController implements EmployeeHandler {

    private EmployeeService employeeService;
    private Employee employee;

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    public void initialize() {
        this.employeeService = SpringFXMLLoader.loadController(EmployeeService.class);
    }

    @Override
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @FXML
    private void handleSavePassword() {
        if (employee == null) return;

        String p1 = newPasswordField.getText() == null ? "" : newPasswordField.getText().trim();
        String p2 = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText().trim();

        if (p1.isBlank() || p2.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter password and confirm it.");
            return;
        }
        if (!p1.equals(p2)) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Passwords do not match.");
            return;
        }
        if (p1.length() < 4) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password must be at least 4 characters.");
            return;
        }

        try {
            employee.setPassword(p1);
            employeeService.saveEmployee(employee);
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save Failed", "Failed to change password: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) newPasswordField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
