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
        } catch (Exception ignore) {
            // Logo is optional, don't fail if not found
        }

        // Setup table columns
        setupTableColumns();
    }

    private void setupTableColumns() {
        // Transaction details table
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

        // Format money columns
        colUnitPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : currencyFormat.format(price));
            }
        });

        colLineTotal.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? "" : currencyFormat.format(val));
            }
        });

        // Payment table columns
        colPayType.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                c.getValue().getPaymentType()
        ));

        colPayAmount.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                c.getValue().getAmount()
        ));
        colPayAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? "" : currencyFormat.format(val));
            }
        });

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
    /**
     * NEW METHOD: Set transaction data directly for preview without database lookup
     * Used for unsaved transactions
     */
    public void setTransactionDirect(Transaction transaction,
                                     List<Transactiondetail> details,
                                     List<Payment> payments) {
        this.transaction = transaction;
        Platform.runLater(() -> renderDirectData(transaction, details, payments));
    }

    /**
     * NEW METHOD: Render invoice using in-memory data (no database lookup)
     */
    private void renderDirectData(Transaction tx,
                                  List<Transactiondetail> details,
                                  List<Payment> payments) {
        try {
            // Update invoice header
            lblInvoiceNumber.setText("رقم الفاتورة: " +
                    (tx.getId() != null && tx.getId() > 0 ? tx.getId() : "PREVIEW"));
            lblInvoiceDate.setText(tx.getTransactionDate() == null ? "" :
                    dateFormatter.format(tx.getTransactionDate()));

            // Update customer info
            if (tx.getPerson() != null) {
                lblCustomerName.setText(tx.getPerson().getName());
                lblCustomerLocation.setText(tx.getPerson().getLocation() == null ? "" :
                        tx.getPerson().getLocation());
                if (lblCustomerOpenBalance != null) {
                    lblCustomerOpenBalance.setText("رصيد سابق: " + currencyFormat.format(
                            tx.getPerson().getOpenBalance() == null ?
                                    BigDecimal.ZERO : tx.getPerson().getOpenBalance()));
                }
            } else {
                lblCustomerName.setText("غير محدد");
                lblCustomerLocation.setText("");
                if (lblCustomerOpenBalance != null) {
                    lblCustomerOpenBalance.setText("");
                }
            }

            // Update employee info
            if (tx.getSalesRep() != null) {
                lblEmployeeName.setText(tx.getSalesRep().getName());
                lblEmployeeRole.setText(tx.getSalesRep().getRole());
            } else {
                lblEmployeeName.setText("غير محدد");
                lblEmployeeRole.setText("");
            }

            // Update details table
            if (details != null && !details.isEmpty()) {
                tblDetails.setItems(FXCollections.observableArrayList(details));
            } else {
                tblDetails.setItems(FXCollections.observableArrayList());
            }

            // Calculate and update totals
            BigDecimal subTotal = details.stream()
                    .map(d -> d.getComulativePrice() == null ? BigDecimal.ZERO : d.getComulativePrice())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal taxPercent = BigDecimal.ZERO;
            BigDecimal taxAmount = subTotal.multiply(taxPercent)
                    .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal grandTotal = subTotal.add(taxAmount);

            lblSubTotal.setText(currencyFormat.format(subTotal));
            lblTax.setText(currencyFormat.format(taxAmount) + " (%" + taxPercent + ")");
            lblGrandTotal.setText(currencyFormat.format(grandTotal));

            // Update payments table
            if (payments != null && !payments.isEmpty()) {
                tblPayments.setItems(FXCollections.observableArrayList(payments));
            } else {
                tblPayments.setItems(FXCollections.observableArrayList());
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error rendering invoice: " + e.getMessage());
        }
    }
    private void updateTotalsDirect(List<Transactiondetail> details) {
        BigDecimal subTotal = details.stream()
                .map(d -> d.getComulativePrice() == null ? BigDecimal.ZERO : d.getComulativePrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxPercent = BigDecimal.ZERO;
        BigDecimal taxAmount = subTotal.multiply(taxPercent)
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal grandTotal = subTotal.add(taxAmount);

        lblSubTotal.setText(currencyFormat.format(subTotal));
        lblTax.setText(currencyFormat.format(taxAmount) + " (%" + taxPercent + ")");
        lblGrandTotal.setText(currencyFormat.format(grandTotal));
    }

    private void updatePaymentsTableDirect(List<Payment> payments) {
        if (payments != null && !payments.isEmpty()) {
            tblPayments.setItems(FXCollections.observableArrayList(payments));
        } else {
            tblPayments.setItems(FXCollections.observableArrayList());
        }
    }

    private void loadAndRender() {
        if (this.transaction == null) {
            showError("No transaction provided");
            return;
        }

        try {
            // Check if transaction is saved (has valid ID)
            if (this.transaction.getId() == null || this.transaction.getId() <= 0) {
                // Unsaved transaction - use direct rendering (already set via setTransactionDirect)
                showInfo("Previewing unsaved transaction");
                return;
            }

            // Saved transaction - load from database
            InvoiceService.InvoiceData data = invoiceService.prepareInvoice(this.transaction.getId());

            // Update UI with data
            updateInvoiceHeader(data);
            updateCustomerInfo(data);
            updateEmployeeInfo(data);
            updateDetailsTable(data);
            updateTotals(data);
            updatePaymentsTable(data);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading invoice data: " + e.getMessage());
        }
    }

    private void updateInvoiceHeader(InvoiceService.InvoiceData data) {
        lblInvoiceNumber.setText("رقم الفاتورة: " + data.getTransaction().getId());
        lblInvoiceDate.setText(data.getTransaction().getTransactionDate() == null ? ""
                : dateFormatter.format(data.getTransaction().getTransactionDate()));
    }

    private void updateCustomerInfo(InvoiceService.InvoiceData data) {
        if (data.getTransaction().getPerson() != null) {
            lblCustomerName.setText(data.getTransaction().getPerson().getName());
            lblCustomerLocation.setText(data.getTransaction().getPerson().getLocation() == null ? "" :
                    data.getTransaction().getPerson().getLocation());

            if (lblCustomerOpenBalance != null) {
                lblCustomerOpenBalance.setText("رصيد سابق: " + currencyFormat.format(
                        data.getTransaction().getPerson().getOpenBalance() == null ?
                                BigDecimal.ZERO : data.getTransaction().getPerson().getOpenBalance()));
            }
        } else {
            lblCustomerName.setText("غير محدد");
            lblCustomerLocation.setText("");
            if (lblCustomerOpenBalance != null) {
                lblCustomerOpenBalance.setText("");
            }
        }
    }

    private void updateEmployeeInfo(InvoiceService.InvoiceData data) {
        if (data.getTransaction().getSalesRep() != null) {
            lblEmployeeName.setText(data.getTransaction().getSalesRep().getName());
            lblEmployeeRole.setText(data.getTransaction().getSalesRep().getRole());
        } else {
            lblEmployeeName.setText("غير محدد");
            lblEmployeeRole.setText("");
        }
    }

    private void updateDetailsTable(InvoiceService.InvoiceData data) {
        List<Transactiondetail> details = data.getDetails();
        if (details != null && !details.isEmpty()) {
            tblDetails.setItems(FXCollections.observableArrayList(details));
        } else {
            tblDetails.setItems(FXCollections.observableArrayList());
        }
    }

    private void updateTotals(InvoiceService.InvoiceData data) {
        lblSubTotal.setText(currencyFormat.format(data.getSubTotal()));
        lblTax.setText(currencyFormat.format(data.getTaxAmount()) +
                " (%" + data.getTaxPercent() + ")");
        lblGrandTotal.setText(currencyFormat.format(data.getGrandTotal()));
    }

    private void updatePaymentsTable(InvoiceService.InvoiceData data) {
        List<Payment> payments = data.getPayments();
        if (payments != null && !payments.isEmpty()) {
            tblPayments.setItems(FXCollections.observableArrayList(payments));
        } else {
            tblPayments.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    public void onPrint() {
        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null) {
                // Show print dialog
                boolean proceed = job.showPrintDialog(tblDetails.getScene().getWindow());

                if (proceed) {
                    // Get the entire scene root for printing
                    Node root = tblDetails.getScene().getRoot();

                    // Scale if needed to fit page
                    double scaleX = job.getJobSettings().getPageLayout().getPrintableWidth() / root.getBoundsInParent().getWidth();
                    double scaleY = job.getJobSettings().getPageLayout().getPrintableHeight() / root.getBoundsInParent().getHeight();
                    double scale = Math.min(scaleX, scaleY);

                    if (scale < 1.0) {
                        root.getTransforms().add(new javafx.scene.transform.Scale(scale, scale));
                    }

                    // Print the page
                    boolean success = job.printPage(root);

                    // Remove scale transform
                    if (scale < 1.0) {
                        root.getTransforms().clear();
                    }

                    if (success) {
                        job.endJob();
                        showInfo("تم إرسال الفاتورة إلى الطابعة بنجاح");
                    } else {
                        showError("فشلت عملية الطباعة");
                    }
                } else {
                    // User cancelled
                    showInfo("تم إلغاء الطباعة");
                }
            } else {
                showError("لم يتم العثور على طابعة متاحة");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("خطأ في الطباعة: " + e.getMessage());
        }
    }

    @FXML
    public void onClose() {
        Stage stage = (Stage) lblInvoiceNumber.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("خطأ");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("معلومات");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}