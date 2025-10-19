package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Transaction;
import com.mahmoud.sales.service.ItemService;
import com.mahmoud.sales.service.TransactionService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Controller
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @FXML
    private TableView<Transaction> transactionTable;
    @FXML
    private TableColumn<Transaction, Integer> idColumn;
    @FXML
    private TableColumn<Transaction, Instant> transactionDateColumn;
    @FXML
    private TableColumn<Transaction, BigDecimal> amountColumn;
    @FXML
    private TableColumn<Transaction, String> transactionTypeColumn;

    @FXML
    private TextField personIdField;
    @FXML
    private TextField amountField;
    @FXML
    private TextField transactionTypeField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private ObservableList<Transaction> transactionList;

    @FXML
    public void initialize() {

        // Manually wire dependencies using SpringFXMLLoader
        this.transactionService = SpringFXMLLoader.loadController(TransactionService.class);
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("transactionType"));

        // Load all transactions and display them
        loadTransactions();
    }

    private void loadTransactions() {
        List<Transaction> transactions = transactionService.findAllTransactions();
        transactionList = FXCollections.observableArrayList(transactions);
        transactionTable.setItems(transactionList);
    }

    @FXML
    public void addTransaction() {
        Integer personId = Integer.parseInt(personIdField.getText());
        BigDecimal amount = new BigDecimal(amountField.getText());
        String transactionType = transactionTypeField.getText();

        Transaction transaction = new Transaction();
        transaction.setTotalAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionDate(Instant.now());

        // Assuming person entity is already set with a foreign key reference in the database
        // This would typically involve setting the Person entity reference here

        transactionService.saveTransaction(transaction);
        loadTransactions();  // Reload table
    }

    @FXML
    public void deleteTransaction() {
        Transaction selectedTransaction = transactionTable.getSelectionModel().getSelectedItem();
        if (selectedTransaction != null) {
            transactionService.deleteTransaction(selectedTransaction.getId());
            loadTransactions();  // Reload table
        }
    }
}
