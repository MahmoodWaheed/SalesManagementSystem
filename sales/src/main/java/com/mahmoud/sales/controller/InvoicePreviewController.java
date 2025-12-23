package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Payment;
import com.mahmoud.sales.entity.Transaction;
import com.mahmoud.sales.entity.Transactiondetail;
import com.mahmoud.sales.service.InvoiceService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.ZoneId;
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

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("ar","EG"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private Transaction transaction;

    public InvoicePreviewController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @FXML
    public void initialize() {
        // load optional logo
        try {
            URL logoUrl = getClass().getResource("/images/company-logo.png");
            if (logoUrl != null) {
                companyLogo.setImage(new Image(logoUrl.toExternalForm()));
            }
        } catch (Exception ignore) {}

        // cell value factories (use ReadOnlyObjectWrapper via constructor)
        colItem.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                cell.getValue().getItem() == null ? "" : cell.getValue().getItem().getName()
        ));
        colQty.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                cell.getValue().getQuantity()
        ));
        colUnitPrice.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                cell.getValue().getSellingPrice()
        ));
        colLineTotal.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                cell.getValue().getComulativePrice()
        ));

        // formatting cell factories for money
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

        colPayAmount.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue().getAmount()));
        colPayAmount.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? "" : currencyFormat.format(val));
            }
        });

        colPayType.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue().getPaymentType()));
        colPayDate.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                c.getValue().getPaymentDate() == null ? "" : dateFormatter.format(c.getValue().getPaymentDate())
        ));
    }

    /**
     * Called to pass a transaction reference (from SalesFormController).
     * We reload full invoice data inside the service to avoid lazy-init problems.
     */
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        Platform.runLater(this::loadAndRender);
    }

    private void loadAndRender() {
        if (this.transaction == null) return;

        InvoiceService.InvoiceData data = invoiceService.prepareInvoice(this.transaction.getId());

        lblInvoiceNumber.setText(String.valueOf(data.getTransaction().getId()));
        lblInvoiceDate.setText(data.getTransaction().getTransactionDate() == null ? ""
                : dateFormatter.format(data.getTransaction().getTransactionDate()));

        if (data.getTransaction().getPerson() != null) {
            lblCustomerName.setText(data.getTransaction().getPerson().getName());
            lblCustomerLocation.setText(data.getTransaction().getPerson().getLocation() == null ? "" :
                    data.getTransaction().getPerson().getLocation());
            lblCustomerOpenBalance.setText("رصيد سابق: " + currencyFormat.format(
                    data.getTransaction().getPerson().getOpenBalance() == null ?
                            BigDecimal.ZERO : data.getTransaction().getPerson().getOpenBalance()));
        }

        if (data.getTransaction().getSalesRep() != null) {
            lblEmployeeName.setText(data.getTransaction().getSalesRep().getName());
            lblEmployeeRole.setText(data.getTransaction().getSalesRep().getRole());
        }

        List<Transactiondetail> details = data.getDetails();
        tblDetails.setItems(FXCollections.observableArrayList(details));

        lblSubTotal.setText(currencyFormat.format(data.getSubTotal()));
        lblTax.setText(currencyFormat.format(data.getTaxAmount()) + " (٪" + data.getTaxPercent() + ")");
        lblGrandTotal.setText(currencyFormat.format(data.getGrandTotal()));

        tblPayments.setItems(FXCollections.observableArrayList(data.getPayments()));
    }

    @FXML
    public void onPrint() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(tblDetails.getScene().getWindow())) {
            Node root = tblDetails.getScene().getRoot();
            boolean success = job.printPage(root);
            if (success) job.endJob();
        }
    }

    @FXML
    public void onClose() {
        Stage stage = (Stage) lblInvoiceNumber.getScene().getWindow();
        stage.close();
    }
}
