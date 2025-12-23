

package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.*;
import com.mahmoud.sales.service.*;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

@Component
@Scope("prototype") // important so each FXML gets its own instance
@Controller
public class SalesFormController {

    // Services
    private TransactionService transactionService;
    private TransactiondetailService transactiondetailService;
    private PaymentService paymentService;
    private ItemService itemService;
    private PersonService personService;
    private EmployeeService employeeService;
    private Transaction currentTransaction;


    @FXML private ComboBox<Person> customerComboBox;
    @FXML private ComboBox<Employee> employeeComboBox;
    @FXML private TextField totalAmountField;
    @FXML private TextField transactionTypeField;
    @FXML private Button saveTransactionButton;
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

    private ObservableList<Transactiondetail> transactiondetailList;
    private ObservableList<Payment> paymentList;
    private ObservableList<Person> masterCustomerList;
    private FilteredList<Person> filteredCustomers;
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
        this.transactionService = SpringFXMLLoader.loadController(TransactionService.class);



        // Load data
        List<Person> customers = personService.findByType("Customer");
        masterCustomerList = FXCollections.observableArrayList(customers);
        filteredCustomers = new FilteredList<>(masterCustomerList, p -> true);
        customerComboBox.setItems(filteredCustomers);
        customerComboBox.setEditable(true);
        customerComboBox.setConverter(new StringConverter<Person>() {
            @Override
            public String toString(Person person) { return person==null ? "" : person.getName(); }
            @Override
            public Person fromString(String string) {
                // When user types and presses Enter, try to find exact match
                for (Person p : masterCustomerList) {
                    if (p.getName().equalsIgnoreCase(string)) return p;
                }
                return null;
            }
        });

