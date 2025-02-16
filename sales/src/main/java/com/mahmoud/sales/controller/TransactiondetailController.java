package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Item;
import com.mahmoud.sales.entity.Transactiondetail;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.service.TransactiondetailService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.beans.property.SimpleStringProperty;
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
    private TableColumn<Transactiondetail, String> itemColumn;

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

        // Manually wire dependencies using SpringFXMLLoader
        this.transactiondetailService = SpringFXMLLoader.loadController(TransactiondetailService.class);
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        comulativePriceColumn.setCellValueFactory(new PropertyValueFactory<>("comulativePrice"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        // Add this configuration for itemColumn
        itemColumn.setCellValueFactory(cellData -> {
            Item item = cellData.getValue().getItem();
            return new SimpleStringProperty(item != null ? item.getName() : "");
        });

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
//        transactionDetail.setPrice(price);

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