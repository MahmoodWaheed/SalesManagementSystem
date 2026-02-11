//package com.mahmoud.sales.controller;
//
//import com.mahmoud.sales.entity.Person;
//import com.mahmoud.sales.entity.Phone;
//import com.mahmoud.sales.handler.PersonHandler;
//import com.mahmoud.sales.service.PersonService;
//import com.mahmoud.sales.service.PhoneService;
//import com.mahmoud.sales.util.SpringFXMLLoader;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.layout.HBox;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import javafx.util.Callback; // Correct import for JavaFX
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Controller
//public class CustomerController {
//
//    @Autowired
//    private  PersonService personService;
//    @FXML
//    private TableView<Person> personTable;
//    @FXML
//    private TableColumn<Person, Integer> idColumn;
//    @FXML
//    private TableColumn<Person, String> nameColumn;
//    @FXML
//    private TableColumn<Person, String> locationColumn;
//    @FXML
//    private TableColumn<Person, String> typeColumn;
//    @FXML
//    public TableColumn<Person, BigDecimal> remainingBalanceColumn;
//    @FXML
//    private TableColumn<Person, BigDecimal> balanceColumn;
//    @FXML
//    private TableColumn<Person, String> phonesColumn;
//    @FXML
//    private TableColumn<Person, BigDecimal> transactionAmountColumn;
//    @FXML
//    private TableColumn<Person, BigDecimal> paymentAmountColumn;
//    @FXML
//    private TextField nameField;
//    @FXML
//    private TextField locationField;
//    @FXML
//    private TextField typeField;
//    @FXML
//    private TextField balanceField;
//    @FXML
//    private Label customerCountLabel;
//    @FXML
//    private Label totalOpenBalanceLabel;
//    @FXML
//    private Label totalBalanceLabel;
//    @FXML
//    private TableColumn<Person, Void> actionColumn; // For buttons
//
//    private ObservableList<Person> personList;
//    private PhoneService phoneService;
//
//    @FXML
//    public void initialize() {
//        // Manually wire dependencies using SpringFXMLLoader
//        this.personService = SpringFXMLLoader.loadController(PersonService.class);
//        this.phoneService = SpringFXMLLoader.loadController(PhoneService.class);
//        personTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//        // Initialize table columns
//        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
//        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
//        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
//        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("openBalance"));
////        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("transactionAmount"));
////        paymentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("paymentAmount"));
////        remainingBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
//
//        // Add a column for phones
//        phonesColumn.setCellValueFactory(cellData -> {
//            Person person = cellData.getValue();
//            List<Phone> phones = phoneService.findPhonesByPersonId(person.getId());
//            String phoneNumbers = phones.stream().map(Phone::getPhoneNumber).collect(Collectors.joining(", "));
//            return new javafx.beans.property.SimpleStringProperty(phoneNumbers);
//        });
//
//        // Add a column for remaining balance
//        remainingBalanceColumn.setCellValueFactory(cellData -> {
//            Person person = cellData.getValue();
//
//            // Get the remaining balance for the person
//            BigDecimal remainingBalance = personService.calculateRemainingBalance(person.getId());
//            BigDecimal obenBalance = person.getOpenBalance();
//            remainingBalance = remainingBalance.subtract(obenBalance);
//
//            return new javafx.beans.property.SimpleObjectProperty<>(remainingBalance);
//        });
//
//        // Load all persons into the table
//        loadPersons();
//
//        // Add buttons to each row in the "Actions" column
//        addActionButtonsToTable();
//    }
//
//    // Method to add action buttons to the "Actions" column
//    private void addActionButtonsToTable() {
//        Callback<TableColumn<Person, Void>, TableCell<Person, Void>> cellFactory = new Callback<>() {
//            @Override
//            public TableCell<Person, Void> call(final TableColumn<Person, Void> param) {
//                final TableCell<Person, Void> cell = new TableCell<>() {
//
//                    private final Button deleteButton = new Button("Delete");
//                    private final Button editButton = new Button("Edit");
//                    private final Button viewButton = new Button("View");
//                    {
//                        // Add action handlers
//                        deleteButton.setOnAction(event -> {
//                            Person person = getTableView().getItems().get(getIndex());
//                            handleDeletePopup(person);
//                        });
//
//                        editButton.setOnAction(event -> {
//                            Person person = getTableView().getItems().get(getIndex());
//                            handleEditPopup(person);
//                        });
//
//                        viewButton.setOnAction(event -> {
//                            Person person = getTableView().getItems().get(getIndex());
//                            handleViewPopup(person);
//                        });
//                    }
//
//                    @Override
//                    public void updateItem(Void item, boolean empty) {
//                        super.updateItem(item, empty);
//                        if (empty) {
//                            setGraphic(null);
//                        } else {
//                            HBox buttons = new HBox(editButton, viewButton, deleteButton);
//                            buttons.setSpacing(10);
//                            setGraphic(buttons);
//                        }
//                    }
//                };
//                return cell;
//            }
//        };
//        actionColumn.setCellFactory(cellFactory);
//    }
//    // Handlers for popups
//    public void handleDeletePopup(Person person) {
//        // Logic for showing a delete confirmation popup
//        showPopup("Delete Person", "/fxml/deletePerson.fxml", person);
//    }
//
//    public void handleEditPopup(Person person) {
//        // Logic for showing an edit form popup
//        showPopup("Edit Person", "/fxml/editPerson.fxml", person);
//    }
//
//    public void handleViewPopup(Person person) {
//        // Logic for showing a view details popup
//        showPopup("View Person", "/fxml/ViewPerson.fxml", person);
//    }
//
//    public void handleAddPersonPopup() {
//        // Logic for showing a popup to add a new Customer
//         showPopup("Add New Person", "/fxml/addCustomer.fxml", null);
////        addPerson();
//    }
//
//    @FXML
//    // General method to show popups
//    private void showPopup(String title, String fxmlPath, Person person) {
//        try {
//            // Load the FXML file
//            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
//            Parent root = loader.load();
//
//            // Use the common interface to handle the person object instead of using if statements for each action
//            PersonHandler controller = loader.getController();
//            controller.setPerson(person); // This works for both Edit , Delete , add and View
//
//
//            // Create a new stage for the popup window
//            Stage stage = new Stage();
//            stage.setTitle(title);
//            stage.setScene(new Scene(root));
//            stage.initModality(Modality.APPLICATION_MODAL);  // Block interactions with the main window until this one is closed
//            stage.showAndWait();  // Wait until the popup is closed
//
//            // Reload the table after editing
//            loadPersons();
//        } catch (Exception e) {
//            e.printStackTrace();
//            showAlert(Alert.AlertType.ERROR, title, "Failed to open " + title);
//        }
//    }
//    // Helper method to show alerts
//    private void showAlert(Alert.AlertType alertType, String title, String message) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    @FXML
//    public void addPerson() {
//        // Implement the logic to add a new Person
//        String name = nameField.getText();
//        String location = locationField.getText();
////        String type = typeField.getText();
//        BigDecimal balance = new BigDecimal(balanceField.getText());
//
//
//        // Automatically set the type as "Customer"
//        Person person = new Person();
//        person.setName(name);
//        person.setLocation(location);
//        person.setType("Customer"); // Set type to Customer
//        person.setOpenBalance(balance);
//        personService.savePerson(person);
//        loadPersons();  // Reload table
//    }
//
//    @FXML
//    public void deletePerson() {
//        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
//        if (selectedPerson != null) {
//            personService.deletePerson(selectedPerson.getId());
//            loadPersons();  // Reload table
//        }
//    }
//    @FXML
//    public void loadPersons() {
//        // Fetch all persons from the service
//        List<Person> persons = personService.findAllPersons();
//
//        // Filter persons to show only those of type 'Customer'
//        List<Person> customers = persons.stream()
//                .filter(person -> "Customer".equals(person.getType()))
//                .toList();
//
//        // Update the customer count and total open balance
//        int customerCount = customers.size();
//        BigDecimal totalBalance = customers.stream()
//                .map(Person::getOpenBalance)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Update the أجمالى المديونية (Total Remaining Balance)
//        BigDecimal totalBalanceAll = customers.stream()
//                .map(customer -> personService.calculateRemainingBalance(customer.getId())
//                        .subtract(customer.getOpenBalance()))  // remainingBalance = totalTransactions - openBalance
//                .reduce(BigDecimal.ZERO, BigDecimal::add);  // Sum all remaining balances
//
//        // Set the values in the text views
//        customerCountLabel.setText("Number of Customers: " + customerCount);
//        totalOpenBalanceLabel.setText("Total Open Balance: " + totalBalance);
//        totalBalanceLabel.setText(": أجمالى المديونية" +totalBalanceAll);
//
//        ObservableList<Person> customerData = FXCollections.observableArrayList(customers);
//        personTable.setItems(customerData);
//
//
//    }
//}
package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.handler.PersonHandler;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.service.PhoneService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class CustomerController {

    private static final String TYPE = "Customer";
    private static final int PAGE_SIZE = 50;

    // Services (wired using SpringFXMLLoader to avoid null injection in JavaFX controllers)
    private PersonService personService;
    private PhoneService phoneService;

    // UI
    @FXML private StackPane root;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private Button refreshButton;
    @FXML private Button addButton;

    @FXML private Label customerCountLabel;
    @FXML private Label totalOpenBalanceLabel;
    @FXML private Label totalBalanceLabel;

    @FXML private TableView<Person> personTable;
    @FXML private TableColumn<Person, Integer> idColumn;
    @FXML private TableColumn<Person, String> nameColumn;
    @FXML private TableColumn<Person, String> locationColumn;
    @FXML private TableColumn<Person, BigDecimal> balanceColumn;
    @FXML private TableColumn<Person, BigDecimal> remainingBalanceColumn;
    @FXML private TableColumn<Person, String> phonesColumn;
    @FXML private TableColumn<Person, Void> actionColumn;

    @FXML private Pagination pagination;

    // Data (current page caches)
    private final ObservableList<Person> tableData = FXCollections.observableArrayList();
    private Map<Integer, String> phonesByPersonId = Collections.emptyMap();
    private Map<Integer, BigDecimal> remainingByPersonId = Collections.emptyMap();

    private PauseTransition searchDebounce;
    private Task<?> runningTask;

    @FXML
    public void initialize() {
        // Manual wiring (same approach you used before)
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
        this.phoneService = SpringFXMLLoader.loadController(PhoneService.class);

        setupTable();
        setupSearch();
        setupPagination();

        personTable.setItems(tableData);
        loadPageAsync(0);
    }

    private void setupTable() {
        personTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("openBalance"));

        // No DB calls inside cell factories (uses page caches)
        phonesColumn.setCellValueFactory(cd -> {
            Person p = cd.getValue();
            if (p == null || p.getId() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(phonesByPersonId.getOrDefault(p.getId(), ""));
        });

        remainingBalanceColumn.setCellValueFactory(cd -> {
            Person p = cd.getValue();
            if (p == null || p.getId() == null) return new SimpleObjectProperty<>(BigDecimal.ZERO);

            // Base remaining = transactions - payments
            BigDecimal baseRemaining = remainingByPersonId.getOrDefault(p.getId(), BigDecimal.ZERO);
            BigDecimal open = (p.getOpenBalance() == null) ? BigDecimal.ZERO : p.getOpenBalance();

            // Your existing rule for customers:
            // displayedTotalBalance = (transactions - payments) - openBalance
            BigDecimal total = baseRemaining.subtract(open);
            return new SimpleObjectProperty<>(total);
        });

        addActionButtonsToTable();
    }

    private void setupSearch() {
        searchDebounce = new PauseTransition(Duration.millis(300));
        searchDebounce.setOnFinished(e -> loadPageAsync(0));

        searchField.textProperty().addListener((obs, oldV, newV) -> {
            searchDebounce.playFromStart();
        });
    }

    private void setupPagination() {
        pagination.setPageCount(1);
        pagination.setMaxPageIndicatorCount(7);
        pagination.currentPageIndexProperty().addListener((obs, oldV, newV) -> {
            if (!Objects.equals(oldV, newV)) {
                loadPageAsync(newV.intValue());
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadPageAsync(pagination.getCurrentPageIndex());
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        loadPageAsync(0);
    }

    @FXML
    public void handleAddPersonPopup() {
        showPopup("Add New Customer", "/fxml/addCustomer.fxml", null);
    }

    private void addActionButtonsToTable() {
        Callback<TableColumn<Person, Void>, TableCell<Person, Void>> cellFactory = param -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            private final Button editButton = new Button("Edit");
            private final Button viewButton = new Button("View");

            {
                deleteButton.getStyleClass().addAll("btn", "btn-danger");
                editButton.getStyleClass().addAll("btn", "btn-warning");
                viewButton.getStyleClass().addAll("btn", "btn-primary");

                deleteButton.setOnAction(event -> {
                    Person person = getTableView().getItems().get(getIndex());
                    handleDeletePopup(person);
                });

                editButton.setOnAction(event -> {
                    Person person = getTableView().getItems().get(getIndex());
                    handleEditPopup(person);
                });

                viewButton.setOnAction(event -> {
                    Person person = getTableView().getItems().get(getIndex());
                    handleViewPopup(person);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                HBox buttons = new HBox(viewButton, editButton, deleteButton);
                buttons.setSpacing(8);
                setGraphic(buttons);
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }

    public void handleDeletePopup(Person person) {
        showPopup("Delete Person", "/fxml/deletePerson.fxml", person);
    }

    public void handleEditPopup(Person person) {
        showPopup("Edit Person", "/fxml/editPerson.fxml", person);
    }

    public void handleViewPopup(Person person) {
        showPopup("View Person", "/fxml/ViewPerson.fxml", person);
    }

    private void showPopup(String title, String fxmlPath, Person person) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            PersonHandler controller = loader.getController();
            controller.setPerson(person);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Reload current page after changes
            loadPageAsync(pagination.getCurrentPageIndex());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, title, "Failed to open " + title);
        }
    }

    private void loadPageAsync(int pageIndex) {
        // Cancel previous task if still running
        if (runningTask != null && runningTask.isRunning()) {
            runningTask.cancel();
        }

        final String q = (searchField.getText() == null) ? "" : searchField.getText().trim();
        final int safeIndex = Math.max(pageIndex, 0);

        Task<PageBundle> task = new Task<>() {
            @Override
            protected PageBundle call() {
                var page = personService.findByTypePaged(TYPE, q, safeIndex, PAGE_SIZE);
                List<Person> persons = page.getContent();
                List<Integer> ids = persons.stream()
                        .map(Person::getId)
                        .filter(Objects::nonNull)
                        .toList();

                Map<Integer, String> phonesMap = phoneService.findPhoneNumbersByPersonIds(ids);
                Map<Integer, BigDecimal> remainingMap = personService.getRemainingBalanceByIds(ids);
                PersonService.Totals totals = personService.getTotalsByTypeAndSearch(TYPE, q);

                return new PageBundle(persons, phonesMap, remainingMap, totals, page.getTotalPages());
            }
        };

        runningTask = task;
        setLoading(true);

        task.setOnSucceeded(e -> {
            PageBundle b = task.getValue();
            phonesByPersonId = b.phonesById;
            remainingByPersonId = b.remainingById;
            tableData.setAll(b.rows);
            updateTotalsUI(b.totals);
            updatePaginationUI(b.totalPages, safeIndex);
            setLoading(false);
        });

        task.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Customers", "Failed to load customers.");
        });

        Thread t = new Thread(task, "customers-load-page");
        t.setDaemon(true);
        t.start();
    }

    private void updatePaginationUI(int totalPages, int currentIndex) {
        int pages = Math.max(totalPages, 1);
        // Avoid recursion issues: update pageCount only when changed
        if (pagination.getPageCount() != pages) {
            pagination.setPageCount(pages);
        }
        if (pagination.getCurrentPageIndex() != currentIndex) {
            pagination.setCurrentPageIndex(Math.min(currentIndex, pages - 1));
        }
    }

    private void updateTotalsUI(PersonService.Totals totals) {
        long count = totals.count();
        BigDecimal sumOpen = totals.sumOpenBalance();
        BigDecimal sumRemaining = totals.sumRemainingBalance();

        // Customer total balance rule:
        // totalBalanceAll = sumRemaining - sumOpen
        BigDecimal totalBalanceAll = sumRemaining.subtract(sumOpen);

        customerCountLabel.setText(String.valueOf(count));
        totalOpenBalanceLabel.setText(sumOpen.toString());
        totalBalanceLabel.setText(totalBalanceAll.toString());
    }

    private void setLoading(boolean isLoading) {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(isLoading);
            root.setDisable(isLoading);
            // Keep the loading indicator interactive even if root disabled
            loadingIndicator.setDisable(false);
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private record PageBundle(
            List<Person> rows,
            Map<Integer, String> phonesById,
            Map<Integer, BigDecimal> remainingById,
            PersonService.Totals totals,
            int totalPages
    ) {}
}
