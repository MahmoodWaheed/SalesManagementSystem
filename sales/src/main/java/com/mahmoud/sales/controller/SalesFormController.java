
package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.*;
import com.mahmoud.sales.service.*;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.DoubleStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

// ADD at top
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
@Controller
public class SalesFormController {

    // Services
    private TransactionService transactionService;
    private TransactiondetailService transactiondetailService;
    private PaymentService paymentService;
    private ItemService itemService;
    private PersonService personService;
    private EmployeeService employeeService;
    private InvoiceService invoiceService;
    private Transaction currentTransaction;

    @FXML private ComboBox<Person> customerComboBox;
    @FXML private ComboBox<Employee> employeeComboBox;
    @FXML private ComboBox<TransactionType> transactionTypeComboBox;
    @FXML private ComboBox<String> pWayComboBox;
    @FXML private TextField transactionNumberField;
    @FXML private TextField transactionNoteField;
    @FXML private Label currentInvoiceLabel;
    @FXML private TextField totalAmountField;
    @FXML private TextField pFeeField;

    // FIX #4: Customer Current Balance Field
    @FXML private TextField customerBalanceField;

    @FXML private Button saveTransactionButton;
    @FXML private Button newTransactionButton;
    @FXML private Button printInvoiceButton;
    @FXML private Button firstTransactionButton;
    @FXML private Button previousTransactionButton;
    @FXML private Button nextTransactionButton;
    @FXML private Button lastTransactionButton;
    @FXML private TableView<Transactiondetail> transactionDetailTable;
    @FXML private TableColumn<Transactiondetail, Integer> tdIdColumn;
    @FXML private TableColumn<Transactiondetail, Integer> itemIdColumn;
    @FXML private TableColumn<Transactiondetail, Item> itemColumn;
    @FXML private TableColumn<Transactiondetail, Double> quantityColumn;
    @FXML private TableColumn<Transactiondetail, BigDecimal> sellingPriceColumn;
    @FXML private TableColumn<Transactiondetail, BigDecimal> comulativePriceColumn;
    @FXML private TextField tdQuantityField;
    @FXML private TextField tdSellingPriceField;
    @FXML private TextField tdComulativePriceField;
    @FXML private Button addTransactionDetailButton;
    @FXML private Button deleteTransactionDetailButton;

    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, Integer> pIdColumn;
    @FXML private TableColumn<Payment, BigDecimal> pAmountColumn;
    @FXML private TableColumn<Payment, Instant> pDateColumn;
    @FXML private TableColumn<Payment, String> pTypeColumn;
    @FXML private TableColumn<Payment, String> pWayColumn;
    @FXML private TextField pAmountField;
    @FXML private TextField pTypeField;
    @FXML private TextField pWayField;
    @FXML private Button addPaymentButton;
    @FXML private Button deletePaymentButton;

    // Payment way options with withdrawal fees
    private static final ObservableList<String> PAYMENT_WAYS = FXCollections.observableArrayList(
            "Cash - نقدي",
            "Card - بطاقة",
            "Bank Transfer - تحويل بنكي",
            "Check - شيك",
            "Credit - آجل",
            "Vodafone Cash - فودافون كاش",
            "Etisalat Cash - اتصالات كاش"
    );

    // Payment ways that require withdrawal fees
    private static final String VODAFONE_CASH = "Vodafone Cash - فودافون كاش";
    private static final String ETISALAT_CASH = "Etisalat Cash - اتصالات كاش";
    private static final double FEE_PER_1K = 15.0; // 15 EGP per 1,000 EGP

    // Payment type (always Debtor for sales)
    private static final String PAYMENT_TYPE_DEBTOR = "Debtor - مدين";

    private BigDecimal currentWithdrawalFee = BigDecimal.ZERO;

    // FIX #3: Map to store withdrawal fees for each payment (for persistence)
    private Map<Payment, BigDecimal> paymentFeesMap = new HashMap<>();

    // FIX #3: Store total fees to be persisted
    private BigDecimal totalWithdrawalFees = BigDecimal.ZERO;

