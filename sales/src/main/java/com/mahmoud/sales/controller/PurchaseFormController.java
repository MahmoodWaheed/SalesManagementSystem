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
public class PurchaseFormController {

    // Services
    private PurchasetransactionService purchasetransactionService;
    private PurchasedetailService purchasedetailService;
    private PaymentService paymentService;
    private ItemService itemService;
    private PersonService personService;
    private EmployeeService employeeService;
    private PurchaseInvoiceService purchaseInvoiceService;
    private Purchasetransaction currentPurchaseTransaction;

    @FXML private ComboBox<Person> supplierComboBox;
    @FXML private ComboBox<Employee> employeeComboBox;
    @FXML private ComboBox<PurchaseTransactionType> purchaseTypeComboBox; // NEW
    @FXML private ComboBox<String> pWayComboBox;
    @FXML private TextField purchaseNumberField;
    @FXML private TextField purchaseNoteField;
    @FXML private TextField fatoraNumberField;
    @FXML private Label currentPurchaseLabel;
    @FXML private TextField totalAmountField;
    @FXML private TextField pFeeField;
    @FXML private TextField supplierBalanceField;

    @FXML private Button savePurchaseButton;
    @FXML private Button newPurchaseButton;
    @FXML private Button printInvoiceButton;
    @FXML private Button previewInvoiceButton;
    @FXML private Button firstPurchaseButton;
    @FXML private Button previousPurchaseButton;
    @FXML private Button nextPurchaseButton;
    @FXML private Button lastPurchaseButton;

    @FXML private TableView<Purchasedetail> purchaseDetailTable;
    @FXML private TableColumn<Purchasedetail, Integer> pdIdColumn;
    @FXML private TableColumn<Purchasedetail, Integer> itemIdColumn;
    @FXML private TableColumn<Purchasedetail, Item> itemColumn;
    @FXML private TableColumn<Purchasedetail, Double> quantityColumn;
    @FXML private TableColumn<Purchasedetail, BigDecimal> purchasingPriceColumn;
    @FXML private TableColumn<Purchasedetail, BigDecimal> comulativePriceColumn;

    @FXML private TextField pdQuantityField;
    @FXML private TextField pdPurchasingPriceField;
    @FXML private TextField pdComulativePriceField;
    @FXML private Button addPurchaseDetailButton;
    @FXML private Button deletePurchaseDetailButton;

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

    // Payment type for purchases (always Creditor for Purchase)
    private static final String PAYMENT_TYPE_CREDITOR = "Creditor - دائن";

    private BigDecimal currentWithdrawalFee = BigDecimal.ZERO;
    private Map<Payment, BigDecimal> paymentFeesMap = new HashMap<>();
    private BigDecimal totalWithdrawalFees = BigDecimal.ZERO;

    private ObservableList<Purchasedetail> purchasedetailList;
    private ObservableList<Payment> paymentList;
    private ObservableList<Person> masterSupplierList;
    private ObservableList<Item> itemsList;
    private ObservableList<Employee> employeesList;

    @Autowired
    private SpringFXMLLoader springFXMLLoader;

