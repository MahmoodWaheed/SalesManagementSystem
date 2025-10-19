package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Payment;
import com.mahmoud.sales.entity.Transaction;
import com.mahmoud.sales.entity.Transactiondetail;
import com.mahmoud.sales.service.InvoiceService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
public class InvoicePreviewController {

    @FXML private ImageView companyLogo;
    @FXML private Label lblInvoiceNumber;
    @FXML private Label lblInvoiceDate;

    @FXML private Label lblCustomerName;
    @FXML private Label lblCustomerLocation;
    @FXML private Label lblCustomerOpenBalance;

    @FXML private Label lblEmployeeName;
    @FXML private Label lblEmployeeRole;

    @FXML private TableView<Transactiondetail> tblDetails;
    @FXML private TableColumn<Transactiondetail, String> colItem;
    @FXML private TableColumn<Transactiondetail, Double> colQty;
    @FXML private TableColumn<Transactiondetail, BigDecimal> colUnitPrice;
    @FXML private TableColumn<Transactiondetail, BigDecimal> colLineTotal;

    @FXML private Label lblSubTotal;
    @FXML private Label lblTax;
    @FXML private Label lblGrandTotal;

    @FXML private TableView<Payment> tblPayments;
    @FXML private TableColumn<Payment, String> colPayType;
    @FXML private TableColumn<Payment, BigDecimal> colPayAmount;
    @FXML private TableColumn<Payment, String> colPayDate;

    private final InvoiceService invoiceService;

    // Formatting
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("ar", "EG"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    // current shown transaction
    private Transaction transaction;

    public InvoicePreviewController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @FXML
    public void initialize() {
        // set logo if exists in resources
        try {
            URL logoUrl = getClass().getResource("/images/company-logo.png");
            if (logoUrl != null) {
                companyLogo.setImage(new Image(logoUrl.toExternalForm()));
            }
        } catch (Exception ignored) {}

        // Table columns mapping
        colItem.setCellValueFactory(cell -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cell.getValue().getItem() == null ? "" : cell.getValue().getItem().getName()));

        colQty.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getQuantity()));


        colUnitPrice.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getSellingPrice()));

        colLineTotal.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getComulativePrice()));

        // Payments columns
        colPayType.setCellValueFactory(cell -> javafx.beans.binding.Bindings.createStringBinding(
                () -> cell.getValue().getPaymentType()));

        colPayAmount.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getAmount()));

        colPayDate.setCellValueFactory(cell -> javafx.beans.binding.Bindings.createStringBinding(
                () -> dateFormatter.format(cell.getValue().getPaymentDate())));

        // Set table cell factories to show nicely formatted numbers
        colUnitPrice.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : currencyFormat.format(price));
            }
        });
        colLineTotal.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? "" : currencyFormat.format(val));
            }
        });
        colPayAmount.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? "" : currencyFormat.format(val));
            }
        });
    }

    /**
     * Called externally after FXML load to set which transaction to show.
     */
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        loadAndRender();
    }

    private void loadAndRender() {
        if (transaction == null) return;

        // Load fresh data via the service to ensure relationships are initialized
        var invoiceData = invoiceService.prepareInvoice(transaction.getId());

        // Header
        lblInvoiceNumber.setText(String.valueOf(invoiceData.getTransaction().getId()));
        lblInvoiceDate.setText(dateFormatter.format(invoiceData.getTransaction().getTransactionDate()));

        // Customer & employee
        if (invoiceData.getTransaction().getPerson() != null) {
            lblCustomerName.setText(invoiceData.getTransaction().getPerson().getName());
            lblCustomerLocation.setText(invoiceData.getTransaction().getPerson().getLocation() == null ? "" :
                    invoiceData.getTransaction().getPerson().getLocation());
            lblCustomerOpenBalance.setText("رصيد سابق: " + currencyFormat.format(
                    invoiceData.getTransaction().getPerson().getOpenBalance() == null ?
                            BigDecimal.ZERO : invoiceData.getTransaction().getPerson().getOpenBalance()));
        }
        if (invoiceData.getTransaction().getSalesRep() != null) {
            lblEmployeeName.setText(invoiceData.getTransaction().getSalesRep().getName());
            lblEmployeeRole.setText(invoiceData.getTransaction().getSalesRep().getRole());
        }

        // Table of details
        List<Transactiondetail> details = invoiceData.getDetails();
        tblDetails.setItems(FXCollections.observableArrayList(details));

        // Totals
        lblSubTotal.setText(currencyFormat.format(invoiceData.getSubTotal()));
        lblTax.setText(currencyFormat.format(invoiceData.getTaxAmount()) + " (٪" + invoiceData.getTaxPercent() + ")");
        lblGrandTotal.setText(currencyFormat.format(invoiceData.getGrandTotal()));

        // Payments
        List<Payment> payments = invoiceData.getPayments();
        tblPayments.setItems(FXCollections.observableArrayList(payments));
    }

    @FXML
    public void onPrint() {
        // Basic JavaFX printing: use PrinterJob
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(tblDetails.getScene().getWindow())) {
            Node printable = tblDetails.getScene().getRoot();
            boolean success = job.printPage(printable);
            if (success) job.endJob();
        }
    }

    @FXML
    public void onClose() {
        // close the Stage
        Stage stage = (Stage) lblInvoiceNumber.getScene().getWindow();
        stage.close();
    }
}
