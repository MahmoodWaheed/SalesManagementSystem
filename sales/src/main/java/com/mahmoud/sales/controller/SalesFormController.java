package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.*;
import com.mahmoud.sales.service.*;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.DoubleStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Controller
public class SalesFormController {

    // Injecting service beans via Spring
    // We'll manually wire these using SpringFXMLLoader in initialize()
    private TransactionService transactionService;
    private TransactiondetailService transactiondetailService;
    private PaymentService paymentService;
    private ItemService itemService;
    private PersonService personService;
    private EmployeeService employeeService;

    /**  ****************** Transaction Header Fields ****************** */
    @FXML
    private TextField employeeIdField; // FXML fields for Employee ID used to associate an employee with the transaction
    @FXML
    private TextField personIdField;        // To enter the customer (Person) ID
    @FXML
    private TextField amountField;          // To enter the total amount of the transaction
    @FXML
    private Button addTransactionButton;    // Button for saving transaction (if needed)
    @FXML
    private Button previousTransactionButton; // Button to go back to the previous transaction
    @FXML
    private TextField transactionTypeField; // To enter the type of transaction
    @FXML
    private Button saveTransactionButton; // The "Save Transaction" button
    @FXML
    private Button nextTransactionButton; // Button to create a new transaction (generates Transaction ID)
    @FXML
    private Label transactionIdLabel;       // Label to display the generated Transaction ID

    /** ********** Transaction Detail Section ********** */
    @FXML
    private TableView<Transactiondetail> transactionDetailTable; // Table to display transaction details (line items)
    @FXML
    private TableColumn<Transactiondetail, Integer> tdIdColumn;    // Column for transaction detail ID (composite key part)
    @FXML
    private TableColumn<Transactiondetail, Item> itemColumn;     // Column to display the item name
    @FXML
    private TableColumn<Transactiondetail, Double> quantityColumn; // Column for quantity
    @FXML
    private TableColumn<Transactiondetail, BigDecimal> sellingPriceColumn; // Column for selling price
    @FXML
    private TableColumn<Transactiondetail, BigDecimal> comulativePriceColumn; // Column for cumulative price
    @FXML
    private TableColumn<Transactiondetail, Integer> itemIdColumn; // New column for Item ID
    @FXML
    private TextField tdQuantityField;
    @FXML
    private TextField tdSellingPriceField;
    @FXML
    private TextField tdComulativePriceField;
    @FXML
    private Button addTransactionDetailButton;   // Button to add a new detail
    @FXML
    private Button deleteTransactionDetailButton; // Button to delete selected detail

    /** ********** Payment Section ********** */

    @FXML
    private TableView<Payment> paymentTable; // Table to display payments
    @FXML
    private TableColumn<Payment, Integer> pIdColumn;          // Payment ID column
    @FXML
    private TableColumn<Payment, BigDecimal> pAmountColumn;     // Payment amount column
    @FXML
    private TableColumn<Payment, Instant> pDateColumn;          // Payment date column
    @FXML
    private TableColumn<Payment, String> pTypeColumn;           // Payment type column
    @FXML
    private TableColumn<Payment, String> pWayColumn;            // Payment way column

    // Input fields for adding a new payment
    @FXML
    private TextField pAmountField;
    @FXML
    private TextField pTypeField;
    @FXML
    private TextField pWayField;
    @FXML
    private Button addPaymentButton;    // Button to add a payment
    @FXML
    private Button deletePaymentButton; // Button to delete selected payment
    @FXML
    private ComboBox<Person> customerComboBox; // For selecting a Customer
    @FXML
    private ComboBox<Employee> employeeComboBox; // For selecting an Employee
    @FXML
    private TextField totalAmountField; // To display auto-calculated total

    // Observable lists to hold details and payments for tables
    private ObservableList<Transactiondetail> transactiondetailList;
    private ObservableList<Payment> paymentList;
    private Transaction currentTransaction;

    /**
     * The initialize method is called automatically after the FXML is loaded.
     * We use it to wire dependencies, initialize table columns, and load data.
     */

