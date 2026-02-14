package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.handler.EmployeeHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class ViewEmployeeController implements EmployeeHandler {

    @FXML private Label idLabel;
    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private Label emailLabel;
    @FXML private Label salaryLabel;

    @FXML private Label passwordLabel;
    @FXML private CheckBox showPasswordCheck;

    private String realPassword = null;

    @FXML
    public void initialize() {
        showPasswordCheck.selectedProperty().addListener((obs, oldV, newV) -> updatePasswordLabel());
    }

    @Override
    public void setEmployee(Employee employee) {
        if (employee == null) return;

        idLabel.setText(employee.getId() == null ? "-" : employee.getId().toString());
        nameLabel.setText(employee.getName() == null ? "-" : employee.getName());
        roleLabel.setText(employee.getRole() == null ? "-" : employee.getRole());
        emailLabel.setText(employee.getEmail() == null ? "-" : employee.getEmail());

        BigDecimal salary = employee.getSalary();
        salaryLabel.setText(salary == null ? "0.00" : salary.toString());

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
    private void handleClose() {
        Stage stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }
}
