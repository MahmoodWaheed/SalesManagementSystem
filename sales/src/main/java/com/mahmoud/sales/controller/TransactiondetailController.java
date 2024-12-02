package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Transactiondetail;
import com.mahmoud.sales.service.TransactiondetailService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class TransactiondetailController {

    @Autowired
    private TransactiondetailService transactiondetailService;

    @FXML
    private TableView<Transactiondetail> transactiondetailTable;
    @FXML
    private TableColumn<Transactiondetail, Integer> idColumn;
    @FXML
    private TableColumn<Transactiondetail, Double> quantityColumn;
    @FXML
    private TableColumn<Transactiondetail, BigDecimal> sellingPriceColumn;
    @FXML
    private TableColumn<Transactiondetail, BigDecimal> comulativePriceColumn;
    @FXML
    private TableColumn<Transactiondetail, BigDecimal> priceColumn;

    @FXML
    private TextField quantityField;
    @FXML
    private TextField sellingPriceField;
    @FXML
    private TextField comulativePriceField;
    @FXML
    private TextField priceField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private ObservableList<Transactiondetail> transactiondetailList;

    @FXML
    public void initialize() {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        comulativePriceColumn.setCellValueFactory(new PropertyValueFactory<>("comulativePrice"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Load all transaction details and display them
        loadTransactionDetails();
    }

    private void loadTransactionDetails() {
        List<Transactiondetail> transactionDetails = transactiondetailService.findAllTransactionDetails();
        transactiondetailList = FXCollections.observableArrayList(transactionDetails);
        transactiondetailTable.setItems(transactiondetailList);
    }

    @FXML
    public void addTransactionDetail() {
        Double quantity = Double.parseDouble(quantityField.getText());
        BigDecimal sellingPrice = new BigDecimal(sellingPriceField.getText());
        BigDecimal comulativePrice = new BigDecimal(comulativePriceField.getText());
        BigDecimal price = new BigDecimal(priceField.getText());

        Transactiondetail transactionDetail = new Transactiondetail();
        transactionDetail.setQuantity(quantity);
        transactionDetail.setSellingPrice(sellingPrice);
        transactionDetail.setComulativePrice(comulativePrice);
        transactionDetail.setPrice(price);

        transactiondetailService.saveTransactionDetail(transactionDetail);
        loadTransactionDetails();  // Reload table
    }

    @FXML
    public void deleteTransactionDetail() {
        Transactiondetail selectedTransactionDetail = transactiondetailTable.getSelectionModel().getSelectedItem();
        if (selectedTransactionDetail != null) {
            transactiondetailService.deleteTransactionDetail(selectedTransactionDetail.getId().getTransactionId());
            loadTransactionDetails();  // Reload table
        }
    }
}