    @FXML
    public void initialize() {
        // 1) Wire dependencies using SpringFXMLLoader
        this.paymentService = SpringFXMLLoader.loadController(PaymentService.class);
        this.transactiondetailService = SpringFXMLLoader.loadController(TransactiondetailService.class);
        this.transactionService = SpringFXMLLoader.loadController(TransactionService.class);
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
        this.employeeService = SpringFXMLLoader.loadController(EmployeeService.class);
        this.itemService = SpringFXMLLoader.loadController(ItemService.class);

        // 2) Populate Customer ComboBox (all persons of type "Customer")
        List<Person> customers = personService.findByType("Customer");
        customerComboBox.setItems(FXCollections.observableArrayList(customers));
        customerComboBox.setConverter(new StringConverter<Person>() {
            @Override
            public String toString(Person person) {
                return (person == null) ? "" : person.getName();
            }
            @Override
            public Person fromString(String string) {
                for (Person p : customers) {
                    if (p.getName().equals(string)) {
                        return p;
                    }
                }
                return null;
            }
        });

        // 3) Populate Employee ComboBox
        List<Employee> employees = employeeService.findAllEmployees();
        employeeComboBox.setItems(FXCollections.observableArrayList(employees));
        employeeComboBox.setConverter(new StringConverter<Employee>() {
            @Override
            public String toString(Employee emp) {
                return (emp == null) ? "" : emp.getName();
            }
            @Override
            public Employee fromString(String string) {
                for (Employee e : employees) {
                    if (e.getName().equals(string)) {
                        return e;
                    }
                }
                return null;
            }
        });

        // 4) Initialize Transaction Detail Table Columns
        // Column for Transaction Detail ID (composite key)
        tdIdColumn.setCellValueFactory(new PropertyValueFactory<>("id.transactionId"));

        // New column for Item ID: returns the associated item's ID
        itemIdColumn.setCellValueFactory(cellData -> {
            Item item = cellData.getValue().getItem();
            return new ReadOnlyObjectWrapper<>(item != null ? item.getId() : null);
        });

        // Column for Item (as an editable ComboBox)
        itemColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getItem()));
        itemColumn.setEditable(true);
        // Prepare the list of items for the ComboBox using ItemService
        ObservableList<Item> itemsList = FXCollections.observableArrayList(itemService.findAllItems());
        itemColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<Item>() {
            @Override
            public String toString(Item item) {
                return (item == null) ? "" : item.getName();
            }
            @Override
            public Item fromString(String string) {
                for (Item i : itemsList) {
                    if (i.getName().equals(string)) {
                        return i;
                    }
                }
                return null;
            }
        }, itemsList));
        // When the user edits the item column, update the underlying detail
        itemColumn.setOnEditCommit(event -> {
            Transactiondetail detail = event.getRowValue();
            detail.setItem(event.getNewValue());
            transactionDetailTable.refresh();
        });

        // 5) Setup numeric columns: Quantity, Selling Price, and Cumulative Price
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        quantityColumn.setOnEditCommit(event -> {
            Transactiondetail detail = event.getRowValue();
            detail.setQuantity(event.getNewValue());
            recalcCumulativePrice(detail);
            transactionDetailTable.refresh();
            recalcTotalAmount();
        });

        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        sellingPriceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        sellingPriceColumn.setOnEditCommit(event -> {
            Transactiondetail detail = event.getRowValue();
            detail.setSellingPrice(event.getNewValue());
            recalcCumulativePrice(detail);
            transactionDetailTable.refresh();
            recalcTotalAmount();
        });

        comulativePriceColumn.setCellValueFactory(new PropertyValueFactory<>("comulativePrice"));
        comulativePriceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));

        // 6) Initialize Transaction Details Table as empty and add one blank row for data entry
        transactiondetailList = FXCollections.observableArrayList();
        transactionDetailTable.setItems(transactiondetailList);
        addBlankDetailRow();

        // 7) Initialize Payment Table Columns
        pIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        pDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        pTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
        pWayColumn.setCellValueFactory(new PropertyValueFactory<>("paymentWay"));

        // 8) Initialize Payment Table with an empty list (if starting fresh)
        paymentList = FXCollections.observableArrayList();
        paymentTable.setItems(paymentList);
        // Add a key pressed handler to the table to listen for Enter key
        transactionDetailTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // If the user is not currently editing, check if the last row is "filled"
                if (!(transactionDetailTable.getEditingCell() == null)) {
                    if (transactiondetailList.isEmpty() || isRowFilled(transactiondetailList.get(transactiondetailList.size() - 1))) {
                        addBlankDetailRow();
                        // Automatically select and focus on the new last row
                        transactionDetailTable.getSelectionModel().selectLast();
                        transactionDetailTable.requestFocus();
                    }
                }
            }
        });
    }

    /**
     * Adds a blank Transactiondetail row so the user can immediately start typing in the table.
     */
    private void addBlankDetailRow() {
        Transactiondetail blankDetail = new Transactiondetail();
        // Set default values (optional)
        blankDetail.setQuantity(0.0);
        blankDetail.setSellingPrice(BigDecimal.ZERO);
        blankDetail.setComulativePrice(BigDecimal.ZERO);
        // Item remains null until the user selects one.
        transactiondetailList.add(blankDetail);
    }

    /**
     * Recalculates the cumulative price for a given Transactiondetail (sellingPrice * quantity).
     */
    private void recalcCumulativePrice(Transactiondetail detail) {
        if (detail.getQuantity() != null && detail.getSellingPrice() != null) {
            BigDecimal qty = BigDecimal.valueOf(detail.getQuantity());
            BigDecimal price = detail.getSellingPrice();
            detail.setComulativePrice(price.multiply(qty));
        }
    }

    /**
     * Recalculates the total amount for the transaction by summing all cumulative prices in the current Transaction Details table.
     * The result is displayed in the totalAmountField.
     */
    private void recalcTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (Transactiondetail detail : transactiondetailList) {
            if (detail.getComulativePrice() != null) {
                total = total.add(detail.getComulativePrice());
            }
        }
        totalAmountField.setText(total.toString());
    }



    /**
     * Loads transaction details from the service and updates the table.
     */
    private void loadTransactionDetails() {
        List<Transactiondetail> details = transactiondetailService.findAllTransactionDetails();
        transactiondetailList = FXCollections.observableArrayList(details);
        transactionDetailTable.setItems(transactiondetailList);
    }

    /**
     * Loads payments from the service and updates the payment table.
     */
    private void loadPayments() {
        List<Payment> payments = paymentService.findAllPayments();
        paymentList = FXCollections.observableArrayList(payments);
        paymentTable.setItems(paymentList);
    }

    /**
     * Saves a new Transaction using the header fields.
     * (This method might be used separately if not using the "Next" button.)
     */
    @FXML
    public void addTransaction() {
        try {
            Integer personId = Integer.parseInt(personIdField.getText());
            BigDecimal totalAmount = new BigDecimal(amountField.getText());
            String transactionType = transactionTypeField.getText();

            Transaction transaction = new Transaction();
            transaction.setTransactionDate(Instant.now());
            transaction.setTotalAmount(totalAmount);
            transaction.setTransactionType(transactionType);
            // Optionally, associate a Person (currently commented out)
            // transaction.setPerson(personService.findById(personId));

            transactionService.saveTransaction(transaction);
            showAlert("Transaction saved successfully!");
        } catch (Exception e) {
            showAlert("Error saving transaction: " + e.getMessage());
        }
    }

    /**
     * Adds a new transaction detail (line item) to the transaction details table.
     */
    @FXML
    public void addTransactionDetail() {
        try {


            // Parse input values for quantity and prices
            Double quantity = Double.parseDouble(tdQuantityField.getText());
            BigDecimal sellingPrice = new BigDecimal(tdSellingPriceField.getText());
            BigDecimal comulativePrice = new BigDecimal(tdComulativePriceField.getText());

            Transactiondetail detail = new Transactiondetail();
            detail.setQuantity(quantity);
            detail.setSellingPrice(sellingPrice);
            detail.setComulativePrice(comulativePrice);
            // You may need to associate this detail with the current transaction and an item:
            // detail.setTransaction(currentTransaction);
            // detail.setItem(selectedItem);

            transactiondetailService.saveTransactionDetail(detail);
            loadTransactionDetails(); // Refresh the table
            showAlert("Transaction detail added successfully!");
        } catch (Exception e) {
            showAlert("Error adding transaction detail: " + e.getMessage());
        }
    }

    /**
     * Deletes the selected transaction detail from the table.
     */
    @FXML
    public void deleteTransactionDetail() {
        Transactiondetail selectedDetail = transactionDetailTable.getSelectionModel().getSelectedItem();
        if (selectedDetail != null) {
            // Delete by using the transaction id from the composite key
            transactiondetailService.deleteTransactionDetail(selectedDetail.getId().getTransactionId());
            loadTransactionDetails();
            showAlert("Transaction detail deleted successfully!");
        } else {
            showAlert("Please select a transaction detail to delete.");
        }
    }

    /**
     * Adds a new payment using the payment section fields.
     */
    @FXML
    public void addPayment() {
        try {
            BigDecimal amount = new BigDecimal(pAmountField.getText());
            String type = pTypeField.getText();
            String way = pWayField.getText();

            Payment payment = new Payment();
            payment.setAmount(amount);
            payment.setPaymentDate(Instant.now());
            payment.setPaymentType(type);
            payment.setPaymentWay(way);
            // Optionally, associate this payment with the current Transaction and Person:
            // payment.setTransaction(currentTransaction);
            // payment.setPerson(selectedPerson);

            paymentService.savePayment(payment);
            loadPayments(); // Refresh payment table
            showAlert("Payment added successfully!");
        } catch (Exception e) {
            showAlert("Error adding payment: " + e.getMessage());
        }
    }

    /**
     * Deletes the selected payment from the payment table.
     */
    @FXML
    public void deletePayment() {
        Payment selectedPayment = paymentTable.getSelectionModel().getSelectedItem();
        if (selectedPayment != null) {
            paymentService.deletePayment(selectedPayment.getId());
            loadPayments();
            showAlert("Payment deleted successfully!");
        } else {
            showAlert("Please select a payment to delete.");
        }
    }

    /**
     * Utility method to show an alert with the provided message.
     *
     * @param message The message to display in the alert.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sales Form");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Handler for the "Next" button.
     * This method creates a new Transaction record, associates a Person and Employee,
     * and displays the generated Transaction ID.
     */
    @FXML
    public void onNextTransaction() {
        try {
            // 1. Retrieve header field values
            Integer personId = Integer.parseInt(personIdField.getText());
            String transactionTypeStr = transactionTypeField.getText().trim();
            // Convert the entered Arabic transaction type to the enum (if using an enum)
            TransactionType transactionType = TransactionType.fromArabicValue(transactionTypeStr);

            // Look up the Person using the provided ID
            Person person = personService.findPersonById(personId)
                    .orElseThrow(() -> new RuntimeException("No Person found with ID: " + personId));

            // Retrieve the Employee ID from the input field and look up the Employee
            Integer empId = Integer.parseInt(employeeIdField.getText());
            Employee employee = employeeService.findEmployeeById(empId)
                    .orElseThrow(() -> new RuntimeException("No Employee found with ID: " + empId));

            // 2. Create a new Transaction record with initial data.
            Transaction transaction = new Transaction();
            transaction.setTransactionDate(Instant.now());
            // Set initial total amount to zero; we will recalc it based on detail rows.
            transaction.setTotalAmount(BigDecimal.ZERO);
            transaction.setTransactionType(String.valueOf(transactionType)); // Assuming your entity now accepts a TransactionType
            transaction.setPerson(person);
            transaction.setSalesRep(employee);
            transaction.setNote("New transaction");

            // Save the Transaction to generate its ID.
            transaction = transactionService.saveTransaction(transaction);
            // Set currentTransaction reference if needed later.
            currentTransaction = transaction;

            // 3. Loop over each TransactionDetail in the table and process it.
            BigDecimal computedTotal = BigDecimal.ZERO;
            for (Transactiondetail detail : transactiondetailList) {
                // Calculate detail total as quantity * sellingPrice.
                // Assuming quantity is stored as Double and sellingPrice as BigDecimal.
                BigDecimal qty = BigDecimal.valueOf(detail.getQuantity());
                BigDecimal detailTotal = detail.getSellingPrice().multiply(qty);

                // Accumulate the detail total into the computed total.
                computedTotal = computedTotal.add(detailTotal);

                // Associate this detail with the new Transaction.
                detail.setTransaction(transaction);

                // Save/update the TransactionDetail in the database.
                transactiondetailService.saveTransactionDetail(detail);
            }

            // 4. Update the Transaction record with the computed total amount.
            transaction.setTotalAmount(computedTotal);
            transactionService.saveTransaction(transaction);

            // 5. Update the UI with the new Transaction ID and computed total.
            transactionIdLabel.setText("Transaction ID: " + transaction.getId());
            showAlert("Transaction created with ID: " + transaction.getId() +
                    "\nTotal Amount (computed from details): " + computedTotal);

            // Optionally disable header fields and Next button to prevent further edits.
            personIdField.setDisable(true);
            transactionTypeField.setDisable(true);
            nextTransactionButton.setDisable(true);

        } catch (Exception e) {
            showAlert("Error creating transaction: " + e.getMessage());
        }
    }
    
    /**
            * Handler for the "Previous" button.
     * Retrieves and displays the previous transaction relative to the current one.
     */
    @FXML
    public void onPreviousTransaction() {
        try {
            if (currentTransaction == null) {
                showAlert("No current transaction loaded to reference.");
                return;
            }
            // Retrieve the previous transaction.
            // Note: This assumes that you have implemented a method in transactionService
            // that returns the previous transaction based on the current transaction's ID.
            Optional<Transaction> optionalPrevious = transactionService.findPreviousTransaction(currentTransaction.getId());
            if (optionalPrevious.isPresent()) {
                Transaction previousTransaction = optionalPrevious.get();
                // Update header fields with previous transaction data
                personIdField.setText(String.valueOf(previousTransaction.getPerson().getId()));
                amountField.setText(previousTransaction.getTotalAmount().toString());
                transactionTypeField.setText(previousTransaction.getTransactionType().getArabicValue());
                transactionIdLabel.setText("Transaction ID: " + previousTransaction.getId());
                // Update currentTransaction reference
                currentTransaction = previousTransaction;

                // Optionally, load the transaction details and payments associated with this transaction
                loadTransactionDetailsForTransaction(previousTransaction);
                loadPaymentsForTransaction(previousTransaction);
            } else {
                showAlert("No previous transaction available.");
            }
        } catch (Exception e) {
            showAlert("Error loading previous transaction: " + e.getMessage());
        }
    }
    /**
     * Loads transaction details associated with a specific transaction.
     * (Assumes that transactiondetailService has a method to filter by transaction ID.)
     *
     * @param transaction The transaction for which to load details.
     */
    private void loadTransactionDetailsForTransaction(Transaction transaction) {
        // For example, if you have a method like findTransactionDetailsByTransactionId:
        List<Transactiondetail> details = transactiondetailService.findTransactionDetailsByTransactionId(transaction.getId());
        transactiondetailList = FXCollections.observableArrayList(details);
        transactionDetailTable.setItems(transactiondetailList);
    }
    /**
     * Loads payments associated with a specific transaction.
     * (Assumes that paymentService has a method to filter by transaction ID.)
     *
     * @param transaction The transaction for which to load payments.
     */
    private void loadPaymentsForTransaction(Transaction transaction) {
        // For example, if you have a method like findPaymentsByTransactionId:
        List<Payment> payments = paymentService.findPaymentsByTransactionId(transaction.getId());
        paymentList = FXCollections.observableArrayList(payments);
        paymentTable.setItems(paymentList);
    }
    private boolean isRowFilled(Transactiondetail detail) {
        // Consider a row "filled" if any key field is set.
        boolean quantitySet = detail.getQuantity() != null && detail.getQuantity() > 0;
        boolean sellingPriceSet = detail.getSellingPrice() != null && detail.getSellingPrice().compareTo(BigDecimal.ZERO) > 0;
        boolean itemSet = detail.getItem() != null;
        return quantitySet || sellingPriceSet || itemSet;
    }

    @FXML
    public void onSaveTransaction() {
        try {
            // 1) Get selected customer and employee
            Person selectedCustomer = customerComboBox.getValue();
            Employee selectedEmployee = employeeComboBox.getValue();
            if (selectedCustomer == null || selectedEmployee == null) {
                showAlert("Please select both a Customer and an Employee.");
                return;
            }
            // 2) Create and populate the Transaction
            Transaction transaction = new Transaction();
            transaction.setTransactionDate(Instant.now());
            transaction.setPerson(selectedCustomer);
            transaction.setSalesRep(selectedEmployee);

            // If you're using an enum for transaction type, convert it. Otherwise, just store a string:
            transaction.setTransactionType(transactionTypeField.getText());

            // 3) Parse total from totalAmountField
            BigDecimal total = new BigDecimal(totalAmountField.getText());
            transaction.setTotalAmount(total);

            // 4) Save the Transaction to generate its ID
            transaction = transactionService.saveTransaction(transaction);

            // 5) Associate each detail with the newly saved Transaction
            for (Transactiondetail detail : transactiondetailList) {
                detail.setTransaction(transaction);
                transactiondetailService.saveTransactionDetail(detail);
            }

            showAlert("Transaction saved successfully with ID: " + transaction.getId());
        } catch (Exception e) {
            showAlert("Error saving transaction: " + e.getMessage());
        }
    }
}
