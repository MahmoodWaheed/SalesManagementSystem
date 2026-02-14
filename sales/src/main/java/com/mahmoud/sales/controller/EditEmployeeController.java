package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.handler.EmployeeHandler;
import com.mahmoud.sales.service.EmployeeService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class EditEmployeeController implements EmployeeHandler {

    private EmployeeService employeeService;
    private Employee employee;

    @FXML private TextField nameField;
    @FXML private TextField roleField;
    @FXML private TextField emailField;
    @FXML private TextField salaryField;

    @FXML private Label passwordLabel;
    @FXML private CheckBox showPasswordCheck;

    private String realPassword = null;

    @FXML
    public void initialize() {
        this.employeeService = SpringFXMLLoader.loadController(EmployeeService.class);
        showPasswordCheck.selectedProperty().addListener((obs, o, n) -> updatePasswordLabel());
    }

    @Override
    public void setEmployee(Employee employee) {
        this.employee = employee;
        if (employee == null) return;

        nameField.setText(employee.getName());
        roleField.setText(employee.getRole());
        emailField.setText(employee.getEmail());
        salaryField.setText(employee.getSalary() == null ? "" : employee.getSalary().toString());

        realPassword = employee.getPassword();
        updatePasswordLabel();
    }

    private void updatePasswordLabel() {
        if (realPassword == null || realPassword.isBlank()) {
            passwordLabel.setText("-");
            showPasswordCheck.setDisable(true);
            showPasswordCheck.setSelected(false);
            return;
        }
        showPasswordCheck.setDisable(false);
        if (showPasswordCheck.isSelected()) {
            passwordLabel.setText(realPassword);
        } else {
            passwordLabel.setText("●●●●●●");
        }
    }

    @FXML
    private void handleChangePassword() {
        if (employee == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/changeEmployeePassword.fxml"));
            Parent ui = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof EmployeeHandler handler) {
                handler.setEmployee(employee);
            }

            Stage stage = new Stage();
            stage.setTitle("Change Password");
            stage.setScene(new Scene(ui));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh password label from latest state
            realPassword = employee.getPassword();
            updatePasswordLabel();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Change Password", "Failed to open Change Password window.");
        }
    }

    @FXML
    private void handleSave() {
        if (employee == null) return;

        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String role = roleField.getText() == null ? "" : roleField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String salaryText = salaryField.getText() == null ? "" : salaryField.getText().trim();

        if (name.isBlank() || role.isBlank() || email.isBlank()) {
            showAlert("Validation Error", "Please fill in: Name, Role, Email.");
            return;
        }

        BigDecimal salary = null;
        if (!salaryText.isBlank()) {
            try {
                salary = new BigDecimal(salaryText);
            } catch (NumberFormatException e) {
                showAlert("Input Error", "Salary must be a valid number.");
                return;
            }
        }

        employee.setName(name);
        employee.setRole(role);
        employee.setEmail(email);
        employee.setSalary(salary);

        try {
            employeeService.saveEmployee(employee);
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Save Failed", "Failed to save changes: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
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
}
