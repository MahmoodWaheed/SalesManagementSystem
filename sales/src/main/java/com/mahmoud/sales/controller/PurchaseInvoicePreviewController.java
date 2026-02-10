package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Payment;
import com.mahmoud.sales.entity.Purchasedetail;
import com.mahmoud.sales.entity.Purchasetransaction;
import com.mahmoud.sales.service.PurchaseInvoiceService;
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
public class PurchaseInvoicePreviewController {

    @FXML private ImageView companyLogo;
    @FXML private Label lblInvoiceNumber;
    @FXML private Label lblInvoiceDate;

    @FXML private Label lblSupplierName;
    @FXML private Label lblSupplierLocation;
    @FXML private Label lblSupplierOpenBalance;

    @FXML private Label lblEmployeeName;
    @FXML private Label lblEmployeeRole;

    @FXML private TableView<Purchasedetail> tblDetails;
    @FXML private TableColumn<Purchasedetail, String> colItem;
    @FXML private TableColumn<Purchasedetail, Double> colQty;
    @FXML private TableColumn<Purchasedetail, BigDecimal> colUnitPrice;
    @FXML private TableColumn<Purchasedetail, BigDecimal> colLineTotal;

    @FXML private Label lblSubTotal;
    @FXML private Label lblTax;
    @FXML private Label lblGrandTotal;

    @FXML private TableView<Payment> tblPayments;
    @FXML private TableColumn<Payment, String> colPayType;
    @FXML private TableColumn<Payment, BigDecimal> colPayAmount;
    @FXML private TableColumn<Payment, String> colPayDate;