    @FXML
    public void initialize() {
        // Wire services
        this.paymentService = SpringFXMLLoader.loadController(PaymentService.class);
        this.purchasedetailService = SpringFXMLLoader.loadController(PurchasedetailService.class);
        this.purchasetransactionService = SpringFXMLLoader.loadController(PurchasetransactionService.class);
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
        this.employeeService = SpringFXMLLoader.loadController(EmployeeService.class);
        this.itemService = SpringFXMLLoader.loadController(ItemService.class);
        this.purchaseInvoiceService = SpringFXMLLoader.loadController(PurchaseInvoiceService.class);

        // Initialize purchase type ComboBox
        initializePurchaseTypeComboBox();

        // Initialize payment way ComboBox
        initializePaymentWayComboBox();

        // Initialize supplier ComboBox with improved autocomplete
        initializeSupplierComboBox();

        // Initialize employee ComboBox
        initializeEmployeeComboBox();

        // Initialize items list
        itemsList = FXCollections.observableArrayList(itemService.findAllItems());

        // Setup purchase detail table
        setupPurchaseDetailTable();

        // Setup payments table
        setupPaymentsTable();

        // Initialize purchase number and current purchase label
        initializePurchaseNumber();
        updateCurrentPurchaseLabel();

        // Initialize lists
        purchasedetailList = FXCollections.observableArrayList();
        purchaseDetailTable.setItems(purchasedetailList);
        addBlankDetailRow();

        paymentList = FXCollections.observableArrayList();
        paymentTable.setItems(paymentList);

        // Add listener to payment amount field for fee calculation
        pAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            calculateWithdrawalFee();
        });

        // Keyboard handling for table - TAB to move between cells
        purchaseDetailTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();

                TablePosition<Purchasedetail, ?> pos = purchaseDetailTable.getFocusModel().getFocusedCell();
                if (pos != null) {
                    int row = pos.getRow();
                    int col = pos.getColumn();

                    // Move to next column or next row
                    if (col < purchaseDetailTable.getColumns().size() - 1) {
                        // Move to next column in same row
                        purchaseDetailTable.edit(row, purchaseDetailTable.getColumns().get(col + 1));
                    } else {
                        // Move to first editable column of next row
                        if (row < purchasedetailList.size() - 1) {
                            purchaseDetailTable.getSelectionModel().select(row + 1);
                            purchaseDetailTable.edit(row + 1, itemColumn);
                        } else {
                            // Last row - add new blank row
                            addBlankDetailRow();
                            purchaseDetailTable.getSelectionModel().selectLast();
                            purchaseDetailTable.edit(purchasedetailList.size() - 1, itemColumn);
                        }
                    }
                }
            }
        });
    }

    /**
     * NEW: Initialize purchase type ComboBox with INVOICE and RETURN options
     */
    private void initializePurchaseTypeComboBox() {
        // Populate with PurchaseTransactionType enum values
        ObservableList<PurchaseTransactionType> purchaseTypes = FXCollections.observableArrayList(
                PurchaseTransactionType.INVOICE,
                PurchaseTransactionType.RETURN
        );
        purchaseTypeComboBox.setItems(purchaseTypes);

        // Set default to INVOICE
        purchaseTypeComboBox.setValue(PurchaseTransactionType.INVOICE);

        // Custom string converter to display Arabic values
        purchaseTypeComboBox.setConverter(new StringConverter<PurchaseTransactionType>() {
            @Override
            public String toString(PurchaseTransactionType type) {
                return type == null ? "" : type.getArabicValue() + " (" + type.name() + ")";
            }

            @Override
            public PurchaseTransactionType fromString(String string) {
                return purchaseTypeComboBox.getItems().stream()
                        .filter(type -> (type.getArabicValue() + " (" + type.name() + ")").equals(string))
                        .findFirst()
                        .orElse(PurchaseTransactionType.INVOICE);
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
     * Update purchase note with all withdrawal fees
     */
    private void updatePurchaseNoteWithFees() {
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

        // Set or update the purchase note
        if (feeNotes.length() > 0) {
            String currentNote = purchaseNoteField.getText();

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
                purchaseNoteField.setText(currentNote + "; " + feeNotes.toString());
            } else {
                purchaseNoteField.setText(feeNotes.toString());
            }
        }
    }

    /**
     * Recalculate total amount including items and withdrawal fees
     */
    private void recalcTotalAmountWithFees() {
        // Calculate items total
        BigDecimal itemsTotal = purchasedetailList.stream()
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

        // Update supplier balance
        updateSupplierBalance();
    }

    /**
     * Calculate and update supplier current balance
     * Formula: Opening Balance + Total Purchases - Total Payments
     */
    private void updateSupplierBalance() {
        Person selectedSupplier = supplierComboBox.getValue();
        if (selectedSupplier == null) {
            supplierBalanceField.setText("0.00");
            return;
        }

        try {
            // Get opening balance
            BigDecimal openingBalance = selectedSupplier.getOpenBalance();
            if (openingBalance == null) {
                openingBalance = BigDecimal.ZERO;
            }

            // Get total purchases for this supplier from database
            BigDecimal totalPurchases = personService.calculateRemainingBalance(selectedSupplier.getId());
            if (totalPurchases == null) {
                totalPurchases = BigDecimal.ZERO;
            }

            // Add current unsaved purchase total (if exists)
            String currentTotalText = totalAmountField.getText();
            BigDecimal currentPurchaseTotal = BigDecimal.ZERO;
            if (currentTotalText != null && !currentTotalText.trim().isEmpty()) {
                try {
                    currentPurchaseTotal = new BigDecimal(currentTotalText);
                } catch (NumberFormatException e) {
                    // Ignore if invalid
                }
            }

            // Calculate total payments from payment table
            BigDecimal currentPayments = paymentList.stream()
                    .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Formula: Opening Balance + Total DB Purchases + Current Purchase - Current Payments
            BigDecimal currentBalance = openingBalance
                    .add(totalPurchases)
                    .add(currentPurchaseTotal)
                    .subtract(currentPayments);

            supplierBalanceField.setText(currentBalance.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());

        } catch (Exception e) {
            e.printStackTrace();
            supplierBalanceField.setText("Error");
        }
    }

    private void initializeSupplierComboBox() {
        // Load all suppliers
        List<Person> suppliers = personService.findByType("Supplier");
        masterSupplierList = FXCollections.observableArrayList(suppliers);

        // Set items directly to ComboBox (no filtering at this stage)
        supplierComboBox.setItems(masterSupplierList);
        supplierComboBox.setEditable(true);

        // String converter for display
        supplierComboBox.setConverter(new StringConverter<Person>() {
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
                return masterSupplierList.stream()
                        .filter(p -> p.getName().equalsIgnoreCase(string.trim()))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Get the editor of the ComboBox
        TextField editor = supplierComboBox.getEditor();

        // Flag to prevent recursive updates
        final boolean[] isUpdating = {false};

        // Add listener for autocomplete
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating[0]) return;

            isUpdating[0] = true;
            try {
                if (newVal == null || newVal.trim().isEmpty()) {
                    // Show all suppliers when empty
                    supplierComboBox.setItems(masterSupplierList);
                    supplierComboBox.hide();
                } else {
                    final String filterText = newVal.trim().toLowerCase();

                    // Filter suppliers
                    ObservableList<Person> filtered = masterSupplierList.stream()
                            .filter(person -> person.getName().toLowerCase().contains(filterText))
                            .collect(Collectors.toCollection(FXCollections::observableArrayList));

                    supplierComboBox.setItems(filtered);

                    // Show dropdown if there are matches and user is typing
                    if (!filtered.isEmpty()) {
                        if (!supplierComboBox.isShowing()) {
                            supplierComboBox.show();
                        }
                    } else {
                        supplierComboBox.hide();
                    }
                }
            } finally {
                isUpdating[0] = false;
            }
        });

        // Handle keyboard events
        editor.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DOWN) {
                if (!supplierComboBox.isShowing()) {
                    supplierComboBox.show();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                // If only one item in list, select it
                if (supplierComboBox.getItems().size() == 1) {
                    supplierComboBox.setValue(supplierComboBox.getItems().get(0));
                    supplierComboBox.hide();
                    event.consume();
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                supplierComboBox.hide();
                event.consume();
            }
        });

        // When user selects from dropdown with mouse
        supplierComboBox.setOnAction(event -> {
            Person selected = supplierComboBox.getValue();
            if (selected != null) {
                editor.setText(selected.getName());
                updateSupplierBalance();
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

    private void initializePurchaseNumber() {
        try {
            // Get next purchase ID
            Integer nextId = purchasetransactionService.findAllPurchasetransactions().size() + 1;
            purchaseNumberField.setText(String.valueOf(nextId));
        } catch (Exception e) {
            purchaseNumberField.setText("AUTO");
        }
    }

    private void updateCurrentPurchaseLabel() {
        if (currentPurchaseTransaction != null && currentPurchaseTransaction.getId() != null) {
            currentPurchaseLabel.setText("#" + currentPurchaseTransaction.getId());
            currentPurchaseLabel.setStyle("-fx-text-fill: #805ad5; -fx-background-color: #faf5ff;");
        } else {
            currentPurchaseLabel.setText("NEW");
            currentPurchaseLabel.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fee;");
        }
    }

    private void setupPurchaseDetailTable() {
        // Make table editable
        purchaseDetailTable.setEditable(true);

        pdIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
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
            ComboBoxTableCell<Purchasedetail, Item> cell = new ComboBoxTableCell<>(
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
                Purchasedetail d = e.getRowValue();
                if (d == null) return;

                Item selectedItem = e.getNewValue();
                d.setItem(selectedItem);

                // Auto-fill purchasing price from item
                if (selectedItem != null && selectedItem.getPurchasingPrice() != null) {
                    d.setPurchasingPrice(selectedItem.getPurchasingPrice());
                }

                // Set default quantity if not set
                if (d.getQuantity() == null || d.getQuantity() == 0.0) {
                    d.setQuantity(1.0);
                }

                recalcCumulativePrice(d);
                recalcTotalAmount();

                // Refresh table first
                purchaseDetailTable.refresh();

                // Add blank row if this was the last row
                int currentIndex = purchasedetailList.indexOf(d);
                if (currentIndex == purchasedetailList.size() - 1) {
                    Platform.runLater(() -> {
                        addBlankDetailRow();
                        purchaseDetailTable.refresh();
                    });
                }

                // Move to quantity column after a slight delay
                Platform.runLater(() -> {
                    int rowIndex = purchasedetailList.indexOf(d);
                    if (rowIndex >= 0) {
                        purchaseDetailTable.getSelectionModel().select(rowIndex);
                        purchaseDetailTable.edit(rowIndex, quantityColumn);
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
                Purchasedetail detail = event.getRowValue();
                if (detail == null) return;

                detail.setQuantity(event.getNewValue());
                recalcCumulativePrice(detail);
                recalcTotalAmount();

                // Refresh table
                purchaseDetailTable.refresh();

                // Move to next row or add new row
                int currentIndex = purchasedetailList.indexOf(detail);
                if (currentIndex == purchasedetailList.size() - 1 && isRowFilled(detail)) {
                    Platform.runLater(() -> {
                        addBlankDetailRow();
                        purchaseDetailTable.getSelectionModel().select(currentIndex + 1);
                        purchaseDetailTable.edit(currentIndex + 1, itemColumn);
                    });
                } else if (currentIndex < purchasedetailList.size() - 1) {
                    Platform.runLater(() -> {
                        purchaseDetailTable.edit(currentIndex, purchasingPriceColumn);
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        purchasingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasingPrice"));
        purchasingPriceColumn.setEditable(true);
        purchasingPriceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        purchasingPriceColumn.setOnEditCommit(event -> {
            try {
                Purchasedetail detail = event.getRowValue();
                if (detail == null) return;

                detail.setPurchasingPrice(event.getNewValue());
                recalcCumulativePrice(detail);
                recalcTotalAmount();

                // Refresh table
                purchaseDetailTable.refresh();

                // Move to next row
                int currentIndex = purchasedetailList.indexOf(detail);
                if (currentIndex < purchasedetailList.size() - 1) {
                    Platform.runLater(() -> {
                        purchaseDetailTable.getSelectionModel().select(currentIndex + 1);
                        purchaseDetailTable.edit(currentIndex + 1, itemColumn);
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
     * Preview Invoice (works for both saved and unsaved purchase transactions)
     */
    @FXML
    public void onPreviewInvoice() {
        try {
            // Validate required data
            Person supplier = supplierComboBox.getValue();
            Employee employee = employeeComboBox.getValue();

            List<Purchasedetail> detailsToPreview = purchasedetailList.stream()
                    .filter(this::isRowFilled)
                    .collect(Collectors.toList());

            if (supplier == null || employee == null || detailsToPreview.isEmpty()) {
                showAlert("Please fill in supplier, employee, and at least one item before previewing.");
                return;
            }

            // Load FXML and controller
            Parent root = SpringFXMLLoader.load("/fxml/Purchaseinvoicepreview.fxml");
            PurchaseInvoicePreviewController controller =
                    SpringFXMLLoader.loadController(PurchaseInvoicePreviewController.class);

            // Check if transaction is saved or unsaved
            if (currentPurchaseTransaction != null && currentPurchaseTransaction.getId() != null && currentPurchaseTransaction.getId() > 0) {
                // Saved transaction - use normal database loading
                controller.setPurchaseTransaction(currentPurchaseTransaction);
            } else {
                // Unsaved transaction - build temporary data and use direct rendering
                Purchasetransaction tempTx = buildTemporaryPurchaseTransaction();

                // Create copies of details and payments for preview
                List<Purchasedetail> previewDetails = new ArrayList<>(detailsToPreview);
                List<Payment> previewPayments = new ArrayList<>(paymentList);

                controller.setPurchaseTransactionDirect(tempTx, previewDetails, previewPayments);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            String titleSuffix = (currentPurchaseTransaction != null && currentPurchaseTransaction.getId() != null && currentPurchaseTransaction.getId() > 0)
                    ? " - Purchase " + currentPurchaseTransaction.getId()
                    : " - PREVIEW (Unsaved)";
            stage.setTitle("Purchase Invoice Preview" + titleSuffix);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error opening invoice preview: " + ex.getMessage());
        }
    }

    /**
     * Build a temporary purchase transaction object for preview purposes
     */
    private Purchasetransaction buildTemporaryPurchaseTransaction() {
        Purchasetransaction tx = new Purchasetransaction();
        tx.setId(0); // Temporary ID
        tx.setPurchaseDate(Instant.now());
        tx.setPerson(supplierComboBox.getValue());
        tx.setTransactionType(purchaseTypeComboBox.getValue() != null
                ? purchaseTypeComboBox.getValue()
                : PurchaseTransactionType.INVOICE);

        String note = purchaseNoteField.getText();
        if (note != null && !note.trim().isEmpty()) {
            tx.setNotes(note);
        }

        String fatoraNum = fatoraNumberField.getText();
        if (fatoraNum != null && !fatoraNum.trim().isEmpty()) {
            try {
                tx.setFatoraNumber(Integer.parseInt(fatoraNum));
            } catch (NumberFormatException e) {
                // Ignore if invalid
            }
        }

        // Calculate total
        List<Purchasedetail> detailsToPreview = purchasedetailList.stream()
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
            Purchasedetail detail = detailsToPreview.get(i);
            PurchasedetailId detailId = new PurchasedetailId();
            detailId.setId(i + 1);
            detailId.setPurchasetransactionId(0);
            detail.setId(detailId);
            detail.setPurchaseTransaction(tx);
        }

        // Attach payments
        for (Payment payment : paymentList) {
            payment.setPurchaseTransaction(tx);
        }

        return tx;
    }

    @FXML
    public void onPrintInvoice() {
        try {
            if (currentPurchaseTransaction == null) {
                showAlert("Please save or select a purchase transaction before printing.");
                return;
            }

            // Load invoice data via service (to avoid lazy loading issues)
            PurchaseInvoiceService.PurchaseInvoiceData invoiceData = purchaseInvoiceService.preparePurchaseInvoice(currentPurchaseTransaction.getId());

            // Load the invoice preview FXML
            Parent root = SpringFXMLLoader.load("/fxml/Purchaseinvoicepreview.fxml");
            PurchaseInvoicePreviewController controller = SpringFXMLLoader.loadController(PurchaseInvoicePreviewController.class);
            controller.setPurchaseTransaction(invoiceData.getPurchaseTransaction());

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
     * Create a new blank purchase transaction
     */
    @FXML
    public void onNewPurchase() {
        boolean hasUnsavedWork = purchasedetailList.stream().anyMatch(this::isRowFilled);

        if (hasUnsavedWork && currentPurchaseTransaction == null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Unsaved Changes");
            confirmAlert.setHeaderText("You have unsaved purchase data");
            confirmAlert.setContentText("Are you sure you want to create a new purchase? All current data will be lost.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return; // User cancelled
            }
        }

        // Reset the form
        resetForm();

        // Show confirmation
        showAlert("New purchase created. You can now enter purchase details.");
    }

    private void addBlankDetailRow() {
        Purchasedetail blank = new Purchasedetail();
        blank.setQuantity(0.0);
        blank.setPurchasingPrice(BigDecimal.ZERO);
        blank.setComulativePrice(BigDecimal.ZERO);
        purchasedetailList.add(blank);
    }

    private boolean isRowFilled(Purchasedetail detail) {
        boolean quantitySet = detail.getQuantity() != null && detail.getQuantity() > 0;
        boolean priceSet = detail.getPurchasingPrice() != null && detail.getPurchasingPrice().compareTo(BigDecimal.ZERO) > 0;
        boolean itemSet = detail.getItem() != null;
        return quantitySet || priceSet || itemSet;
    }

    private void recalcCumulativePrice(Purchasedetail d) {
        if (d == null) return;
        if (d.getQuantity() != null && d.getPurchasingPrice() != null) {
            BigDecimal q = BigDecimal.valueOf(d.getQuantity());
            d.setComulativePrice(d.getPurchasingPrice().multiply(q));
        } else {
            d.setComulativePrice(BigDecimal.ZERO);
        }
    }

    private void recalcTotalAmount() {
        recalcTotalAmountWithFees();
    }

    @FXML
    public void addPurchaseDetail() {
        try {
            Double qty = Double.parseDouble(pdQuantityField.getText());
            BigDecimal purchasing = new BigDecimal(pdPurchasingPriceField.getText());
            BigDecimal cum = purchasing.multiply(BigDecimal.valueOf(qty));
            Purchasedetail d = new Purchasedetail();
            d.setQuantity(qty);
            d.setPurchasingPrice(purchasing);
            d.setComulativePrice(cum);
            purchasedetailList.add(d);
            recalcTotalAmount();
            addBlankDetailRow();
        } catch (Exception ex) {
            showAlert("Invalid detail input: " + ex.getMessage());
        }
    }

    @FXML
    public void deletePurchaseDetail() {
        Purchasedetail sel = purchaseDetailTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            purchasedetailList.remove(sel);
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
            p.setPaymentType(PAYMENT_TYPE_CREDITOR); // Always Creditor for purchases
            p.setPaymentWay(paymentWay);
            paymentList.add(p);

            // Store fee in map
            paymentFeesMap.put(p, feeAmount);

            // Update purchase note with fees
            updatePurchaseNoteWithFees();

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
            updatePurchaseNoteWithFees(); // Update notes
            recalcTotalAmount(); // Recalculate total
        } else {
            showAlert("Select a payment to delete.");
        }
    }

    @FXML
    public void onSavePurchase() {
        try {
            // Validation
            Person supplier = supplierComboBox.getValue();
            Employee employee = employeeComboBox.getValue();
            PurchaseTransactionType purchaseType = purchaseTypeComboBox.getValue();

            if (supplier == null) {
                showAlert("Choose a supplier");
                return;
            }
            if (employee == null) {
                showAlert("Choose an employee");
                return;
            }
            if (purchaseType == null) {
                showAlert("Select purchase type");
                return;
            }

            // Filter details (remove blanks)
            List<Purchasedetail> detailsToSave = purchasedetailList.stream()
                    .filter(this::isRowFilled)
                    .collect(Collectors.toList());
            if (detailsToSave.isEmpty()) {
                showAlert("Add at least one purchase detail");
                return;
            }

            // Build Purchase Transaction
            Purchasetransaction pt = new Purchasetransaction();
            pt.setPurchaseDate(Instant.now());
            pt.setPerson(supplier);
            pt.setSalesRep(employee);
            pt.setTransactionType(purchaseType);

            // Set note if provided
            String note = purchaseNoteField.getText();
            if (note != null && !note.trim().isEmpty()) {
                pt.setNotes(note);
            }

            // Set fatora number if provided
            String fatoraNum = fatoraNumberField.getText();
            if (fatoraNum != null && !fatoraNum.trim().isEmpty()) {
                try {
                    pt.setFatoraNumber(Integer.parseInt(fatoraNum));
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }

            // Calculate items total
            BigDecimal itemsTotal = detailsToSave.stream()
                    .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calculate withdrawal fees
            BigDecimal totalFees = calculateTotalWithdrawalFees();

            // Calculate grand total
            BigDecimal grandTotal = itemsTotal.add(totalFees);

            // Store both the total AND the fee separately
            pt.setWithdrawalFee(totalFees);
            pt.setTotalAmount(grandTotal);

            // Save purchase transaction with details and payments
            Purchasetransaction saved = purchasetransactionService.savePurchaseWithDetailsAndPayments(
                    pt,
                    detailsToSave,
                    paymentList
            );

            currentPurchaseTransaction = saved;

            // Update the purchase label
            updateCurrentPurchaseLabel();

            // Update supplier balance after saving
            updateSupplierBalance();

            // Confirmation message
            showAlert("Purchase saved successfully!\n\nPurchase ID: " + saved.getId() +
                    "\nType: " + purchaseType.getArabicValue() +
                    "\nTotal Amount: " + grandTotal.toPlainString() + " EGP" +
                    (totalFees.compareTo(BigDecimal.ZERO) > 0 ?
                            "\n(includes withdrawal fee: " + totalFees.toPlainString() + " EGP)" : ""));

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error saving purchase: " + ex.getMessage());
        }
    }

    private void resetForm() {
        purchasedetailList.clear();
        paymentList.clear();
        paymentFeesMap.clear();
        totalWithdrawalFees = BigDecimal.ZERO;
        addBlankDetailRow();
        recalcTotalAmount();
        supplierComboBox.setValue(null);
        employeeComboBox.setValue(null);
        purchaseTypeComboBox.setValue(PurchaseTransactionType.INVOICE);
        pWayComboBox.setValue("Cash - نقدي");
        purchaseNoteField.clear();
        fatoraNumberField.clear();
        pFeeField.setText("0.00");
        supplierBalanceField.setText("0.00");
        initializePurchaseNumber();
        currentPurchaseTransaction = null;
        updateCurrentPurchaseLabel();
    }

    @FXML
    public void onFirstPurchase() {
        try {
            List<Purchasetransaction> allPurchases = purchasetransactionService.findAllPurchasetransactions();
            if (allPurchases.isEmpty()) {
                showAlert("No purchase transactions found in database");
                return;
            }

            Purchasetransaction firstPt = allPurchases.stream()
                    .min((t1, t2) -> t1.getId().compareTo(t2.getId()))
                    .orElse(null);

            if (firstPt != null) {
                loadPurchaseTransaction(firstPt.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading first purchase: " + e.getMessage());
        }
    }

    @FXML
    public void onPreviousPurchase() {
        try {
            if (currentPurchaseTransaction == null || currentPurchaseTransaction.getId() == null) {
                showAlert("No current purchase transaction selected");
                return;
            }

            List<Purchasetransaction> allPurchases = purchasetransactionService.findAllPurchasetransactions();
            Purchasetransaction previousPt = allPurchases.stream()
                    .filter(t -> t.getId() < currentPurchaseTransaction.getId())
                    .max((t1, t2) -> t1.getId().compareTo(t2.getId()))
                    .orElse(null);

            if (previousPt != null) {
                loadPurchaseTransaction(previousPt.getId());
            } else {
                showAlert("This is the first purchase transaction");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading previous purchase: " + e.getMessage());
        }
    }

    @FXML
    public void onNextPurchase() {
        try {
            if (currentPurchaseTransaction == null || currentPurchaseTransaction.getId() == null) {
                showAlert("No current purchase transaction selected");
                return;
            }

            List<Purchasetransaction> allPurchases = purchasetransactionService.findAllPurchasetransactions();
            Purchasetransaction nextPt = allPurchases.stream()
                    .filter(t -> t.getId() > currentPurchaseTransaction.getId())
                    .min((t1, t2) -> t1.getId().compareTo(t2.getId()))
                    .orElse(null);

            if (nextPt != null) {
                loadPurchaseTransaction(nextPt.getId());
            } else {
                showAlert("This is the last purchase transaction");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading next purchase: " + e.getMessage());
        }
    }

    @FXML
    public void onLastPurchase() {
        try {
            List<Purchasetransaction> allPurchases = purchasetransactionService.findAllPurchasetransactions();
            if (allPurchases.isEmpty()) {
                showAlert("No purchase transactions found in database");
                return;
            }

            Purchasetransaction lastPt = allPurchases.stream()
                    .max((t1, t2) -> t1.getId().compareTo(t2.getId()))
                    .orElse(null);

            if (lastPt != null) {
                loadPurchaseTransaction(lastPt.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading last purchase: " + e.getMessage());
        }
    }

    private void loadPurchaseTransaction(Integer purchaseTransactionId) {
        try {
            // Load purchase transaction data through PurchaseInvoiceService (handles lazy loading)
            PurchaseInvoiceService.PurchaseInvoiceData data =
                    SpringFXMLLoader.loadController(PurchaseInvoiceService.class)
                            .preparePurchaseInvoice(purchaseTransactionId);

            currentPurchaseTransaction = data.getPurchaseTransaction();

            // Populate form fields
            supplierComboBox.setValue(currentPurchaseTransaction.getPerson());
            employeeComboBox.setValue(currentPurchaseTransaction.getSalesRep());
            purchaseTypeComboBox.setValue(currentPurchaseTransaction.getTransactionType() != null
                    ? currentPurchaseTransaction.getTransactionType()
                    : PurchaseTransactionType.INVOICE);

            if (currentPurchaseTransaction.getTotalAmount() != null) {
                totalAmountField.setText(currentPurchaseTransaction.getTotalAmount().toPlainString());
            } else {
                totalAmountField.clear();
            }

            // Set note if exists
            if (currentPurchaseTransaction.getNotes() != null) {
                purchaseNoteField.setText(currentPurchaseTransaction.getNotes());
            } else {
                purchaseNoteField.clear();
            }

            // Set fatora number if exists
            if (currentPurchaseTransaction.getFatoraNumber() != null) {
                fatoraNumberField.setText(currentPurchaseTransaction.getFatoraNumber().toString());
            } else {
                fatoraNumberField.clear();
            }

            // Restore withdrawal fees from database
            paymentFeesMap.clear();

            if (currentPurchaseTransaction.getWithdrawalFee() != null) {
                totalWithdrawalFees = currentPurchaseTransaction.getWithdrawalFee();
            } else {
                totalWithdrawalFees = BigDecimal.ZERO;
            }

            // Load purchase details
            purchasedetailList.clear();
            purchasedetailList.addAll(data.getDetails());
            addBlankDetailRow();

            // Load payments
            paymentList.clear();
            paymentList.addAll(data.getPayments());

            // Rebuild fee association with payments
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

            // Update purchase note with restored fees
            updatePurchaseNoteWithFees();

            // Update purchase label
            updateCurrentPurchaseLabel();

            // Update supplier balance
            updateSupplierBalance();

            // Refresh tables
            purchaseDetailTable.refresh();
            paymentTable.refresh();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error loading purchase transaction: " + e.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Purchase");
        a.setContentText(msg);
        a.showAndWait();
    }
}
//package com.mahmoud.sales.controller;
//
//import com.mahmoud.sales.entity.*;
//import com.mahmoud.sales.service.*;
//import com.mahmoud.sales.util.SpringFXMLLoader;
//import javafx.application.Platform;
//import javafx.beans.property.ReadOnlyObjectWrapper;
//import javafx.beans.property.SimpleObjectProperty;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.print.PrinterJob;
//import javafx.scene.Node;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.ComboBoxTableCell;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.control.cell.TextFieldTableCell;
//import javafx.scene.input.KeyCode;
//import javafx.scene.input.KeyEvent;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//import javafx.util.StringConverter;
//import javafx.util.converter.BigDecimalStringConverter;
//import javafx.util.converter.DoubleStringConverter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Controller;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.time.Instant;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Component
//@Scope("prototype")
//@Controller
//public class PurchaseFormController {
//
//    // Services
//    private PurchasetransactionService purchasetransactionService;
//    private PurchasedetailService purchasedetailService;
//    private PaymentService paymentService;
//    private ItemService itemService;
//    private PersonService personService;
//    private EmployeeService employeeService;
//    private PurchaseInvoiceService purchaseInvoiceService;
//    private Purchasetransaction currentPurchaseTransaction;
//
//    @FXML private ComboBox<Person> supplierComboBox;
//    @FXML private ComboBox<Employee> employeeComboBox;
//    @FXML private ComboBox<String> pWayComboBox;
//    @FXML private TextField purchaseNumberField;
//    @FXML private TextField purchaseNoteField;
//    @FXML private TextField fatoraNumberField;
//    @FXML private Label currentPurchaseLabel;
//    @FXML private TextField totalAmountField;
//    @FXML private TextField pFeeField;
//    @FXML private TextField supplierBalanceField;
//
//    @FXML private Button savePurchaseButton;
//    @FXML private Button newPurchaseButton;
//    @FXML private Button printInvoiceButton;
//    @FXML private Button previewInvoiceButton;
//    @FXML private Button firstPurchaseButton;
//    @FXML private Button previousPurchaseButton;
//    @FXML private Button nextPurchaseButton;
//    @FXML private Button lastPurchaseButton;
//
//    @FXML private TableView<Purchasedetail> purchaseDetailTable;
//    @FXML private TableColumn<Purchasedetail, Integer> pdIdColumn;
//    @FXML private TableColumn<Purchasedetail, Integer> itemIdColumn;
//    @FXML private TableColumn<Purchasedetail, Item> itemColumn;
//    @FXML private TableColumn<Purchasedetail, Double> quantityColumn;
//    @FXML private TableColumn<Purchasedetail, BigDecimal> purchasingPriceColumn;
//    @FXML private TableColumn<Purchasedetail, BigDecimal> comulativePriceColumn;
//
//    @FXML private TextField pdQuantityField;
//    @FXML private TextField pdPurchasingPriceField;
//    @FXML private TextField pdComulativePriceField;
//    @FXML private Button addPurchaseDetailButton;
//    @FXML private Button deletePurchaseDetailButton;
//
//    @FXML private TableView<Payment> paymentTable;
//    @FXML private TableColumn<Payment, Integer> pIdColumn;
//    @FXML private TableColumn<Payment, BigDecimal> pAmountColumn;
//    @FXML private TableColumn<Payment, Instant> pDateColumn;
//    @FXML private TableColumn<Payment, String> pTypeColumn;
//    @FXML private TableColumn<Payment, String> pWayColumn;
//    @FXML private TextField pAmountField;
//    @FXML private TextField pTypeField;
//    @FXML private TextField pWayField;
//    @FXML private Button addPaymentButton;
//    @FXML private Button deletePaymentButton;
//
//    // Payment way options with withdrawal fees
//    private static final ObservableList<String> PAYMENT_WAYS = FXCollections.observableArrayList(
//            "Cash - نقدي",
//            "Card - بطاقة",
//            "Bank Transfer - تحويل بنكي",
//            "Check - شيك",
//            "Credit - آجل",
//            "Vodafone Cash - فودافون كاش",
//            "Etisalat Cash - اتصالات كاش"
//    );
//
//    // Payment ways that require withdrawal fees
//    private static final String VODAFONE_CASH = "Vodafone Cash - فودافون كاش";
//    private static final String ETISALAT_CASH = "Etisalat Cash - اتصالات كاش";
//    private static final double FEE_PER_1K = 15.0; // 15 EGP per 1,000 EGP
//
//    // Payment type for purchases (always Creditor for Purchase)
//    private static final String PAYMENT_TYPE_CREDITOR = "Creditor - دائن";
//
//    private BigDecimal currentWithdrawalFee = BigDecimal.ZERO;
//    private Map<Payment, BigDecimal> paymentFeesMap = new HashMap<>();
//    private BigDecimal totalWithdrawalFees = BigDecimal.ZERO;
//
//    private ObservableList<Purchasedetail> purchasedetailList;
//    private ObservableList<Payment> paymentList;
//    private ObservableList<Person> masterSupplierList;
//    private ObservableList<Item> itemsList;
//    private ObservableList<Employee> employeesList;
//
//    @Autowired
//    private SpringFXMLLoader springFXMLLoader;
//
//    @FXML
//    public void initialize() {
//        // Wire services
//        this.paymentService = SpringFXMLLoader.loadController(PaymentService.class);
//        this.purchasedetailService = SpringFXMLLoader.loadController(PurchasedetailService.class);
//        this.purchasetransactionService = SpringFXMLLoader.loadController(PurchasetransactionService.class);
//        this.personService = SpringFXMLLoader.loadController(PersonService.class);
//        this.employeeService = SpringFXMLLoader.loadController(EmployeeService.class);
//        this.itemService = SpringFXMLLoader.loadController(ItemService.class);
//        this.purchaseInvoiceService = SpringFXMLLoader.loadController(PurchaseInvoiceService.class);
//
//        initializePaymentWayComboBox();
//        initializeSupplierComboBox();
//        initializeEmployeeComboBox();
//
//        itemsList = FXCollections.observableArrayList(itemService.findAllItems());
//
//        setupPurchaseDetailTable();
//        setupPaymentsTable();
//
//        initializePurchaseNumber();
//        updateCurrentPurchaseLabel();
//
//        purchasedetailList = FXCollections.observableArrayList();
//        purchaseDetailTable.setItems(purchasedetailList);
//        addBlankDetailRow();
//
//        paymentList = FXCollections.observableArrayList();
//        paymentTable.setItems(paymentList);
//
//        pAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
//            calculateWithdrawalFee();
//        });
//
//        purchaseDetailTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//            if (event.getCode() == KeyCode.TAB) {
//                event.consume();
//                TablePosition<Purchasedetail, ?> pos = purchaseDetailTable.getFocusModel().getFocusedCell();
//                if (pos != null) {
//                    int row = pos.getRow();
//                    int col = pos.getColumn();
//                    if (col < purchaseDetailTable.getColumns().size() - 1) {
//                        purchaseDetailTable.edit(row, purchaseDetailTable.getColumns().get(col + 1));
//                    } else {
//                        if (row < purchasedetailList.size() - 1) {
//                            purchaseDetailTable.getSelectionModel().select(row + 1);
//                            purchaseDetailTable.edit(row + 1, itemColumn);
//                        } else {
//                            addBlankDetailRow();
//                            purchaseDetailTable.getSelectionModel().selectLast();
//                            purchaseDetailTable.edit(purchasedetailList.size() - 1, itemColumn);
//                        }
//                    }
//                }
//            }
//        });
//    }
//
//    private void initializePaymentWayComboBox() {
//        pWayComboBox.setItems(PAYMENT_WAYS);
//        pWayComboBox.setValue("Cash - نقدي");
//        pWayComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
//            calculateWithdrawalFee();
//        });
//    }
//
//    private void calculateWithdrawalFee() {
//        try {
//            String paymentWay = pWayComboBox.getValue();
//            String amountText = pAmountField.getText();
//
//            if (paymentWay == null || amountText == null || amountText.trim().isEmpty()) {
//                pFeeField.setText("0.00");
//                currentWithdrawalFee = BigDecimal.ZERO;
//                return;
//            }
//
//            if (VODAFONE_CASH.equals(paymentWay) || ETISALAT_CASH.equals(paymentWay)) {
//                BigDecimal amount = new BigDecimal(amountText);
//                BigDecimal fee = amount.divide(new BigDecimal("1000"), 4, BigDecimal.ROUND_HALF_UP)
//                        .multiply(new BigDecimal(FEE_PER_1K));
//                fee = fee.setScale(2, BigDecimal.ROUND_HALF_UP);
//                pFeeField.setText(fee.toPlainString());
//                currentWithdrawalFee = fee;
//            } else {
//                pFeeField.setText("0.00");
//                currentWithdrawalFee = BigDecimal.ZERO;
//            }
//        } catch (NumberFormatException e) {
//            pFeeField.setText("0.00");
//            currentWithdrawalFee = BigDecimal.ZERO;
//        }
//    }
//
//    private BigDecimal calculateTotalWithdrawalFees() {
//        BigDecimal totalFees = BigDecimal.ZERO;
//        for (Map.Entry<Payment, BigDecimal> entry : paymentFeesMap.entrySet()) {
//            totalFees = totalFees.add(entry.getValue());
//        }
//        return totalFees;
//    }
//
//    private void updatePurchaseNoteWithFees() {
//        StringBuilder feeNotes = new StringBuilder();
//
//        for (Map.Entry<Payment, BigDecimal> entry : paymentFeesMap.entrySet()) {
//            Payment payment = entry.getKey();
//            BigDecimal fee = entry.getValue();
//
//            if (fee.compareTo(BigDecimal.ZERO) > 0) {
//                if (feeNotes.length() > 0) {
//                    feeNotes.append("; ");
//                }
//                feeNotes.append(String.format("Withdrawal fee (%s): %.2f EGP",
//                        payment.getPaymentWay(), fee));
//            }
//        }
//
//        if (feeNotes.length() > 0) {
//            String currentNote = purchaseNoteField.getText();
//            if (currentNote != null && currentNote.contains("Withdrawal fee")) {
//                String[] parts = currentNote.split(";");
//                StringBuilder cleanNotes = new StringBuilder();
//                for (String part : parts) {
//                    if (!part.trim().startsWith("Withdrawal fee")) {
//                        if (cleanNotes.length() > 0) {
//                            cleanNotes.append("; ");
//                        }
//                        cleanNotes.append(part.trim());
//                    }
//                }
//                currentNote = cleanNotes.toString();
//            }
//
//            if (currentNote != null && !currentNote.trim().isEmpty()) {
//                purchaseNoteField.setText(currentNote + "; " + feeNotes.toString());
//            } else {
//                purchaseNoteField.setText(feeNotes.toString());
//            }
//        }
//    }
//
//    private void recalcTotalAmountWithFees() {
//        BigDecimal itemsTotal = purchasedetailList.stream()
//                .filter(this::isRowFilled)
//                .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        BigDecimal totalFees = calculateTotalWithdrawalFees();
//        BigDecimal grandTotal = itemsTotal.add(totalFees);
//
//        totalWithdrawalFees = totalFees;
//        totalAmountField.setText(grandTotal.toPlainString());
//        updateSupplierBalance();
//    }
//
//    private void updateSupplierBalance() {
//        Person selectedSupplier = supplierComboBox.getValue();
//        if (selectedSupplier == null) {
//            supplierBalanceField.setText("0.00");
//            return;
//        }
//
//        try {
//            BigDecimal openingBalance = selectedSupplier.getOpenBalance();
//            if (openingBalance == null) {
//                openingBalance = BigDecimal.ZERO;
//            }
//
//            BigDecimal totalPurchases = personService.calculateRemainingBalance(selectedSupplier.getId());
//            if (totalPurchases == null) {
//                totalPurchases = BigDecimal.ZERO;
//            }
//
//            String currentTotalText = totalAmountField.getText();
//            BigDecimal currentPurchaseTotal = BigDecimal.ZERO;
//            if (currentTotalText != null && !currentTotalText.trim().isEmpty()) {
//                try {
//                    currentPurchaseTotal = new BigDecimal(currentTotalText);
//                } catch (NumberFormatException e) {
//                    // Ignore
//                }
//            }
//
//            BigDecimal currentPayments = paymentList.stream()
//                    .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//            BigDecimal currentBalance = openingBalance
//                    .add(totalPurchases)
//                    .add(currentPurchaseTotal)
//                    .subtract(currentPayments);
//
//            supplierBalanceField.setText(currentBalance.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            supplierBalanceField.setText("Error");
//        }
//    }
//
//    private void initializeSupplierComboBox() {
//        List<Person> suppliers = personService.findByType("Supplier");
//        masterSupplierList = FXCollections.observableArrayList(suppliers);
//        supplierComboBox.setItems(masterSupplierList);
//        supplierComboBox.setEditable(true);
//
//        supplierComboBox.setConverter(new StringConverter<Person>() {
//            @Override
//            public String toString(Person person) {
//                return person == null ? "" : person.getName();
//            }
//
//            @Override
//            public Person fromString(String string) {
//                if (string == null || string.trim().isEmpty()) {
//                    return null;
//                }
//                return masterSupplierList.stream()
//                        .filter(p -> p.getName().equalsIgnoreCase(string.trim()))
//                        .findFirst()
//                        .orElse(null);
//            }
//        });
//
//        TextField editor = supplierComboBox.getEditor();
//        final boolean[] isUpdating = {false};
//
//        editor.textProperty().addListener((obs, oldVal, newVal) -> {
//            if (isUpdating[0]) return;
//            isUpdating[0] = true;
//            try {
//                if (newVal == null || newVal.trim().isEmpty()) {
//                    supplierComboBox.setItems(masterSupplierList);
//                    supplierComboBox.hide();
//                } else {
//                    final String filterText = newVal.trim().toLowerCase();
//                    ObservableList<Person> filtered = masterSupplierList.stream()
//                            .filter(person -> person.getName().toLowerCase().contains(filterText))
//                            .collect(Collectors.toCollection(FXCollections::observableArrayList));
//                    supplierComboBox.setItems(filtered);
//                    if (!filtered.isEmpty()) {
//                        if (!supplierComboBox.isShowing()) {
//                            supplierComboBox.show();
//                        }
//                    } else {
//                        supplierComboBox.hide();
//                    }
//                }
//            } finally {
//                isUpdating[0] = false;
//            }
//        });
//
//        editor.setOnKeyPressed(event -> {
//            if (event.getCode() == KeyCode.DOWN) {
//                if (!supplierComboBox.isShowing()) {
//                    supplierComboBox.show();
//                }
//                event.consume();
//            } else if (event.getCode() == KeyCode.ENTER) {
//                if (supplierComboBox.getItems().size() == 1) {
//                    supplierComboBox.setValue(supplierComboBox.getItems().get(0));
//                    supplierComboBox.hide();
//                    event.consume();
//                }
//            } else if (event.getCode() == KeyCode.ESCAPE) {
//                supplierComboBox.hide();
//                event.consume();
//            }
//        });
//
//        supplierComboBox.setOnAction(event -> {
//            Person selected = supplierComboBox.getValue();
//            if (selected != null) {
//                editor.setText(selected.getName());
//                updateSupplierBalance();
//            }
//        });
//    }
//
//    private void initializeEmployeeComboBox() {
//        employeesList = FXCollections.observableArrayList(employeeService.findAllEmployees());
//        employeeComboBox.setItems(employeesList);
//        employeeComboBox.setConverter(new StringConverter<Employee>() {
//            @Override
//            public String toString(Employee e) {
//                return e == null ? "" : e.getName();
//            }
//            @Override
//            public Employee fromString(String s) {
//                return employeesList.stream()
//                        .filter(e -> e.getName().equals(s))
//                        .findFirst()
//                        .orElse(null);
//            }
//        });
//    }
//
//    private void initializePurchaseNumber() {
//        try {
//            Integer nextId = purchasetransactionService.findAllPurchasetransactions().size() + 1;
//            purchaseNumberField.setText(String.valueOf(nextId));
//        } catch (Exception e) {
//            purchaseNumberField.setText("AUTO");
//        }
//    }
//
//    private void updateCurrentPurchaseLabel() {
//        if (currentPurchaseTransaction != null && currentPurchaseTransaction.getId() != null) {
//            currentPurchaseLabel.setText("#" + currentPurchaseTransaction.getId());
//            currentPurchaseLabel.setStyle("-fx-text-fill: #27ae60; -fx-background-color: #e8f8f5;");
//        } else {
//            currentPurchaseLabel.setText("NEW");
//            currentPurchaseLabel.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: #fee;");
//        }
//    }
//
//    private void setupPurchaseDetailTable() {
//        purchaseDetailTable.setEditable(true);
//
//        pdIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
//                cell.getValue().getId() != null ? cell.getValue().getId().getId() : null
//        ));
//
//        itemIdColumn.setCellValueFactory(cellData -> {
//            Item it = cellData.getValue().getItem();
//            return new ReadOnlyObjectWrapper<>(it != null ? it.getId() : null);
//        });
//
//        itemColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getItem()));
//        itemColumn.setEditable(true);
//        itemColumn.setCellFactory(column -> {
//            ComboBoxTableCell<Purchasedetail, Item> cell = new ComboBoxTableCell<>(
//                    new StringConverter<Item>() {
//                        @Override
//                        public String toString(Item item) {
//                            return item == null ? "" : item.getName();
//                        }
//                        @Override
//                        public Item fromString(String string) {
//                            if (string == null || string.trim().isEmpty()) {
//                                return null;
//                            }
//                            return itemsList.stream()
//                                    .filter(i -> i.getName().equalsIgnoreCase(string.trim()))
//                                    .findFirst()
//                                    .orElse(null);
//                        }
//                    },
//                    itemsList.toArray(new Item[0])
//            );
//            cell.setComboBoxEditable(true);
//            return cell;
//        });
//
//        itemColumn.setOnEditCommit(e -> {
//            try {
//                Purchasedetail d = e.getRowValue();
//                if (d == null) return;
//
//                Item selectedItem = e.getNewValue();
//                d.setItem(selectedItem);
//
//                if (selectedItem != null && selectedItem.getPurchasingPrice() != null) {
//                    d.setPurchasingPrice(selectedItem.getPurchasingPrice());
//                }
//
//                if (d.getQuantity() == null || d.getQuantity() == 0.0) {
//                    d.setQuantity(1.0);
//                }
//
//                recalcCumulativePrice(d);
//                recalcTotalAmount();
//                purchaseDetailTable.refresh();
//
//                int currentIndex = purchasedetailList.indexOf(d);
//                if (currentIndex == purchasedetailList.size() - 1) {
//                    Platform.runLater(() -> {
//                        addBlankDetailRow();
//                        purchaseDetailTable.refresh();
//                    });
//                }
//
//                Platform.runLater(() -> {
//                    int rowIndex = purchasedetailList.indexOf(d);
//                    if (rowIndex >= 0) {
//                        purchaseDetailTable.getSelectionModel().select(rowIndex);
//                        purchaseDetailTable.edit(rowIndex, quantityColumn);
//                    }
//                });
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });
//
//        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
//        quantityColumn.setEditable(true);
//        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
//        quantityColumn.setOnEditCommit(event -> {
//            try {
//                Purchasedetail detail = event.getRowValue();
//                if (detail == null) return;
//
//                detail.setQuantity(event.getNewValue());
//                recalcCumulativePrice(detail);
//                recalcTotalAmount();
//                purchaseDetailTable.refresh();
//
//                int currentIndex = purchasedetailList.indexOf(detail);
//                if (currentIndex == purchasedetailList.size() - 1 && isRowFilled(detail)) {
//                    Platform.runLater(() -> {
//                        addBlankDetailRow();
//                        purchaseDetailTable.getSelectionModel().select(currentIndex + 1);
//                        purchaseDetailTable.edit(currentIndex + 1, itemColumn);
//                    });
//                } else if (currentIndex < purchasedetailList.size() - 1) {
//                    Platform.runLater(() -> {
//                        purchaseDetailTable.edit(currentIndex, purchasingPriceColumn);
//                    });
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });
//
//        purchasingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasingPrice"));
//        purchasingPriceColumn.setEditable(true);
//        purchasingPriceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
//        purchasingPriceColumn.setOnEditCommit(event -> {
//            try {
//                Purchasedetail detail = event.getRowValue();
//                if (detail == null) return;
//
//                detail.setPurchasingPrice(event.getNewValue());
//                recalcCumulativePrice(detail);
//                recalcTotalAmount();
//                purchaseDetailTable.refresh();
//
//                int currentIndex = purchasedetailList.indexOf(detail);
//                if (currentIndex < purchasedetailList.size() - 1) {
//                    Platform.runLater(() -> {
//                        purchaseDetailTable.getSelectionModel().select(currentIndex + 1);
//                        purchaseDetailTable.edit(currentIndex + 1, itemColumn);
//                    });
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        });
//
//        comulativePriceColumn.setCellValueFactory(new PropertyValueFactory<>("comulativePrice"));
//    }
//
//    private void setupPaymentsTable() {
//        pIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        pAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
//        pDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
//        pTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
//        pWayColumn.setCellValueFactory(new PropertyValueFactory<>("paymentWay"));
//    }
//
//    @FXML
//    public void onNewPurchase() {
//        boolean hasUnsavedWork = purchasedetailList.stream().anyMatch(this::isRowFilled);
//
//        if (hasUnsavedWork && currentPurchaseTransaction == null) {
//            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
//            confirmAlert.setTitle("Unsaved Changes");
//            confirmAlert.setHeaderText("You have unsaved purchase data");
//            confirmAlert.setContentText("Are you sure you want to create a new purchase? All current data will be lost.");
//
//            Optional<ButtonType> result = confirmAlert.showAndWait();
//            if (result.isEmpty() || result.get() != ButtonType.OK) {
//                return;
//            }
//        }
//
//        resetForm();
//        showAlert("New purchase created. You can now enter purchase details.");
//    }
//
//    private void addBlankDetailRow() {
//        Purchasedetail blank = new Purchasedetail();
//        blank.setQuantity(0.0);
//        blank.setPurchasingPrice(BigDecimal.ZERO);
//        blank.setComulativePrice(BigDecimal.ZERO);
//        purchasedetailList.add(blank);
//    }
//
//    private boolean isRowFilled(Purchasedetail detail) {
//        boolean quantitySet = detail.getQuantity() != null && detail.getQuantity() > 0;
//        boolean priceSet = detail.getPurchasingPrice() != null && detail.getPurchasingPrice().compareTo(BigDecimal.ZERO) > 0;
//        boolean itemSet = detail.getItem() != null;
//        return quantitySet || priceSet || itemSet;
//    }
//
//    private void recalcCumulativePrice(Purchasedetail d) {
//        if (d == null) return;
//        if (d.getQuantity() != null && d.getPurchasingPrice() != null) {
//            BigDecimal q = BigDecimal.valueOf(d.getQuantity());
//            d.setComulativePrice(d.getPurchasingPrice().multiply(q));
//        } else {
//            d.setComulativePrice(BigDecimal.ZERO);
//        }
//    }
//
//    private void recalcTotalAmount() {
//        recalcTotalAmountWithFees();
//    }
//
//    @FXML
//    public void addPurchaseDetail() {
//        try {
//            Double qty = Double.parseDouble(pdQuantityField.getText());
//            BigDecimal purchasing = new BigDecimal(pdPurchasingPriceField.getText());
//            BigDecimal cum = purchasing.multiply(BigDecimal.valueOf(qty));
//            Purchasedetail d = new Purchasedetail();
//            d.setQuantity(qty);
//            d.setPurchasingPrice(purchasing);
//            d.setComulativePrice(cum);
//            purchasedetailList.add(d);
//            recalcTotalAmount();
//            addBlankDetailRow();
//        } catch (Exception ex) {
//            showAlert("Invalid detail input: " + ex.getMessage());
//        }
//    }
//
//    @FXML
//    public void deletePurchaseDetail() {
//        Purchasedetail sel = purchaseDetailTable.getSelectionModel().getSelectedItem();
//        if (sel != null) {
//            purchasedetailList.remove(sel);
//            recalcTotalAmount();
//        } else {
//            showAlert("Select a detail to delete.");
//        }
//    }
//
//    @FXML
//    public void addPayment() {
//        try {
//            BigDecimal amt = new BigDecimal(pAmountField.getText());
//            if (amt.compareTo(BigDecimal.ZERO) <= 0) {
//                showAlert("Payment must be > 0");
//                return;
//            }
//
//            String paymentWay = pWayComboBox.getValue();
//            if (paymentWay == null || paymentWay.trim().isEmpty()) {
//                showAlert("Please select payment way");
//                return;
//            }
//
//            calculateWithdrawalFee();
//            BigDecimal feeAmount = new BigDecimal(pFeeField.getText());
//
//            Payment p = new Payment();
//            p.setAmount(amt);
//            p.setPaymentDate(Instant.now());
//            p.setPaymentType(PAYMENT_TYPE_CREDITOR);
//            p.setPaymentWay(paymentWay);
//            paymentList.add(p);
//
//            paymentFeesMap.put(p, feeAmount);
//            updatePurchaseNoteWithFees();
//            recalcTotalAmount();
//
//            pAmountField.clear();
//            pWayComboBox.setValue("Cash - نقدي");
//            pFeeField.setText("0.00");
//            currentWithdrawalFee = BigDecimal.ZERO;
//        } catch (Exception ex) {
//            showAlert("Invalid payment input: " + ex.getMessage());
//        }
//    }
//
//    @FXML
//    public void deletePayment() {
//        Payment sel = paymentTable.getSelectionModel().getSelectedItem();
//        if (sel != null) {
//            paymentList.remove(sel);
//            paymentFeesMap.remove(sel);
//            updatePurchaseNoteWithFees();
//            recalcTotalAmount();
//        } else {
//            showAlert("Select a payment to delete.");
//        }
//    }
//
//    @FXML
//    public void onSavePurchase() {
//        try {
//            Person supplier = supplierComboBox.getValue();
//            Employee employee = employeeComboBox.getValue();
//
//            if (supplier == null) {
//                showAlert("Choose a supplier");
//                return;
//            }
//            if (employee == null) {
//                showAlert("Choose an employee");
//                return;
//            }
//
//            List<Purchasedetail> detailsToSave = purchasedetailList.stream()
//                    .filter(this::isRowFilled)
//                    .collect(Collectors.toList());
//            if (detailsToSave.isEmpty()) {
//                showAlert("Add at least one purchase detail");
//                return;
//            }
//
//            Purchasetransaction pt = new Purchasetransaction();
//            pt.setPurchaseDate(Instant.now());
//            pt.setPerson(supplier);
//            pt.setSalesRep(employee);
//
//            String note = purchaseNoteField.getText();
//            if (note != null && !note.trim().isEmpty()) {
//                pt.setNotes(note);
//            }
//
//            String fatoraNum = fatoraNumberField.getText();
//            if (fatoraNum != null && !fatoraNum.trim().isEmpty()) {
//                try {
//                    pt.setFatoraNumber(Integer.parseInt(fatoraNum));
//                } catch (NumberFormatException e) {
//                    // Ignore
//                }
//            }
//
//            BigDecimal itemsTotal = detailsToSave.stream()
//                    .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//            BigDecimal totalFees = calculateTotalWithdrawalFees();
//            BigDecimal grandTotal = itemsTotal.add(totalFees);
//
//            pt.setWithdrawalFee(totalFees);
//            pt.setTotalAmount(grandTotal);
//
//            Purchasetransaction saved = purchasetransactionService.savePurchaseWithDetailsAndPayments(
//                    pt,
//                    detailsToSave,
//                    paymentList
//            );
//
//            currentPurchaseTransaction = saved;
//            updateCurrentPurchaseLabel();
//            updateSupplierBalance();
//
//            showAlert("Purchase saved successfully!\n\nPurchase ID: " + saved.getId() +
//                    "\nTotal Amount: " + grandTotal.toPlainString() + " EGP" +
//                    (totalFees.compareTo(BigDecimal.ZERO) > 0 ?
//                            "\n(includes withdrawal fee: " + totalFees.toPlainString() + " EGP)" : ""));
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            showAlert("Error saving purchase: " + ex.getMessage());
//        }
//    }
//
//    private void resetForm() {
//        purchasedetailList.clear();
//        paymentList.clear();
//        paymentFeesMap.clear();
//        totalWithdrawalFees = BigDecimal.ZERO;
//        addBlankDetailRow();
//        recalcTotalAmount();
//        supplierComboBox.setValue(null);
//        employeeComboBox.setValue(null);
//        pWayComboBox.setValue("Cash - نقدي");
//        purchaseNoteField.clear();
//        fatoraNumberField.clear();
//        pFeeField.setText("0.00");
//        supplierBalanceField.setText("0.00");
//        initializePurchaseNumber();
//        currentPurchaseTransaction = null;
//        updateCurrentPurchaseLabel();
//    }
//
//    @FXML
//    public void onFirstPurchase() {
//        try {
//            List<Purchasetransaction> allPurchases = purchasetransactionService.findAllPurchasetransactions();
//            if (allPurchases.isEmpty()) {
//                showAlert("No purchase transactions found in database");
//                return;
//            }
//
//            Purchasetransaction firstPt = allPurchases.stream()
//                    .min(Comparator.comparing(Purchasetransaction::getId))
//                    .orElse(null);
//
//            if (firstPt != null) {
//                loadPurchaseTransaction(firstPt.getId());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            showAlert("Error loading first purchase: " + e.getMessage());
//        }
//    }
//
//    @FXML
//    public void onPreviousPurchase() {
//        try {
//            if (currentPurchaseTransaction == null || currentPurchaseTransaction.getId() == null) {
//                showAlert("No current purchase selected");
//                return;
//            }
//
//            List<Purchasetransaction> allPurchases = purchasetransactionService.findAllPurchasetransactions();
//            Purchasetransaction previousPt = allPurchases.stream()
//                    .filter(pt -> pt.getId() < currentPurchaseTransaction.getId())
//                    .max(Comparator.comparing(Purchasetransaction::getId))
//                    .orElse(null);
//
//            if (previousPt != null) {
//                loadPurchaseTransaction(previousPt.getId());
//            } else {
//                showAlert("This is the first purchase transaction");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            showAlert("Error loading previous purchase: " + e.getMessage());
//        }
//    }
//
//    @FXML
//    public void onNextPurchase() {
//        try {
//            if (currentPurchaseTransaction == null || currentPurchaseTransaction.getId() == null) {
//                showAlert("No current purchase selected");
//                return;
//            }
//
//            List<Purchasetransaction> allPurchases = purchasetransactionService.findAllPurchasetransactions();
//            Purchasetransaction nextPt = allPurchases.stream()
//                    .filter(pt -> pt.getId() > currentPurchaseTransaction.getId())
//                    .min(Comparator.comparing(Purchasetransaction::getId))
//                    .orElse(null);
//
//            if (nextPt != null) {
//                loadPurchaseTransaction(nextPt.getId());
//            } else {
//                showAlert("This is the last purchase transaction");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            showAlert("Error loading next purchase: " + e.getMessage());
//        }
//    }
//
//    @FXML
//    public void onLastPurchase() {
//        try {
//            List<Purchasetransaction> allPurchases = purchasetransactionService.findAllPurchasetransactions();
//            if (allPurchases.isEmpty()) {
//                showAlert("No purchase transactions found in database");
//                return;
//            }
//
//            Purchasetransaction lastPt = allPurchases.stream()
//                    .max(Comparator.comparing(Purchasetransaction::getId))
//                    .orElse(null);
//
//            if (lastPt != null) {
//                loadPurchaseTransaction(lastPt.getId());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            showAlert("Error loading last purchase: " + e.getMessage());
//        }
//    }
//
//    private void loadPurchaseTransaction(Integer purchaseTransactionId) {
//        try {
//            PurchaseInvoiceService.PurchaseInvoiceData data =
//                    purchaseInvoiceService.preparePurchaseInvoice(purchaseTransactionId);
//
//            currentPurchaseTransaction = data.getPurchaseTransaction();
//
//            supplierComboBox.setValue(currentPurchaseTransaction.getPerson());
//            employeeComboBox.setValue(currentPurchaseTransaction.getSalesRep());
//
//            if (currentPurchaseTransaction.getTotalAmount() != null) {
//                totalAmountField.setText(currentPurchaseTransaction.getTotalAmount().toPlainString());
//            } else {
//                totalAmountField.clear();
//            }
//
//            if (currentPurchaseTransaction.getNotes() != null) {
//                purchaseNoteField.setText(currentPurchaseTransaction.getNotes());
//            } else {
//                purchaseNoteField.clear();
//            }
//
//            if (currentPurchaseTransaction.getFatoraNumber() != null) {
//                fatoraNumberField.setText(currentPurchaseTransaction.getFatoraNumber().toString());
//            } else {
//                fatoraNumberField.clear();
//            }
//
//            paymentFeesMap.clear();
//
//            if (currentPurchaseTransaction.getWithdrawalFee() != null) {
//                totalWithdrawalFees = currentPurchaseTransaction.getWithdrawalFee();
//            } else {
//                totalWithdrawalFees = BigDecimal.ZERO;
//            }
//
//            purchasedetailList.clear();
//            purchasedetailList.addAll(data.getDetails());
//            addBlankDetailRow();
//
//            paymentList.clear();
//            paymentList.addAll(data.getPayments());
//
//            if (totalWithdrawalFees.compareTo(BigDecimal.ZERO) > 0) {
//                List<Payment> paymentsWithFees = paymentList.stream()
//                        .filter(p ->
//                                VODAFONE_CASH.equals(p.getPaymentWay()) ||
//                                        ETISALAT_CASH.equals(p.getPaymentWay()))
//                        .toList();
//
//                if (!paymentsWithFees.isEmpty()) {
//                    BigDecimal totalPaymentsAmount = paymentsWithFees.stream()
//                            .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
//                            .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//                    if (totalPaymentsAmount.compareTo(BigDecimal.ZERO) > 0) {
//                        for (Payment payment : paymentsWithFees) {
//                            BigDecimal paymentAmount =
//                                    payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
//
//                            BigDecimal proportion = paymentAmount.divide(
//                                    totalPaymentsAmount, 6, RoundingMode.HALF_UP);
//
//                            BigDecimal paymentFee = totalWithdrawalFees
//                                    .multiply(proportion)
//                                    .setScale(2, RoundingMode.HALF_UP);
//
//                            paymentFeesMap.put(payment, paymentFee);
//                        }
//                    }
//                }
//            }
//
//            updatePurchaseNoteWithFees();
//            updateCurrentPurchaseLabel();
//            updateSupplierBalance();
//
//            purchaseDetailTable.refresh();
//            paymentTable.refresh();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            showAlert("Error loading purchase: " + e.getMessage());
//        }
//    }
//
//    @FXML
//    public void onPreviewInvoice() {
//        try {
//            Person supplier = supplierComboBox.getValue();
//            Employee employee = employeeComboBox.getValue();
//
//            List<Purchasedetail> detailsToPreview = purchasedetailList.stream()
//                    .filter(this::isRowFilled)
//                    .collect(Collectors.toList());
//
//            if (supplier == null || employee == null || detailsToPreview.isEmpty()) {
//                showAlert("Please fill in supplier, employee, and at least one item before previewing.");
//                return;
//            }
//
//            Parent root = SpringFXMLLoader.load("/fxml/Purchaseinvoicepreview.fxml");
//            PurchaseInvoicePreviewController controller =
//                    SpringFXMLLoader.loadController(PurchaseInvoicePreviewController.class);
//
//            if (currentPurchaseTransaction != null && currentPurchaseTransaction.getId() != null && currentPurchaseTransaction.getId() > 0) {
//                controller.setPurchaseTransaction(currentPurchaseTransaction);
//            } else {
//                Purchasetransaction tempTx = buildTemporaryPurchaseTransaction();
//                List<Purchasedetail> previewDetails = new ArrayList<>(detailsToPreview);
//                List<Payment> previewPayments = new ArrayList<>(paymentList);
//                controller.setPurchaseTransactionDirect(tempTx, previewDetails, previewPayments);
//            }
//
//            Stage stage = new Stage();
//            stage.initModality(Modality.APPLICATION_MODAL);
//            String titleSuffix = (currentPurchaseTransaction != null && currentPurchaseTransaction.getId() != null && currentPurchaseTransaction.getId() > 0)
//                    ? " - Purchase " + currentPurchaseTransaction.getId()
//                    : " - PREVIEW (Unsaved)";
//            stage.setTitle("Purchase Invoice Preview" + titleSuffix);
//            stage.setScene(new Scene(root));
//            stage.show();
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            showAlert("Error opening invoice preview: " + ex.getMessage());
//        }
//    }
//
//    private Purchasetransaction buildTemporaryPurchaseTransaction() {
//        Purchasetransaction tx = new Purchasetransaction();
//        tx.setId(0);
//        tx.setPurchaseDate(Instant.now());
//        tx.setPerson(supplierComboBox.getValue());
//        tx.setSalesRep(employeeComboBox.getValue());
//
//        String note = purchaseNoteField.getText();
//        if (note != null && !note.trim().isEmpty()) {
//            tx.setNotes(note);
//        }
//
//        String fatoraNum = fatoraNumberField.getText();
//        if (fatoraNum != null && !fatoraNum.trim().isEmpty()) {
//            try {
//                tx.setFatoraNumber(Integer.parseInt(fatoraNum));
//            } catch (NumberFormatException e) {
//                // Ignore if invalid
//            }
//        }
//
//        List<Purchasedetail> detailsToPreview = purchasedetailList.stream()
//                .filter(this::isRowFilled)
//                .collect(Collectors.toList());
//
//        BigDecimal itemsTotal = detailsToPreview.stream()
//                .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        BigDecimal totalFees = calculateTotalWithdrawalFees();
//        BigDecimal grandTotal = itemsTotal.add(totalFees);
//
//        tx.setWithdrawalFee(totalFees);
//        tx.setTotalAmount(grandTotal);
//
//        for (int i = 0; i < detailsToPreview.size(); i++) {
//            Purchasedetail detail = detailsToPreview.get(i);
//            PurchasedetailId detailId = new PurchasedetailId();
//            detailId.setId(i + 1);
//            detailId.setPurchasetransactionId(0);
//            detail.setId(detailId);
//            detail.setPurchaseTransaction(tx);
//        }
//
//        for (Payment payment : paymentList) {
//            payment.setPurchaseTransaction(tx);
//        }
//
//        return tx;
//    }
//
//    @FXML
//    public void onPrintInvoice() {
//        try {
//            if (currentPurchaseTransaction == null) {
//                showAlert("Please save or select a purchase transaction before printing.");
//                return;
//            }
//
//            PurchaseInvoiceService.PurchaseInvoiceData invoiceData = purchaseInvoiceService.preparePurchaseInvoice(currentPurchaseTransaction.getId());
//
//            Parent root = SpringFXMLLoader.load("/fxml/Purchaseinvoicepreview.fxml");
//            PurchaseInvoicePreviewController controller = SpringFXMLLoader.loadController(PurchaseInvoicePreviewController.class);
//            controller.setPurchaseTransaction(invoiceData.getPurchaseTransaction());
//
//            Stage hiddenStage = new Stage();
//            hiddenStage.setOpacity(0);
//            Scene scene = new Scene(root);
//            hiddenStage.setScene(scene);
//            hiddenStage.show();
//
//            Platform.runLater(() -> {
//                try {
//                    PrinterJob job = PrinterJob.createPrinterJob();
//                    if (job != null) {
//                        boolean proceed = job.showPrintDialog(hiddenStage);
//
//                        if (proceed) {
//                            Node printNode = scene.getRoot();
//
//                            double scaleX = job.getJobSettings().getPageLayout().getPrintableWidth() / printNode.getBoundsInParent().getWidth();
//                            double scaleY = job.getJobSettings().getPageLayout().getPrintableHeight() / printNode.getBoundsInParent().getHeight();
//                            double scale = Math.min(scaleX, scaleY);
//
//                            if (scale < 1.0) {
//                                printNode.getTransforms().add(new javafx.scene.transform.Scale(scale, scale));
//                            }
//
//                            boolean success = job.printPage(printNode);
//
//                            if (scale < 1.0) {
//                                printNode.getTransforms().clear();
//                            }
//
//                            if (success) {
//                                job.endJob();
//                                showAlert("تم إرسال الفاتورة إلى الطابعة بنجاح");
//                            } else {
//                                showAlert("فشلت عملية الطباعة");
//                            }
//                        }
//                    } else {
//                        showAlert("لم يتم العثور على طابعة متاحة");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    showAlert("خطأ في الطباعة: " + e.getMessage());
//                } finally {
//                    hiddenStage.close();
//                }
//            });
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            showAlert("Error printing invoice: " + ex.getMessage());
//        }
//    }
//
//    private void showAlert(String msg) {
//        Alert a = new Alert(Alert.AlertType.INFORMATION);
//        a.setTitle("Purchase");
//        a.setContentText(msg);
//        a.showAndWait();
//    }
//}