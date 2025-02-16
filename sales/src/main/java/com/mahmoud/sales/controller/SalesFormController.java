package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.*;
import com.mahmoud.sales.service.*;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Controller
public class SalesFormController {

    // Injecting service beans via Spring
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactiondetailService transactiondetailService;
    @Autowired
    private PaymentService paymentService;

    // We'll manually wire these using SpringFXMLLoader in initialize()
    private PersonService personService;
    private EmployeeService employeeService;

    // FXML fields for Employee ID used to associate an employee with the transaction
    @FXML
    private TextField employeeIdField;

    // ********** Transaction Header Fields **********
    @FXML
    private TextField personIdField;        // To enter the customer (Person) ID
    @FXML
    private TextField amountField;          // To enter the total amount of the transaction
    @FXML
    private TextField transactionTypeField; // To enter the type of transaction
    @FXML
    private Button addTransactionButton;    // Button for saving transaction (if needed)
    // New FXML element for Previous button
    @FXML
    private Button previousTransactionButton; // Button to go back to the previous transaction

    // ********** Transaction Detail Section **********
    @FXML
    private TableView<Transactiondetail> transactionDetailTable; // Table to display transaction details (line items)
    @FXML
    private TableColumn<Transactiondetail, Integer> tdIdColumn;    // Column for transaction detail ID (composite key part)
    @FXML
    private TableColumn<Transactiondetail, String> itemColumn;     // Column to display the item name
    @FXML
    private TableColumn<Transactiondetail, Double> quantityColumn; // Column for quantity
    @FXML
    private TableColumn<Transactiondetail, BigDecimal> sellingPriceColumn; // Column for selling price
    @FXML
    private TableColumn<Transactiondetail, BigDecimal> comulativePriceColumn; // Column for cumulative price

    // Input fields for adding a new transaction detail (line item)
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

    // ********** Payment Section **********
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

    // ********** New Elements for Next Transaction **********
    @FXML
    private Button nextTransactionButton; // Button to create a new transaction (generates Transaction ID)
    @FXML
    private Label transactionIdLabel;       // Label to display the generated Transaction ID

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
        // Manually wire dependencies using SpringFXMLLoader to ensure Spring beans are injected
        this.paymentService = SpringFXMLLoader.loadController(PaymentService.class);
        this.transactiondetailService = SpringFXMLLoader.loadController(TransactiondetailService.class);
        this.transactionService = SpringFXMLLoader.loadController(TransactionService.class);
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
        this.employeeService = SpringFXMLLoader.loadController(EmployeeService.class);

        // ********** Initialize Transaction Detail Table Columns **********
        // For composite keys, we may need custom cell value factories.
        tdIdColumn.setCellValueFactory(new PropertyValueFactory<>("id.transactionId"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        comulativePriceColumn.setCellValueFactory(new PropertyValueFactory<>("comulativePrice"));
        // For the item column, we display the item's name (if available)
        itemColumn.setCellValueFactory(cellData -> {
            Item item = cellData.getValue().getItem();
            return new SimpleStringProperty(item != null ? item.getName() : "");
        });
        // Load existing transaction details into the table
        loadTransactionDetails();

        // ********** Initialize Payment Table Columns **********
        pIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        pDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        pTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
        pWayColumn.setCellValueFactory(new PropertyValueFactory<>("paymentWay"));
        // Load existing payments into the table
        loadPayments();
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
            // Retrieve and parse header field values
            Integer personId = Integer.parseInt(personIdField.getText());
            BigDecimal totalAmount = new BigDecimal(amountField.getText());
            String transactionTypeStr = transactionTypeField.getText().trim();

            // Convert the entered Arabic transaction type to the enum.
            TransactionType transactionType = TransactionType.fromArabicValue(transactionTypeStr);


            // Look up the Person entity by ID using PersonService (returns Optional<Person>)
            Person person = personService.findPersonById(personId)
                    .orElseThrow(() -> new RuntimeException("No Person found with ID: " + personId));

            // Retrieve the Employee ID from the input field and look up the Employee
            Integer empId = Integer.parseInt(employeeIdField.getText());
            Employee employee = employeeService.findEmployeeById(empId)
                    .orElseThrow(() -> new RuntimeException("No Employee found with ID: " + empId));

            // Create a new Transaction object and set its properties
            Transaction transaction = new Transaction();
            transaction.setTransactionDate(Instant.now());
            transaction.setTotalAmount(totalAmount);
            transaction.setTransactionType(transactionType.getArabicValue());  // set using the enum
            transaction.setNote("New transaction");  // Optional note
            // Associate the Transaction with the Person and Employee
            transaction.setPerson(person);
            transaction.setSalesRep(employee);  // This must be non-null per the entity constraint

            // Save the Transaction, which will auto-generate its ID
            transactionService.saveTransaction(transaction);
            // Update current transaction reference if needed
            currentTransaction = transaction;

            // Display the generated Transaction ID in the label
            transactionIdLabel.setText("Transaction ID: " + transaction.getId());

            // Optionally disable header fields and Next button to prevent further edits
            personIdField.setDisable(true);
            amountField.setDisable(true);
            transactionTypeField.setDisable(true);
            nextTransactionButton.setDisable(true);

            showAlert("Transaction created with ID: " + transaction.getId());
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
}
