package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Payment;
import com.mahmoud.sales.service.PaymentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @FXML
    private TableView<Payment> paymentTable;
    @FXML
    private TableColumn<Payment, Integer> idColumn;
    @FXML
    private TableColumn<Payment, BigDecimal> amountColumn;
    @FXML
    private TableColumn<Payment, Instant> dateColumn;
    @FXML
    private TableColumn<Payment, String> typeColumn;
    @FXML
    private TableColumn<Payment, String> wayColumn;

    @FXML
    private TextField amountField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField wayField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private ObservableList<Payment> paymentList;

    @FXML
    public void initialize() {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
        wayColumn.setCellValueFactory(new PropertyValueFactory<>("paymentWay"));

        // Load all payments and display them
        loadPayments();
    }

    private void loadPayments() {
        List<Payment> payments = paymentService.findAllPayments();
        paymentList = FXCollections.observableArrayList(payments);
        paymentTable.setItems(paymentList);
    }

    @FXML
    public void addPayment() {
        BigDecimal amount = new BigDecimal(amountField.getText());
        String type = typeField.getText();
        String way = wayField.getText();

        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setPaymentDate(Instant.now());
        payment.setPaymentType(type);
        payment.setPaymentWay(way);

        paymentService.savePayment(payment);
        loadPayments();  // Reload table
    }

    @FXML
    public void deletePayment() {
        Payment selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment != null) {
            paymentService.deletePayment(selectedPayment.getId());
            loadPayments();  // Reload table
        }
    }
}
