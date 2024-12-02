package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Purchasedetail;
import com.mahmoud.sales.entity.PurchasedetailId;
import com.mahmoud.sales.service.PurchasedetailService;
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
public class PurchasedetailController {

    @Autowired
    private PurchasedetailService purchasedetailService;

    @FXML
    private TableView<Purchasedetail> purchasedetailTable;
    @FXML
    private TableColumn<Purchasedetail, Integer> idColumn;
    @FXML
    private TableColumn<Purchasedetail, Double> quantityColumn;
    @FXML
    private TableColumn<Purchasedetail, BigDecimal> purchasingPriceColumn;
    @FXML
    private TableColumn<Purchasedetail, BigDecimal> cumulativePriceColumn;

    @FXML
    private TextField transactionIdField;
    @FXML
    private TextField itemIdField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField purchasingPriceField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private ObservableList<Purchasedetail> purchasedetailList;

    @FXML
    public void initialize() {
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        purchasingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasingPrice"));
        cumulativePriceColumn.setCellValueFactory(new PropertyValueFactory<>("comulativePrice"));

        // Load all purchase details and display them
        loadPurchasedetails();
    }

    private void loadPurchasedetails() {
        List<Purchasedetail> purchasedetails = purchasedetailService.findAllPurchasedetails();
        purchasedetailList = FXCollections.observableArrayList(purchasedetails);
        purchasedetailTable.setItems(purchasedetailList);
    }

    @FXML
    public void addPurchasedetail() {
        Integer transactionId = Integer.parseInt(transactionIdField.getText());
        Integer itemId = Integer.parseInt(itemIdField.getText());
        Double quantity = Double.parseDouble(quantityField.getText());
        BigDecimal purchasingPrice = new BigDecimal(purchasingPriceField.getText());

        PurchasedetailId purchasedetailId = new PurchasedetailId();
        purchasedetailId.setPurchasetransactionId(transactionId);
        purchasedetailId.setId(itemId);  // Assuming `id` is related to the item ID.

        Purchasedetail purchasedetail = new Purchasedetail();
        purchasedetail.setId(purchasedetailId);
        purchasedetail.setQuantity(quantity);
        purchasedetail.setPurchasingPrice(purchasingPrice);
        purchasedetail.setComulativePrice(purchasingPrice.multiply(BigDecimal.valueOf(quantity)));

        purchasedetailService.savePurchasedetail(purchasedetail);
        loadPurchasedetails();  // Reload table
    }

    @FXML
    public void deletePurchasedetail() {
        Purchasedetail selectedPurchasedetail = purchasedetailTable.getSelectionModel().getSelectedItem();
        if (selectedPurchasedetail != null) {
            purchasedetailService.deletePurchasedetail(selectedPurchasedetail.getId());
            loadPurchasedetails();  // Reload table
        }
    }
}
