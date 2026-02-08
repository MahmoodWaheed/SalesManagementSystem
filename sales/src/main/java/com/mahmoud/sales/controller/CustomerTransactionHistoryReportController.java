package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Payment;
import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.entity.Transaction;
import com.mahmoud.sales.service.CustomerReportService;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Controller
public class CustomerTransactionHistoryReportController {

    @Autowired
    private CustomerReportService reportService;

    @Autowired
    private PersonService personService;

    // Header controls
    @FXML private Button printButton;
    @FXML private Button exportButton;
    @FXML private Button closeButton;

    // Filter controls
    @FXML private ComboBox<Person> customerComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button generateButton;
    @FXML private Button clearButton;

    // Customer info labels
    @FXML private Label customerNameLabel;
    @FXML private Label customerIdLabel;
    @FXML private Label customerLocationLabel;
    @FXML private Label reportPeriodLabel;

    // Summary labels
    @FXML private Label openingBalanceLabel;
    @FXML private Label totalTransactionsLabel;
    @FXML private Label transactionCountLabel;
    @FXML private Label totalPaymentsLabel;
    @FXML private Label paymentCountLabel;
    @FXML private Label currentBalanceLabel;

    // Transactions table
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> txIdColumn;
    @FXML private TableColumn<Transaction, String> txDateColumn;
    @FXML private TableColumn<Transaction, String> txTypeColumn;
    @FXML private TableColumn<Transaction, String> txSalesRepColumn;
    @FXML private TableColumn<Transaction, BigDecimal> txAmountColumn;
    @FXML private TableColumn<Transaction, String> txNoteColumn;
    @FXML private Label transactionsInfoLabel;

    // Payments table
    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, Integer> payIdColumn;
    @FXML private TableColumn<Payment, String> payDateColumn;
    @FXML private TableColumn<Payment, BigDecimal> payAmountColumn;
    @FXML private TableColumn<Payment, String> payTypeColumn;
    @FXML private TableColumn<Payment, String> payWayColumn;
    @FXML private TableColumn<Payment, Integer> payTransactionColumn;
    @FXML private Label paymentsInfoLabel;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ObservableList<Person> customersList;
    private CustomerReportService.CustomerReportData currentReportData;

    @FXML
    public void initialize() {
        // Wire services
        this.reportService = SpringFXMLLoader.loadController(CustomerReportService.class);
        this.personService = SpringFXMLLoader.loadController(PersonService.class);

        // Setup customer combo box - SIMPLE VERSION (no autocomplete)
        setupCustomerComboBoxSimple();

        // Setup tables
        setupTransactionsTable();
        setupPaymentsTable();

        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusMonths(1));

