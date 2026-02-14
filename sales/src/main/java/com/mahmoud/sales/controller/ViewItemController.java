package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Item;
import com.mahmoud.sales.handler.ItemHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;

@Controller
public class ViewItemController implements ItemHandler {

    @FXML private Label idLabel;
    @FXML private Label nameLabel;
    @FXML private Label descLabel;
    @FXML private Label balanceLabel;
    @FXML private Label sellingLabel;
    @FXML private Label purchasingLabel;

    @Override
    public void setItem(Item item) {
        if (item == null) return;

        idLabel.setText(item.getId() == null ? "-" : item.getId().toString());
        nameLabel.setText(item.getName() == null ? "-" : item.getName());
        descLabel.setText(item.getDescription() == null || item.getDescription().isBlank() ? "-" : item.getDescription());

        Double bal = item.getItemBalance();
        balanceLabel.setText(bal == null ? "0" : bal.toString());

        BigDecimal sell = item.getSellingPrice();
        sellingLabel.setText(sell == null ? "0.00" : sell.toString());

        BigDecimal buy = item.getPurchasingPrice();
        purchasingLabel.setText(buy == null ? "0.00" : buy.toString());
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) idLabel.getScene().getWindow();
        stage.close();
    }
}