        // Autocomplete: listen to editor text changes and filter
        TextField editor = customerComboBox.getEditor();
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            final String filter = (newVal == null) ? "" : newVal.trim().toLowerCase();
            filteredCustomers.setPredicate(person -> person.getName().toLowerCase().contains(filter));
            if (!customerComboBox.isShowing()) customerComboBox.show(); // open dropdown to show matches
        });

        // Employees
        employeesList = FXCollections.observableArrayList(employeeService.findAllEmployees());
        employeeComboBox.setItems(employeesList);
        employeeComboBox.setConverter(new StringConverter<Employee>() {
            @Override public String toString(Employee e) { return e==null ? "" : e.getName(); }
            @Override public Employee fromString(String s) {
                return employeesList.stream().filter(e -> e.getName().equals(s)).findFirst().orElse(null);
            }
        });

        // Items
        itemsList = FXCollections.observableArrayList(itemService.findAllItems());

        // Transaction detail table setup
        tdIdColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(
                cell.getValue().getId() != null ? cell.getValue().getId().getId() : null
        ));

        itemIdColumn.setCellValueFactory(cellData -> {
            Item it = cellData.getValue().getItem();
            return new ReadOnlyObjectWrapper<>(it != null ? it.getId() : null);
        });

        itemColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getItem()));
        itemColumn.setEditable(true);
        itemColumn.setCellFactory(ComboBoxTableCell.forTableColumn(new StringConverter<Item>() {
            @Override public String toString(Item item) { return item==null ? "" : item.getName(); }
            @Override public Item fromString(String string) {
                return itemsList.stream().filter(i -> i.getName().equals(string)).findFirst().orElse(null);
            }
        }, itemsList));
        itemColumn.setOnEditCommit(e -> {
            Transactiondetail d = e.getRowValue();
            d.setItem(e.getNewValue());
            recalcCumulativePrice(d);
            recalcTotalAmount();
            transactionDetailTable.refresh();
        });

        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        quantityColumn.setOnEditCommit(event -> {
            Transactiondetail detail = event.getRowValue();
            detail.setQuantity(event.getNewValue());
            recalcCumulativePrice(detail);
            recalcTotalAmount();
            transactionDetailTable.refresh();
        });

        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        sellingPriceColumn.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        sellingPriceColumn.setOnEditCommit(event -> {
            Transactiondetail detail = event.getRowValue();
            detail.setSellingPrice(event.getNewValue());
            recalcCumulativePrice(detail);
            recalcTotalAmount();
            transactionDetailTable.refresh();
        });

        comulativePriceColumn.setCellValueFactory(new PropertyValueFactory<>("comulativePrice"));

        transactiondetailList = FXCollections.observableArrayList();
        transactionDetailTable.setItems(transactiondetailList);
        addBlankDetailRow();

        // Payments table
        pIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        pAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        pDateColumn.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        pTypeColumn.setCellValueFactory(new PropertyValueFactory<>("paymentType"));
        pWayColumn.setCellValueFactory(new PropertyValueFactory<>("paymentWay"));
        paymentList = FXCollections.observableArrayList();
        paymentTable.setItems(paymentList);

        // total field binds to computed value (string)
        // We'll update it explicitly in recalcTotalAmount()

        // Keyboard handling to add blank row when last is filled
        transactionDetailTable.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (transactiondetailList.isEmpty()
                        || isRowFilled(transactiondetailList.get(transactiondetailList.size()-1))) {
                    addBlankDetailRow();
                    transactionDetailTable.getSelectionModel().selectLast();
                    transactionDetailTable.requestFocus();
                }
            }
        });
    }
    @FXML
    public void onPreviewInvoice() {
        try {
            if (currentTransaction == null) {
                showAlert("Please save or select a transaction before previewing the invoice.");
                return;
            }

            // Load the FXML using SpringFXMLLoader helper (it uses applicationContext::getBean)


            Parent root = SpringFXMLLoader.load("/fxml/InvoicePreview.fxml");

            // Get Spring-managed controller instance and pass the transaction (controller is a Spring bean)
            com.mahmoud.sales.controller.InvoicePreviewController controller =
                    SpringFXMLLoader.loadController(com.mahmoud.sales.controller.InvoicePreviewController.class);

            controller.setTransaction(currentTransaction);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Invoice Preview - Transaction " + currentTransaction.getId());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error opening invoice preview: " + ex.getMessage());
        }
    }


    private void addBlankDetailRow() {
        Transactiondetail blank = new Transactiondetail();
        blank.setQuantity(0.0);
        blank.setSellingPrice(BigDecimal.ZERO);
        blank.setComulativePrice(BigDecimal.ZERO);
        // id will be assigned on save
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
        BigDecimal total = transactiondetailList.stream()
                .filter(this::isRowFilled)
                .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalAmountField.setText(total.toPlainString());
    }

    @FXML
    public void addTransactionDetail() {
        try {
            // You might use the small fields for quick-add; else user edits table directly.
            Double qty = Double.parseDouble(tdQuantityField.getText());
            BigDecimal selling = new BigDecimal(tdSellingPriceField.getText());
            BigDecimal cum = selling.multiply(BigDecimal.valueOf(qty));
            Transactiondetail d = new Transactiondetail();
            d.setQuantity(qty);
            d.setSellingPrice(selling);
            d.setComulativePrice(cum);
            // item must be assigned by user in the table or you can parse an item id/name here.
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
        } else showAlert("Select a detail to delete.");
    }

    @FXML
    public void addPayment() {
        try {
            BigDecimal amt = new BigDecimal(pAmountField.getText());
            if (amt.compareTo(BigDecimal.ZERO) <= 0) { showAlert("Payment must be > 0"); return; }
            Payment p = new Payment();
            p.setAmount(amt);
            p.setPaymentDate(Instant.now());
            p.setPaymentType(pTypeField.getText());
            p.setPaymentWay(pWayField.getText());
            // person & transaction assigned at save
            paymentList.add(p);
        } catch (Exception ex) {
            showAlert("Invalid payment input: " + ex.getMessage());
        }
    }

    @FXML
    public void deletePayment() {
        Payment sel = paymentTable.getSelectionModel().getSelectedItem();
        if (sel != null) paymentList.remove(sel);
        else showAlert("Select a payment to delete.");
    }

    @FXML
    public void onSaveTransaction() {
        try {
            // Validation
            Person customer = customerComboBox.getValue();
            Employee employee = employeeComboBox.getValue();
            if (customer == null) { showAlert("Choose a customer"); return; }
            if (employee == null) { showAlert("Choose an employee"); return; }
            // Filter details (remove blanks)
            List<Transactiondetail> detailsToSave = transactiondetailList.stream()
                    .filter(this::isRowFilled).collect(Collectors.toList());
            if (detailsToSave.isEmpty()) { showAlert("Add at least one transaction detail"); return; }

            // Build Transaction
            Transaction tx = new Transaction();
            tx.setTransactionDate(Instant.now());
            tx.setPerson(customer);
            tx.setSalesRep(employee);
            // If you use TransactionType enum, convert here:
            try {
                // assume setter accepting enum exists; else set string
                tx.setTransactionType(TransactionType.valueOf(transactionTypeField.getText()));
            } catch (Exception ignore) {
                // fallback: if your entity has setTransactionType(String) override,
                // ensure it exists or use an enum mapping
                // We'll store as null or ignore
            }

            // compute total
            BigDecimal total = detailsToSave.stream()
                    .map(d -> d.getComulativePrice() != null ? d.getComulativePrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            tx.setTotalAmount(total);

            // call service that persists transaction + details + payments atomically
            Transaction saved = transactionService.saveTransactionWithDetailsAndPayments(tx, detailsToSave, paymentList);
            currentTransaction = saved; // <--- store for preview

            showAlert("Transaction saved (ID: " + saved.getId() + "). Total: " + saved.getTotalAmount());
            // reset UI or reload
            transactiondetailList.clear();
            paymentList.clear();
            addBlankDetailRow();
            recalcTotalAmount();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error saving transaction: " + ex.getMessage());
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Sales");
        a.setContentText(msg);
        a.showAndWait();
    }
}