    private final PurchaseInvoiceService invoiceService;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("ar","EG"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private Purchasetransaction purchaseTransaction;

    public PurchaseInvoicePreviewController(PurchaseInvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @FXML
    public void initialize() {
        // Load optional logo
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
        // Purchase details table
        colItem.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                cell.getValue().getItem() == null ? "" : cell.getValue().getItem().getName()
        ));

        colQty.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                cell.getValue().getQuantity()
        ));

        colUnitPrice.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
                cell.getValue().getPurchasingPrice()
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
     * Called to pass a purchase transaction reference (from PurchaseFormController).
     * We reload full invoice data inside the service to avoid lazy-init problems.
     */
    public void setPurchaseTransaction(Purchasetransaction purchaseTransaction) {
        this.purchaseTransaction = purchaseTransaction;
        Platform.runLater(this::loadAndRender);
    }

    /**
     * NEW METHOD: Set transaction data directly for preview without database lookup
     * Used for unsaved transactions
     */
    public void setPurchaseTransactionDirect(Purchasetransaction purchaseTransaction,
                                             List<Purchasedetail> details,
                                             List<Payment> payments) {
        this.purchaseTransaction = purchaseTransaction;
        Platform.runLater(() -> renderDirectData(purchaseTransaction, details, payments));
    }

    /**
     * NEW METHOD: Render invoice using in-memory data (no database lookup)
     */
    private void renderDirectData(Purchasetransaction tx,
                                  List<Purchasedetail> details,
                                  List<Payment> payments) {
        try {
            // Update invoice header
            lblInvoiceNumber.setText("رقم فاتورة الشراء: " +
                    (tx.getId() != null && tx.getId() > 0 ? tx.getId() : "PREVIEW"));
            lblInvoiceDate.setText(tx.getPurchaseDate() == null ? "" :
                    dateFormatter.format(tx.getPurchaseDate()));

            // Update supplier info
            if (tx.getPerson() != null) {
                lblSupplierName.setText(tx.getPerson().getName());
                lblSupplierLocation.setText(tx.getPerson().getLocation() == null ? "" :
                        tx.getPerson().getLocation());
                if (lblSupplierOpenBalance != null) {
                    lblSupplierOpenBalance.setText("رصيد سابق: " + currencyFormat.format(
                            tx.getPerson().getOpenBalance() == null ?
                                    BigDecimal.ZERO : tx.getPerson().getOpenBalance()));
                }
            } else {
                lblSupplierName.setText("غير محدد");
                lblSupplierLocation.setText("");
                if (lblSupplierOpenBalance != null) {
                    lblSupplierOpenBalance.setText("");
                }
            }

            // Update employee info (purchasing agent)
            if (lblEmployeeName != null && lblEmployeeRole != null) {
                lblEmployeeName.setText("موظف الشراء");
                lblEmployeeRole.setText("Purchasing Agent");
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

    private void loadAndRender() {
        if (this.purchaseTransaction == null) {
            showError("No purchase transaction provided");
            return;
        }

        try {
            // Check if transaction is saved (has valid ID)
            if (this.purchaseTransaction.getId() == null || this.purchaseTransaction.getId() <= 0) {
                // Unsaved transaction - use direct rendering (already set via setPurchaseTransactionDirect)
                showInfo("Previewing unsaved purchase transaction");
                return;
            }

            // Saved transaction - load from database
            PurchaseInvoiceService.PurchaseInvoiceData data = invoiceService.preparePurchaseInvoice(this.purchaseTransaction.getId());

            // Update UI with data
            updateInvoiceHeader(data);
            updateSupplierInfo(data);
            updateEmployeeInfo(data);
            updateDetailsTable(data);
            updateTotals(data);
            updatePaymentsTable(data);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading invoice data: " + e.getMessage());
        }
    }

    private void updateInvoiceHeader(PurchaseInvoiceService.PurchaseInvoiceData data) {
        lblInvoiceNumber.setText("رقم فاتورة الشراء: " + data.getPurchaseTransaction().getId());
        lblInvoiceDate.setText(data.getPurchaseTransaction().getPurchaseDate() == null ? ""
                : dateFormatter.format(data.getPurchaseTransaction().getPurchaseDate()));
    }

    private void updateSupplierInfo(PurchaseInvoiceService.PurchaseInvoiceData data) {
        if (data.getPurchaseTransaction().getPerson() != null) {
            lblSupplierName.setText(data.getPurchaseTransaction().getPerson().getName());
            lblSupplierLocation.setText(data.getPurchaseTransaction().getPerson().getLocation() == null ? "" :
                    data.getPurchaseTransaction().getPerson().getLocation());

            if (lblSupplierOpenBalance != null) {
                lblSupplierOpenBalance.setText("رصيد سابق: " + currencyFormat.format(
                        data.getPurchaseTransaction().getPerson().getOpenBalance() == null ?
                                BigDecimal.ZERO : data.getPurchaseTransaction().getPerson().getOpenBalance()));
            }
        } else {
            lblSupplierName.setText("غير محدد");
            lblSupplierLocation.setText("");
            if (lblSupplierOpenBalance != null) {
                lblSupplierOpenBalance.setText("");
            }
        }
    }

    private void updateEmployeeInfo(PurchaseInvoiceService.PurchaseInvoiceData data) {
        if (lblEmployeeName != null && lblEmployeeRole != null) {
            lblEmployeeName.setText("موظف الشراء");
            lblEmployeeRole.setText("Purchasing Agent");
        }
    }

    private void updateDetailsTable(PurchaseInvoiceService.PurchaseInvoiceData data) {
        List<Purchasedetail> details = data.getDetails();
        if (details != null && !details.isEmpty()) {
            tblDetails.setItems(FXCollections.observableArrayList(details));
        } else {
            tblDetails.setItems(FXCollections.observableArrayList());
        }
    }

    private void updateTotals(PurchaseInvoiceService.PurchaseInvoiceData data) {
        lblSubTotal.setText(currencyFormat.format(data.getSubTotal()));
        lblTax.setText(currencyFormat.format(data.getTaxAmount()) +
                " (%" + data.getTaxPercent() + ")");
        lblGrandTotal.setText(currencyFormat.format(data.getGrandTotal()));
    }

    private void updatePaymentsTable(PurchaseInvoiceService.PurchaseInvoiceData data) {
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