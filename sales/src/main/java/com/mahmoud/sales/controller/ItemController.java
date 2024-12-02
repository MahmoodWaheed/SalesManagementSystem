package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Item;
import com.mahmoud.sales.service.ItemService;
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
import java.util.List;

@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @FXML
    private TableView<Item> itemTable;
    @FXML
    private TableColumn<Item, Integer> idColumn;
    @FXML
    private TableColumn<Item, String> nameColumn;
    @FXML
    private TableColumn<Item, Double> balanceColumn;
    @FXML
    private TableColumn<Item, BigDecimal> sellingPriceColumn;

    @FXML
    private TextField nameField;
    @FXML
    private TextField balanceField;
    @FXML
    private TextField sellingPriceField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private ObservableList<Item> itemList;

    @FXML
    public void initialize() {
        // Initialize the table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("itemBalance"));
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));

        // Load all items and display them
        loadItems();
    }

    private void loadItems() {
        List<Item> items = itemService.findAllItems();
        itemList = FXCollections.observableArrayList(items);
        itemTable.setItems(itemList);
    }

    @FXML
    public void addItem() {
        String name = nameField.getText();
        Double balance = Double.valueOf(balanceField.getText());
        BigDecimal sellingPrice = new BigDecimal(sellingPriceField.getText());

        Item item = new Item();
        item.setName(name);
        item.setItemBalance(balance);
        item.setSellingPrice(sellingPrice);

        itemService.saveItem(item);
        loadItems(); // Reload the table
    }

    @FXML
    public void deleteItem() {
        Item selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            itemService.deleteItem(selectedItem.getId());
            loadItems(); // Reload the table
        }
    }
}