    private ObservableList<Transactiondetail> transactiondetailList;
    private ObservableList<Payment> paymentList;
    private ObservableList<Person> masterCustomerList;
    private ObservableList<Item> itemsList;
    private ObservableList<Employee> employeesList;
    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    @FXML
    public void initialize() {
        // Wire services
        this.paymentService = SpringFXMLLoader.loadController(PaymentService.class);
        this.transactiondetailService = SpringFXMLLoader.loadController(TransactiondetailService.class);
        this.transactionService = SpringFXMLLoader.loadController(TransactionService.class);
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
        this.employeeService = SpringFXMLLoader.loadController(EmployeeService.class);
        this.itemService = SpringFXMLLoader.loadController(ItemService.class);
        this.invoiceService = SpringFXMLLoader.loadController(InvoiceService.class);

        // Initialize transaction type ComboBox
        initializeTransactionTypeComboBox();

        // Initialize payment way ComboBox
        initializePaymentWayComboBox();

        // Initialize customer ComboBox with improved autocomplete
        initializeCustomerComboBox();

        // Initialize employee ComboBox
        initializeEmployeeComboBox();

        // Initialize items list
        itemsList = FXCollections.observableArrayList(itemService.findAllItems());

        // Setup transaction detail table
        setupTransactionDetailTable();

        // Setup payments table
        setupPaymentsTable();

        // Initialize transaction number and invoice label
        initializeTransactionNumber();
        updateCurrentInvoiceLabel();

        // Initialize lists
        transactiondetailList = FXCollections.observableArrayList();
        transactionDetailTable.setItems(transactiondetailList);
        addBlankDetailRow();

        paymentList = FXCollections.observableArrayList();
        paymentTable.setItems(paymentList);

        // Add listener to payment amount field for fee calculation
        pAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            calculateWithdrawalFee();
        });

        // Keyboard handling for table - TAB to move between cells
        transactionDetailTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();

                TablePosition<Transactiondetail, ?> pos = transactionDetailTable.getFocusModel().getFocusedCell();
                if (pos != null) {
                    int row = pos.getRow();
                    int col = pos.getColumn();

                    // Move to next column or next row
                    if (col < transactionDetailTable.getColumns().size() - 1) {
                        // Move to next column in same row
                        transactionDetailTable.edit(row, transactionDetailTable.getColumns().get(col + 1));
                    } else {
                        // Move to first editable column of next row
                        if (row < transactiondetailList.size() - 1) {
                            transactionDetailTable.getSelectionModel().select(row + 1);
                            transactionDetailTable.edit(row + 1, itemColumn);
                        } else {
                            // Last row - add new blank row
                            addBlankDetailRow();
                            transactionDetailTable.getSelectionModel().selectLast();
                            transactionDetailTable.edit(transactiondetailList.size() - 1, itemColumn);
                        }
                    }
                }
            }
        });
    }

    private void initializeTransactionTypeComboBox() {
        // Populate with TransactionType enum values
        ObservableList<TransactionType> transactionTypes = FXCollections.observableArrayList(
                TransactionType.INVOICE,
                TransactionType.RETURN
        );
        transactionTypeComboBox.setItems(transactionTypes);

        // Set default to INVOICE
        transactionTypeComboBox.setValue(TransactionType.INVOICE);

        // Custom string converter to display Arabic values
        transactionTypeComboBox.setConverter(new StringConverter<TransactionType>() {
            @Override
            public String toString(TransactionType type) {
                return type == null ? "" : type.getArabicValue() + " (" + type.name() + ")";
            }

            @Override
            public TransactionType fromString(String string) {
                return transactionTypeComboBox.getItems().stream()
                        .filter(type -> (type.getArabicValue() + " (" + type.name() + ")").equals(string))
                        .findFirst()
                        .orElse(TransactionType.INVOICE);
            }
        });
    }

    private void initializePaymentWayComboBox() {
        pWayComboBox.setItems(PAYMENT_WAYS);
        // Set default to Cash
        pWayComboBox.setValue("Cash - نقدي");

        // Add listener for fee calculation
        pWayComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            calculateWithdrawalFee();
        });
    }

    private void calculateWithdrawalFee() {
        try {
            String paymentWay = pWayComboBox.getValue();
            String amountText = pAmountField.getText();

            if (paymentWay == null || amountText == null || amountText.trim().isEmpty()) {
                pFeeField.setText("0.00");
                currentWithdrawalFee = BigDecimal.ZERO;
                return;
            }

            // Check if payment way requires withdrawal fee
            if (VODAFONE_CASH.equals(paymentWay) || ETISALAT_CASH.equals(paymentWay)) {
                BigDecimal amount = new BigDecimal(amountText);
                // Calculate fee: 15 EGP per 1,000 EGP
                BigDecimal fee = amount.divide(new BigDecimal("1000"), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(new BigDecimal(FEE_PER_1K));
                fee = fee.setScale(2, BigDecimal.ROUND_HALF_UP);
                pFeeField.setText(fee.toPlainString());
                currentWithdrawalFee = fee;
            } else {
                pFeeField.setText("0.00");
                currentWithdrawalFee = BigDecimal.ZERO;
            }
        } catch (NumberFormatException e) {
            pFeeField.setText("0.00");
            currentWithdrawalFee = BigDecimal.ZERO;
        }
    }

    /**
     * Calculate total withdrawal fees from all payments
     */
    private BigDecimal calculateTotalWithdrawalFees() {
        BigDecimal totalFees = BigDecimal.ZERO;

        for (Map.Entry<Payment, BigDecimal> entry : paymentFeesMap.entrySet()) {
            totalFees = totalFees.add(entry.getValue());
        }

        return totalFees;
    }

    /**
     * Update transaction note with all withdrawal fees
     * FIX #3: Ensure fees are properly formatted for persistence
     */
    private void updateTransactionNoteWithFees() {
        StringBuilder feeNotes = new StringBuilder();

        for (Map.Entry<Payment, BigDecimal> entry : paymentFeesMap.entrySet()) {
            Payment payment = entry.getKey();
            BigDecimal fee = entry.getValue();

            if (fee.compareTo(BigDecimal.ZERO) > 0) {
                if (feeNotes.length() > 0) {
                    feeNotes.append("; ");
                }
                feeNotes.append(String.format("Withdrawal fee (%s): %.2f EGP",
                        payment.getPaymentWay(), fee));
            }
        }

        // Set or update the transaction note
        if (feeNotes.length() > 0) {
            String currentNote = transactionNoteField.getText();

            // Remove old fee notes if any
            if (currentNote != null && currentNote.contains("Withdrawal fee")) {
                // Keep only non-fee notes
                String[] parts = currentNote.split(";");
                StringBuilder cleanNotes = new StringBuilder();
                for (String part : parts) {
                    if (!part.trim().startsWith("Withdrawal fee")) {
                        if (cleanNotes.length() > 0) {
                            cleanNotes.append("; ");
                        }
                        cleanNotes.append(part.trim());
                    }
                }
                currentNote = cleanNotes.toString();
            }

            // Combine clean notes with new fee notes
            if (currentNote != null && !currentNote.trim().isEmpty()) {
                transactionNoteField.setText(currentNote + "; " + feeNotes.toString());
            } else {
                transactionNoteField.setText(feeNotes.toString());
            }
        }
    }

    /**
     * Recalculate total amount including items and withdrawal fees
     */
    private void recalcTotalAmountWithFees() {
        // Calculate items total
        BigDecimal itemsTotal = transactiondetailList.stream()
                .filter(this::isRowFilled)
                .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add withdrawal fees
        BigDecimal totalFees = calculateTotalWithdrawalFees();
        BigDecimal grandTotal = itemsTotal.add(totalFees);

        // Store total fees for persistence
        totalWithdrawalFees = totalFees;

        // Update UI
        totalAmountField.setText(grandTotal.toPlainString());

        // Update customer balance
        updateCustomerBalance();
    }

    /**
     * FIX #4: Calculate and update customer current balance
     * Formula: Opening Balance + Total Transactions - Total Payments
     */
    private void updateCustomerBalance() {
        Person selectedCustomer = customerComboBox.getValue();
        if (selectedCustomer == null) {
            customerBalanceField.setText("0.00");
            return;
        }

        try {
            // Get opening balance
            BigDecimal openingBalance = selectedCustomer.getOpenBalance();
            if (openingBalance == null) {
                openingBalance = BigDecimal.ZERO;
            }

            // Get total transactions for this customer from database
            BigDecimal totalTransactions = personService.calculateRemainingBalance(selectedCustomer.getId());
            if (totalTransactions == null) {
                totalTransactions = BigDecimal.ZERO;
            }

            // Add current unsaved transaction total (if exists)
            String currentTotalText = totalAmountField.getText();
            BigDecimal currentTransactionTotal = BigDecimal.ZERO;
            if (currentTotalText != null && !currentTotalText.trim().isEmpty()) {
                try {
                    currentTransactionTotal = new BigDecimal(currentTotalText);
                } catch (NumberFormatException e) {
                    // Ignore if invalid
                }
            }

            // Calculate total payments from payment table
            BigDecimal currentPayments = paymentList.stream()
                    .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Formula: Opening Balance + Total DB Transactions + Current Transaction - Current Payments
            BigDecimal currentBalance = openingBalance
                    .add(totalTransactions)
                    .add(currentTransactionTotal)
                    .subtract(currentPayments);

            customerBalanceField.setText(currentBalance.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());

        } catch (Exception e) {
            e.printStackTrace();
            customerBalanceField.setText("Error");
        }
    }

    private void initializeCustomerComboBox() {
        // Load all customers
        List<Person> customers = personService.findByType("Customer");
        masterCustomerList = FXCollections.observableArrayList(customers);

        // Set items directly to ComboBox (no filtering at this stage)
        customerComboBox.setItems(masterCustomerList);
        customerComboBox.setEditable(true);

        // String converter for display
        customerComboBox.setConverter(new StringConverter<Person>() {
            @Override
            public String toString(Person person) {
                return person == null ? "" : person.getName();
            }

            @Override
            public Person fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }
                // Find exact match
                return masterCustomerList.stream()
                        .filter(p -> p.getName().equalsIgnoreCase(string.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Get the editor of the ComboBox
        TextField editor = customerComboBox.getEditor();

        // Flag to prevent recursive updates
        final boolean[] isUpdating = {false};

        // Add listener for autocomplete
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating[0]) return;

            isUpdating[0] = true;
            try {
                if (newVal == null || newVal.trim().isEmpty()) {
                    // Show all customers when empty
                    customerComboBox.setItems(masterCustomerList);
                    customerComboBox.hide();
                } else {
                    final String filterText = newVal.trim().toLowerCase();

                    // Filter customers
                    ObservableList<Person> filtered = masterCustomerList.stream()
                            .filter(person -> person.getName().toLowerCase().contains(filterText))
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));

                    customerComboBox.setItems(filtered);

                    // Show dropdown if there are matches and user is typing
                    if (!filtered.isEmpty()) {
                        if (!customerComboBox.isShowing()) {
                            customerComboBox.show();
                        }
                    } else {
                        customerComboBox.hide();
                    }
                }
            } finally {
                isUpdating[0] = false;
            }
        });

        // Handle keyboard events
        editor.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) {
                if (!customerComboBox.isShowing()) {
                    customerComboBox.show();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                // If only one item in list, select it
                if (customerComboBox.getItems().size() == 1) {
                    customerComboBox.setValue(customerComboBox.getItems().get(0));
                    customerComboBox.hide();
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                customerComboBox.hide();
                event.consume();
            }
        });

        // When user selects from dropdown with mouse
        customerComboBox.setOnAction(event -> {
            Person selected = customerComboBox.getValue();
            if (selected != null) {
                editor.setText(selected.getName());
                // FIX #4: Update balance when customer is selected
                updateCustomerBalance();
            }
        });
    }

    private void initializeEmployeeComboBox() {
        employeesList = FXCollections.observableArrayList(employeeService.findAllEmployees());
        employeeComboBox.setItems(employeesList);
        employeeComboBox.setConverter(new StringConverter<Employee>() {
            @Override
            public String toString(Employee e) {
                return e == null ? "" : e.getName();
            }
            @Override
            public Employee fromString(String s) {
                return employeesList.stream()
                        .filter(e -> e.getName().equals(s))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void initializeTransactionNumber() {
        try {
            // Get next transaction ID
            Integer nextId = transactionService.getNextTransactionId();
            transactionNumberField.setText(String.valueOf(nextId));
        } catch (Exception e) {
            transactionNumberField.setText("AUTO");
        }
    }

    private void updateCurrentInvoiceLabel() {
        if (currentTransaction != null && currentTransaction.getId() != null) {
            currentInvoiceLabel.setText("#" + currentTransaction.getId());
            currentInvoiceLabel.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #e8f8f5;");
        } else {
            currentInvoiceLabel.setText("NEW");
            currentInvoiceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fee;");
        }
    }

    private void setupTransactionDetailTable() {
        // Make table editable
        transactionDetailTable.setEditable(true);

        tdIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getId() != null ? cell.getValue().getId().getId() : null
        ));

        itemIdColumn.setCellValueFactory(cellData -> {
            Item it = cellData.getValue().getItem();
            return new ReadOnlyObjectWrapper<>(it != null ? it.getId() : null);
        });

        // Item column with autocomplete
        itemColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getItem()));
        itemColumn.setEditable(true);

        // Custom cell factory for autocomplete
        itemColumn.setCellFactory(column -> {
            ComboBoxTableCell<Transactiondetail, Item> cell = new ComboBoxTableCell<>(
                    new StringConverter<Item>() {
                        @Override
                        public String toString(Item item) {
                            return item == null ? "" : item.getName();
                        }
                        @Override
                        public Item fromString(String string) {
                            if (string == null || string.trim().isEmpty()) {
                                return null;
                            }
                            return itemsList.stream()
                                    .filter(i -> i.getName().equalsIgnoreCase(string.trim()))
                                    .findFirst()
                                    .orElse(null);
                        }
                    },
                    itemsList.toArray(new Item[0])
            );

            // Make ComboBox editable for autocomplete
            cell.setComboBoxEditable(true);

            return cell;
        });

        itemColumn.setOnEditCommit(e -> {
            try {
                Transactiondetail d = e.getRowValue();
                if (d == null) return;

                Item selectedItem = e.getNewValue();
                d.setItem(selectedItem);

                // Auto-fill selling price from item
                if (selectedItem != null && selectedItem.getSellingPrice() != null) {
                    d.setSellingPrice(selectedItem.getSellingPrice());
                }

                // Set default quantity if not set
                if (d.getQuantity() == null || d.getQuantity() == 0.0) {
                    d.setQuantity(1.0);
                }

                recalcCumulativePrice(d);
                recalcTotalAmount();

                // Refresh table first
                transactionDetailTable.refresh();

                // Add blank row if this was the last row
                int currentIndex = transactiondetailList.indexOf(d);
                if (currentIndex == transactiondetailList.size() - 1) {
                    Platform.runLater(() -> {
                        addBlankDetailRow();
                        transactionDetailTable.refresh();
                    });
                }

                // Move to quantity column after a slight delay
                Platform.runLater(() -> {
                    int rowIndex = transactiondetailList.indexOf(d);
                    if (rowIndex >= 0) {
                        transactionDetailTable.getSelectionModel().select(rowIndex);
                        transactionDetailTable.edit(rowIndex, quantityColumn);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setEditable(true);
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        quantityColumn.setOnEditCommit(event -> {
            try {
                Transactiondetail detail = event.getRowValue();
                if (detail == null) return;

                detail.setQuantity(event.getNewValue());
                recalcCumulativePrice(detail);
                recalcTotalAmount();

                // Refresh table
                transactionDetailTable.refresh();

                // Move to next row or add new row
                int currentIndex = transactiondetailList.indexOf(detail);
                if (currentIndex == transactiondetailList.size() - 1 && isRowFilled(detail)) {
                    Platform.runLater(() -> {
                        addBlankDetailRow();
                        transactionDetailTable.getSelectionModel().select(currentIndex + 1);
                        transactionDetailTable.edit(currentIndex + 1, itemColumn);
                    });
                } else if (currentIndex < transactiondetailList.size() - 1) {
                    Platform.runLater(() -> {
                        transactionDetailTable.edit(currentIndex, sellingPriceColumn);
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        sellingPriceColumn.setEditable(true);
        sellingPriceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        sellingPriceColumn.setOnEditCommit(event -> {
            try {
                Transactiondetail detail = event.getRowValue();
                if (detail == null) return;

                detail.setSellingPrice(event.getNewValue());
                recalcCumulativePrice(detail);
                recalcTotalAmount();

                // Refresh table
                transactionDetailTable.refresh();

                // Move to next row
                int currentIndex = transactiondetailList.indexOf(detail);
                if (currentIndex < transactiondetailList.size() - 1) {
                    Platform.runLater(() -> {
                        transactionDetailTable.getSelectionModel().select(currentIndex + 1);
                        transactionDetailTable.edit(currentIndex + 1, itemColumn);
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        comulativePriceColumn.setCellValueFactory(new PropertyValueFactory<>("comulativePrice"));
    }

    private void setupPaymentsTable() {
        pIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        pDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        pTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
        pWayColumn.setCellValueFactory(new PropertyValueFactory<>("paymentWay"));
    }

    /**
     * Preview Invoice (works for both saved and unsaved transactions)
     */
    @FXML
    public void onPreviewInvoice() {
        try {
            // Validate required data
            Person customer = customerComboBox.getValue();
            Employee employee = employeeComboBox.getValue();

            List<Transactiondetail> detailsToPreview = transactiondetailList.stream()
                    .filter(this::isRowFilled)
                    .collect(Collectors.toList());

            if (customer == null || employee == null || detailsToPreview.isEmpty()) {
                showAlert("Please fill in customer, employee, and at least one item before previewing.");
                return;
            }

            // Load FXML and controller
            Parent root = SpringFXMLLoader.load("/fxml/InvoicePreview.fxml");
            InvoicePreviewController controller =
                    SpringFXMLLoader.loadController(InvoicePreviewController.class);

            // Check if transaction is saved or unsaved
            if (currentTransaction != null && currentTransaction.getId() != null && currentTransaction.getId() > 0) {
                // Saved transaction - use normal database loading
                controller.setTransaction(currentTransaction);
            } else {
                // Unsaved transaction - build temporary data and use direct rendering
                Transaction tempTx = buildTemporaryTransaction();

                // Create copies of details and payments for preview
                List<Transactiondetail> previewDetails = new ArrayList<>(detailsToPreview);
                List<Payment> previewPayments = new ArrayList<>(paymentList);

                controller.setTransactionDirect(tempTx, previewDetails, previewPayments);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            String titleSuffix = (currentTransaction != null && currentTransaction.getId() != null && currentTransaction.getId() > 0)
                    ? " - Transaction " + currentTransaction.getId()
                    : " - PREVIEW (Unsaved)";
            stage.setTitle("Invoice Preview" + titleSuffix);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error opening invoice preview: " + ex.getMessage());
        }
    }

    /**
     * FIX #1: Build a temporary transaction object for preview purposes
     */
    private Transaction buildTemporaryTransaction() {
        Transaction tx = new Transaction();
        tx.setId(0); // Temporary ID
        tx.setTransactionDate(Instant.now());
        tx.setPerson(customerComboBox.getValue());
        tx.setSalesRep(employeeComboBox.getValue());
        tx.setTransactionType(transactionTypeComboBox.getValue() != null
                ? transactionTypeComboBox.getValue()
                : TransactionType.INVOICE);

        String note = transactionNoteField.getText();
        if (note != null && !note.trim().isEmpty()) {
            tx.setNote(note);
        }

        // Calculate total
        List<Transactiondetail> detailsToPreview = transactiondetailList.stream()
                .filter(this::isRowFilled)
                .collect(Collectors.toList());

        BigDecimal itemsTotal = detailsToPreview.stream()
                .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFees = calculateTotalWithdrawalFees();
        BigDecimal grandTotal = itemsTotal.add(totalFees);

        tx.setTotalAmount(grandTotal);

        // Attach details (with temporary IDs)
        for (int i = 0; i < detailsToPreview.size(); i++) {
            Transactiondetail detail = detailsToPreview.get(i);
            TransactiondetailId detailId = new TransactiondetailId(i + 1, 0);
            detail.setId(detailId);
            detail.setTransaction(tx);
        }

        // Attach payments
        for (Payment payment : paymentList) {
            payment.setTransaction(tx);
        }

        return tx;
    }

    /**
     * NEW METHOD: Print Invoice directly from Sales Form
     */
    @FXML
    public void onPrintInvoice() {
        try {
            if (currentTransaction == null) {
                showAlert("Please save or select a transaction before printing.");
                return;
            }

            // Load invoice data via service (to avoid lazy loading issues)
            InvoiceService.InvoiceData invoiceData = invoiceService.prepareInvoice(currentTransaction.getId());

            // Load the invoice preview FXML
            Parent root = SpringFXMLLoader.load("/fxml/InvoicePreview.fxml");
            InvoicePreviewController controller = SpringFXMLLoader.loadController(InvoicePreviewController.class);
            controller.setTransaction(invoiceData.getTransaction());

            // Create an invisible stage for rendering
            Stage hiddenStage = new Stage();
            hiddenStage.setOpacity(0); // Make it invisible
            Scene scene = new Scene(root);
            hiddenStage.setScene(scene);
            hiddenStage.show();

            // Allow layout to complete before printing
            Platform.runLater(() -> {
                try {
                    PrinterJob job = PrinterJob.createPrinterJob();
                    if (job != null) {
                        boolean proceed = job.showPrintDialog(hiddenStage);

                        if (proceed) {
                            Node printNode = scene.getRoot();

                            // Scale to fit page
                            double scaleX = job.getJobSettings().getPageLayout().getPrintableWidth() / printNode.getBoundsInParent().getWidth();
                            double scaleY = job.getJobSettings().getPageLayout().getPrintableHeight() / printNode.getBoundsInParent().getHeight();
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
                                showAlert("تم إرسال الفاتورة إلى الطابعة بنجاح");
                            } else {
                                showAlert("فشلت عملية الطباعة");
                            }
                        }
                    } else {
                        showAlert("لم يتم العثور على طابعة متاحة");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("خطأ في الطباعة: " + e.getMessage());
                } finally {
                    hiddenStage.close();
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error printing invoice: " + ex.getMessage());
        }
    }

    /**
     * NEW METHOD: Create a new blank transaction
     */
    @FXML
    public void onNewTransaction() {
        // Check if there's unsaved work
        boolean hasUnsavedWork = transactiondetailList.stream().anyMatch(this::isRowFilled);

        if (hasUnsavedWork && currentTransaction == null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Unsaved Changes");
            confirmAlert.setHeaderText("You have unsaved transaction data");
            confirmAlert.setContentText("Are you sure you want to create a new transaction? All current data will be lost.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return; // User cancelled
            }
        }

        // Reset the form
        resetForm();

        // Show confirmation
        showAlert("New transaction created. You can now enter transaction details.");
    }

    private void addBlankDetailRow() {
        Transactiondetail blank = new Transactiondetail();
        blank.setQuantity(0.0);
        blank.setSellingPrice(BigDecimal.ZERO);
        blank.setComulativePrice(BigDecimal.ZERO);
        transactiondetailList.add(blank);
    }

    private boolean isRowFilled(Transactiondetail detail) {
        boolean quantitySet = detail.getQuantity() != null && detail.getQuantity() > 0;
        boolean priceSet = detail.getSellingPrice() != null && detail.getSellingPrice().compareTo(BigDecimal.ZERO) > 0;
        boolean itemSet = detail.getItem() != null;
        return quantitySet || priceSet || itemSet;
    }

    private void recalcCumulativePrice(Transactiondetail d) {
        if (d == null) return;
        if (d.getQuantity() != null && d.getSellingPrice() != null) {
            BigDecimal q = BigDecimal.valueOf(d.getQuantity());
            d.setComulativePrice(d.getSellingPrice().multiply(q));
        } else {
            d.setComulativePrice(BigDecimal.ZERO);
        }
    }

    private void recalcTotalAmount() {
        recalcTotalAmountWithFees();
    }

    @FXML
    public void addTransactionDetail() {
        try {
            Double qty = Double.parseDouble(tdQuantityField.getText());
            BigDecimal selling = new BigDecimal(tdSellingPriceField.getText());
            BigDecimal cum = selling.multiply(BigDecimal.valueOf(qty));
            Transactiondetail d = new Transactiondetail();
            d.setQuantity(qty);
            d.setSellingPrice(selling);
            d.setComulativePrice(cum);
            transactiondetailList.add(d);
            recalcTotalAmount();
            addBlankDetailRow();
        } catch (Exception ex) {
            showAlert("Invalid detail input: " + ex.getMessage());
        }
    }

    @FXML
    public void deleteTransactionDetail() {
        Transactiondetail sel = transactionDetailTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            transactiondetailList.remove(sel);
            recalcTotalAmount();
        } else {
            showAlert("Select a detail to delete.");
        }
    }

    @FXML
    public void addPayment() {
        try {
            BigDecimal amt = new BigDecimal(pAmountField.getText());
            if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert("Payment must be > 0");
                return;
            }

            String paymentWay = pWayComboBox.getValue();
            if (paymentWay == null || paymentWay.trim().isEmpty()) {
                showAlert("Please select payment way");
                return;
            }

            // Calculate fee if applicable
            calculateWithdrawalFee();
            BigDecimal feeAmount = new BigDecimal(pFeeField.getText());

            Payment p = new Payment();
            p.setAmount(amt);
            p.setPaymentDate(Instant.now());
            p.setPaymentType(PAYMENT_TYPE_DEBTOR); // Always Debtor for sales
            p.setPaymentWay(paymentWay);
            paymentList.add(p);

            // Store fee in map
            paymentFeesMap.put(p, feeAmount);

            // Update transaction note with fees
            updateTransactionNoteWithFees();

            // Recalculate total with fees
            recalcTotalAmount();

            // Clear fields
            pAmountField.clear();
            pWayComboBox.setValue("Cash - نقدي");
            pFeeField.setText("0.00");
            currentWithdrawalFee = BigDecimal.ZERO;
        } catch (Exception ex) {
            showAlert("Invalid payment input: " + ex.getMessage());
        }
    }

    @FXML
    public void deletePayment() {
        Payment sel = paymentTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            paymentList.remove(sel);
            paymentFeesMap.remove(sel); // Remove fee from map
            updateTransactionNoteWithFees(); // Update notes
            recalcTotalAmount(); // Recalculate total
        } else {
            showAlert("Select a payment to delete.");
        }
    }

    @FXML
    public void onSaveTransaction() {
        try {
            // Validation
            Person customer = customerComboBox.getValue();
            Employee employee = employeeComboBox.getValue();
            TransactionType transactionType = transactionTypeComboBox.getValue();

            if (customer == null) {
                showAlert("Choose a customer");
                return;
            }
            if (employee == null) {
                showAlert("Choose an employee");
                return;
            }
            if (transactionType == null) {
                showAlert("Select transaction type");
                return;
            }

            // Filter details (remove blanks)
            List<Transactiondetail> detailsToSave = transactiondetailList.stream()
                    .filter(this::isRowFilled)
                    .collect(Collectors.toList());
            if (detailsToSave.isEmpty()) {
                showAlert("Add at least one transaction detail");
                return;
            }

            // Build Transaction
            Transaction tx = new Transaction();
            tx.setTransactionDate(Instant.now());
            tx.setPerson(customer);
            tx.setSalesRep(employee);
            tx.setTransactionType(transactionType);

            // Set note if provided
            String note = transactionNoteField.getText();
            if (note != null && !note.trim().isEmpty()) {
                tx.setNote(note);
            }

            // Calculate items total
            BigDecimal itemsTotal = detailsToSave.stream()
                    .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate withdrawal fees
            BigDecimal totalFees = calculateTotalWithdrawalFees();

            // Calculate grand total
            BigDecimal grandTotal = itemsTotal.add(totalFees);

            // CRITICAL: Store both the total AND the fee separately
            tx.setWithdrawalFee(totalFees);  // ✅ NEW: Store fee separately
            tx.setTotalAmount(grandTotal);   // ✅ Store grand total


            // Save transaction with details and payments
            Transaction saved = transactionService.saveTransactionWithDetailsAndPayments(
                    tx,
                    detailsToSave,
                    paymentList
            );

            currentTransaction = saved;

            // Update the invoice label
            updateCurrentInvoiceLabel();

            // Update customer balance after saving
            updateCustomerBalance();

            // Confirmation message
            showAlert("Transaction saved successfully!\n\nTransaction ID: " + saved.getId() +
                    "\nTotal Amount: " + grandTotal.toPlainString() + " EGP" +
                    (totalFees.compareTo(BigDecimal.ZERO) > 0 ?
                            "\n(includes withdrawal fee: " + totalFees.toPlainString() + " EGP)" : ""));

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error saving transaction: " + ex.getMessage());
        }
    }

    private void resetForm() {
        transactiondetailList.clear();
        paymentList.clear();
        paymentFeesMap.clear(); // Clear fees map
        totalWithdrawalFees = BigDecimal.ZERO;
        addBlankDetailRow();
        recalcTotalAmount();
        customerComboBox.setValue(null);
        employeeComboBox.setValue(null);
        transactionTypeComboBox.setValue(TransactionType.INVOICE);
        pWayComboBox.setValue("Cash - نقدي");
        transactionNoteField.clear();
        pFeeField.setText("0.00");
        customerBalanceField.setText("0.00"); // FIX #4: Reset balance field
        initializeTransactionNumber();
        currentTransaction = null;
        updateCurrentInvoiceLabel();
    }

    @FXML
    public void onFirstTransaction() {
        try {
            // Load the first transaction (ID = 1 or minimum ID)
            List<Transaction> allTransactions = transactionService.findAllTransactions();
            if (allTransactions.isEmpty()) {
                showAlert("No transactions found in database");
                return;
            }

            // Get first transaction (lowest ID)
            Transaction firstTx = allTransactions.stream()
                    .min((t1, t2) -> t1.getId().compareTo(t2.getId()))
                    .orElse(null);

            if (firstTx != null) {
                loadTransaction(firstTx.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading first transaction: " + e.getMessage());
        }
    }

    @FXML
    public void onPreviousTransaction() {
        try {
            if (currentTransaction == null || currentTransaction.getId() == null) {
                showAlert("No current transaction selected");
                return;
            }

            // Find previous transaction (ID < current ID)
            Optional<Transaction> previousTx = transactionService.findPreviousTransaction(currentTransaction.getId());

            if (previousTx.isPresent()) {
                loadTransaction(previousTx.get().getId());
            } else {
                showAlert("This is the first transaction");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading previous transaction: " + e.getMessage());
        }
    }

    @FXML
    public void onNextTransaction() {
        try {
            if (currentTransaction == null || currentTransaction.getId() == null) {
                showAlert("No current transaction selected");
                return;
            }

            // Find next transaction (ID > current ID)
            List<Transaction> allTransactions = transactionService.findAllTransactions();
            Transaction nextTx = allTransactions.stream()
                    .filter(t -> t.getId() > currentTransaction.getId())
                    .min((t1, t2) -> t1.getId().compareTo(t2.getId()))
                    .orElse(null);

            if (nextTx != null) {
                loadTransaction(nextTx.getId());
            } else {
                showAlert("This is the last transaction");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading next transaction: " + e.getMessage());
        }
    }

    @FXML
    public void onLastTransaction() {
        try {
            // Load the last transaction (maximum ID)
            List<Transaction> allTransactions = transactionService.findAllTransactions();
            if (allTransactions.isEmpty()) {
                showAlert("No transactions found in database");
                return;
            }

            // Get last transaction (highest ID)
            Transaction lastTx = allTransactions.stream()
                    .max((t1, t2) -> t1.getId().compareTo(t2.getId()))
                    .orElse(null);

            if (lastTx != null) {
                loadTransaction(lastTx.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading last transaction: " + e.getMessage());
        }
    }

private void loadTransaction(Integer transactionId) {

    try {

        // Load transaction data through InvoiceService (handles lazy loading)
        InvoiceService.InvoiceData data =
                SpringFXMLLoader.loadController(InvoiceService.class)
                        .prepareInvoice(transactionId);

        currentTransaction = data.getTransaction();

        // Populate form fields
        customerComboBox.setValue(currentTransaction.getPerson());
        employeeComboBox.setValue(currentTransaction.getSalesRep());
        transactionTypeComboBox.setValue(currentTransaction.getTransactionType());

        if (currentTransaction.getTotalAmount() != null) {
            totalAmountField.setText(currentTransaction.getTotalAmount().toPlainString());
        } else {
            totalAmountField.clear();
        }

        // Set note if exists
        if (currentTransaction.getNote() != null) {
            transactionNoteField.setText(currentTransaction.getNote());
        } else {
            transactionNoteField.clear();
        }

        // =========================================================
        // CRITICAL FIX: Restore withdrawal fees from database
        // =========================================================
        paymentFeesMap.clear();

        if (currentTransaction.getWithdrawalFee() != null) {
            totalWithdrawalFees = currentTransaction.getWithdrawalFee();
        } else {
            totalWithdrawalFees = BigDecimal.ZERO;
        }
        // =========================================================


        // Load transaction details
        transactiondetailList.clear();
        transactiondetailList.addAll(data.getDetails());
        addBlankDetailRow();


        // =========================================================
        // Load payments
        // =========================================================
        paymentList.clear();
        paymentList.addAll(data.getPayments());


        // =========================================================
        // CRITICAL FIX: Rebuild fee association with payments
        // =========================================================
        if (totalWithdrawalFees.compareTo(BigDecimal.ZERO) > 0) {

            List<Payment> paymentsWithFees = paymentList.stream()
                    .filter(p ->
                            VODAFONE_CASH.equals(p.getPaymentWay()) ||
                                    ETISALAT_CASH.equals(p.getPaymentWay()))
                    .toList();

            if (!paymentsWithFees.isEmpty()) {

                BigDecimal totalPaymentsAmount = paymentsWithFees.stream()
                        .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalPaymentsAmount.compareTo(BigDecimal.ZERO) > 0) {

                    for (Payment payment : paymentsWithFees) {

                        BigDecimal paymentAmount =
                                payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;

                        BigDecimal proportion = paymentAmount.divide(
                                totalPaymentsAmount, 6, RoundingMode.HALF_UP);

                        BigDecimal paymentFee = totalWithdrawalFees
                                .multiply(proportion)
                                .setScale(2, RoundingMode.HALF_UP);

                        paymentFeesMap.put(payment, paymentFee);
                    }
                }
            }
        }
        // =========================================================


        // Update transaction note with restored fees
        updateTransactionNoteWithFees();

        // Update invoice label
        updateCurrentInvoiceLabel();

        // Update customer balance
        updateCustomerBalance();

        // Refresh tables
        transactionDetailTable.refresh();
        paymentTable.refresh();

    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Error loading transaction: " + e.getMessage());
    }
}

    /**
     * Restore payment fee associations after loading transaction
     * Distributes the total withdrawal fee across payments that have fees
     */
    private void restorePaymentFeeAssociations() {
        if (totalWithdrawalFees.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // Find payments that typically have fees (Vodafone Cash, Etisalat Cash)
        List<Payment> paymentsWithFees = paymentList.stream()
                .filter(p -> VODAFONE_CASH.equals(p.getPaymentWay()) ||
                        ETISALAT_CASH.equals(p.getPaymentWay()))
                .collect(Collectors.toList());

        if (paymentsWithFees.isEmpty()) {
            return;
        }

        // Calculate fee for each payment based on its amount
        BigDecimal totalPaymentAmount = paymentsWithFees.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPaymentAmount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        // Distribute fees proportionally
        for (Payment payment : paymentsWithFees) {
            BigDecimal paymentAmount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
            BigDecimal proportion = paymentAmount.divide(totalPaymentAmount, 4, BigDecimal.ROUND_HALF_UP);
            BigDecimal paymentFee = totalWithdrawalFees.multiply(proportion)
                    .setScale(2, BigDecimal.ROUND_HALF_UP);
            paymentFeesMap.put(payment, paymentFee);
        }
    }

    /**
     * FIX #3: Parse withdrawal fees from transaction note
     */
    private void parseWithdrawalFeesFromNote(String note) {
        paymentFeesMap.clear();
        totalWithdrawalFees = BigDecimal.ZERO;

        if (note == null || !note.contains("Withdrawal fee")) {
            return;
        }

        // Parse format: "Withdrawal fee (Payment Way): XX.XX EGP"
        String[] parts = note.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("Withdrawal fee")) {
                try {
                    // Extract fee amount
                    int startIdx = part.lastIndexOf(":") + 1;
                    int endIdx = part.lastIndexOf("EGP");
                    if (startIdx > 0 && endIdx > startIdx) {
                        String feeStr = part.substring(startIdx, endIdx).trim();
                        BigDecimal fee = new BigDecimal(feeStr);
                        totalWithdrawalFees = totalWithdrawalFees.add(fee);
                    }
                } catch (Exception e) {
                    // Ignore parsing errors
                }
            }
        }
    }

    /**
     * FIX #3: Associate parsed fees with loaded payments
     * This is a simplified version - fees are restored to total
     */
    private void associateFeesWithPayments() {
        // Since we can't perfectly match fees to payments after loading,
        // we just ensure the total includes the fees
        // The totalWithdrawalFees variable now contains the total fees
    }
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sales");
        a.setContentText(msg);
        a.showAndWait();
    }
}