        // Initialize currency format
        currencyFormat.setMaximumFractionDigits(2);
        currencyFormat.setMinimumFractionDigits(2);
    }

    /**
     * SIMPLE VERSION: Non-editable ComboBox to avoid selection bugs
     * Use this if you experience IndexOutOfBoundsException
     */
    private void setupCustomerComboBoxSimple() {
        // Load all customers
        List<Person> customers = personService.findByType("Customer");
        customersList = FXCollections.observableArrayList(customers);
        customerComboBox.setItems(customersList);

        // String converter for display
        customerComboBox.setConverter(new StringConverter<Person>() {
            @Override
            public String toString(Person person) {
                return person == null ? "" : person.getName() + " (ID: " + person.getId() + ")";
            }

            @Override
            public Person fromString(String string) {
                return null;
            }
        });

        // Make it NON-editable (avoids the bug)
        customerComboBox.setEditable(false);

        // Note: Without editability, you lose autocomplete, but gain stability
        // If you have many customers, consider adding a separate search TextField
    }

    private void setupTransactionsTable() {
        txIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getId()
        ));

        txDateColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getTransactionDate() == null ? "" :
                        dateFormatter.format(cell.getValue().getTransactionDate()
                                .atZone(ZoneId.systemDefault()).toLocalDateTime())
        ));

        txTypeColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getTransactionType() != null ?
                        cell.getValue().getTransactionType().getArabicValue() : "-"
        ));

        txSalesRepColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getSalesRep() != null ?
                        cell.getValue().getSalesRep().getName() : "-"
        ));

        txAmountColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getTotalAmount()
        ));
        txAmountColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText("");
                } else {
                    setText(String.format("%.2f EGP", amount));
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        txNoteColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getNote() != null ? cell.getValue().getNote() : "-"
        ));
    }

    private void setupPaymentsTable() {
        payIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getId()
        ));

        payDateColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getPaymentDate() == null ? "" :
                        dateFormatter.format(cell.getValue().getPaymentDate()
                                .atZone(ZoneId.systemDefault()).toLocalDateTime())
        ));

        payAmountColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getAmount()
        ));
        payAmountColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText("");
                } else {
                    setText(String.format("%.2f EGP", amount));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #48bb78;");
                }
            }
        });

        payTypeColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getPaymentType() != null ?
                        cell.getValue().getPaymentType() : "-"
        ));

        payWayColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getPaymentWay() != null ?
                        cell.getValue().getPaymentWay() : "-"
        ));

        payTransactionColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getTransaction() != null ?
                        cell.getValue().getTransaction().getId() : null
        ));
    }

    @FXML
    public void onGenerateReport() {
        try {
            // Validate selection
            Person selectedCustomer = customerComboBox.getValue();
            if (selectedCustomer == null) {
                showAlert("Please select a customer");
                return;
            }

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showAlert("Please select both start and end dates");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showAlert("Start date cannot be after end date");
                return;
            }

            // Convert to Instant
            Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            // Generate report
            currentReportData = reportService.generateCustomerReport(
                    selectedCustomer.getId(),
                    startInstant,
                    endInstant
            );

            // Update UI
            updateReportDisplay();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error generating report: " + e.getMessage());
        }
    }

    private void updateReportDisplay() {
        if (currentReportData == null) return;

        // Update customer info
        customerNameLabel.setText(currentReportData.getCustomer().getName());
        customerIdLabel.setText(String.valueOf(currentReportData.getCustomer().getId()));
        customerLocationLabel.setText(currentReportData.getCustomer().getLocation() != null ?
                currentReportData.getCustomer().getLocation() : "-");

        // Update report period
        LocalDate start = currentReportData.getStartDate()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = currentReportData.getEndDate()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        reportPeriodLabel.setText(start + " to " + end);

        // Update summary cards
        openingBalanceLabel.setText(String.format("%.2f EGP",
                currentReportData.getOpeningBalance()));

        totalTransactionsLabel.setText(String.format("%.2f EGP",
                currentReportData.getTotalTransactions()));
        transactionCountLabel.setText(currentReportData.getTransactions().size() + " transactions");

        totalPaymentsLabel.setText(String.format("%.2f EGP",
                currentReportData.getTotalPayments()));
        paymentCountLabel.setText(currentReportData.getPayments().size() + " payments");

        currentBalanceLabel.setText(String.format("%.2f EGP",
                currentReportData.getCurrentBalance()));

        // Style current balance based on value
        if (currentReportData.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
            currentBalanceLabel.setStyle("-fx-text-fill: #e53e3e;"); // Red for debt
        } else if (currentReportData.getCurrentBalance().compareTo(BigDecimal.ZERO) < 0) {
            currentBalanceLabel.setStyle("-fx-text-fill: #48bb78;"); // Green for credit
        } else {
            currentBalanceLabel.setStyle("-fx-text-fill: #4a5568;"); // Gray for zero
        }

        // Update transactions table
        transactionsTable.setItems(FXCollections.observableArrayList(
                currentReportData.getTransactions()
        ));
        transactionsInfoLabel.setText("(Showing " +
                currentReportData.getTransactions().size() + " transactions)");

        // Update payments table
        paymentsTable.setItems(FXCollections.observableArrayList(
                currentReportData.getPayments()
        ));
        paymentsInfoLabel.setText("(Showing " +
                currentReportData.getPayments().size() + " payments)");
    }

    @FXML
    public void onClearFilters() {
        customerComboBox.setValue(null);
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        // Clear display
        customerNameLabel.setText("-");
        customerIdLabel.setText("-");
        customerLocationLabel.setText("-");
        reportPeriodLabel.setText("-");

        openingBalanceLabel.setText("0.00 EGP");
        totalTransactionsLabel.setText("0.00 EGP");
        transactionCountLabel.setText("0 transactions");
        totalPaymentsLabel.setText("0.00 EGP");
        paymentCountLabel.setText("0 payments");
        currentBalanceLabel.setText("0.00 EGP");

        transactionsTable.setItems(FXCollections.observableArrayList());
        paymentsTable.setItems(FXCollections.observableArrayList());

        transactionsInfoLabel.setText("(Showing 0 transactions)");
        paymentsInfoLabel.setText("(Showing 0 payments)");

        currentReportData = null;
    }

    @FXML
    public void onPrintReport() {
        try {
            if (currentReportData == null) {
                showAlert("Please generate a report first");
                return;
            }

            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null) {
                boolean proceed = job.showPrintDialog(printButton.getScene().getWindow());

                if (proceed) {
                    Node printNode = printButton.getScene().getRoot();

                    // Scale to fit page
                    double scaleX = job.getJobSettings().getPageLayout().getPrintableWidth() /
                            printNode.getBoundsInParent().getWidth();
                    double scaleY = job.getJobSettings().getPageLayout().getPrintableHeight() /
                            printNode.getBoundsInParent().getHeight();
                    double scale = Math.min(scaleX, scaleY);

                    if (scale < 1.0) {
                        printNode.getTransforms().add(new javafx.scene.transform.Scale(scale, scale));
                    }

                    boolean success = job.printPage(printNode);

                    if (scale < 1.0) {
                        printNode.getTransforms().clear();
                    }

                    if (success) {
                        job.endJob();
                        showInfo("Report sent to printer successfully");
                    } else {
                        showAlert("Print failed");
                    }
                }
            } else {
                showAlert("No printer available");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error printing report: " + e.getMessage());
        }
    }

    @FXML
    public void onExportPDF() {
        showInfo("PDF export feature coming soon!\nFor now, please use the Print button.");
    }

    @FXML
    public void onClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Customer Report");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Customer Report");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
