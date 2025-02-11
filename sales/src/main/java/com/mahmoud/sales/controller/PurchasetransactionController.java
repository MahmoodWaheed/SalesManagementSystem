package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Purchasetransaction;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.service.PurchasetransactionService;
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
public class PurchasetransactionController {

    @Autowired
    private PurchasetransactionService purchasetransactionService;

    @FXML
    private TableView<Purchasetransaction> purchasetransactionTable;
    @FXML
    private TableColumn<Purchasetransaction, Integer> idColumn;
    @FXML
    private TableColumn<Purchasetransaction, Instant> purchaseDateColumn;
    @FXML
    private TableColumn<Purchasetransaction, BigDecimal> totalAmountColumn;
    @FXML
    private TableColumn<Purchasetransaction, Integer> fatoraNumberColumn;

    @FXML
    private TextField personIdField;
    @FXML
    private TextField totalAmountField;
    @FXML
    private TextField fatoraNumberField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private ObservableList<Purchasetransaction> purchasetransactionList;

    @FXML
    public void initialize() {

        // Manually wire dependencies using SpringFXMLLoader
        this.purchasetransactionService = SpringFXMLLoader.loadController(PurchasetransactionService.class);
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
        totalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        fatoraNumberColumn.setCellValueFactory(new PropertyValueFactory<>("fatoraNumber"));

        // Load all purchase transactions and display them
        loadPurchasetransactions();
    }

    private void loadPurchasetransactions() {
        List<Purchasetransaction> purchasetransactions = purchasetransactionService.findAllPurchasetransactions();
        purchasetransactionList = FXCollections.observableArrayList(purchasetransactions);
        purchasetransactionTable.setItems(purchasetransactionList);
    }

    @FXML
    public void addPurchasetransaction() {
        Integer personId = Integer.parseInt(personIdField.getText());
        BigDecimal totalAmount = new BigDecimal(totalAmountField.getText());
        Integer fatoraNumber = Integer.parseInt(fatoraNumberField.getText());

        Purchasetransaction purchasetransaction = new Purchasetransaction();
        purchasetransaction.setTotalAmount(totalAmount);
        purchasetransaction.setFatoraNumber(fatoraNumber);
        purchasetransaction.setPurchaseDate(Instant.now());

        // Assuming person entity is already set with a foreign key reference in the database
        // This would typically involve setting the Person entity reference here

        purchasetransactionService.savePurchasetransaction(purchasetransaction);
        loadPurchasetransactions();  // Reload table
    }

    @FXML
    public void deletePurchasetransaction() {
        Purchasetransaction selectedPurchasetransaction = purchasetransactionTable.getSelectionModel().getSelectedItem();
        if (selectedPurchasetransaction != null) {
            purchasetransactionService.deletePurchasetransaction(selectedPurchasetransaction.getId());
            loadPurchasetransactions();  // Reload table
        }
    }
}
