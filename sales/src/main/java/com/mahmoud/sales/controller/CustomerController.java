package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.entity.Phone;
import com.mahmoud.sales.handler.PersonHandler;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.service.PhoneService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import javafx.util.Callback; // Correct import for JavaFX
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CustomerController {


    @Autowired
    private  PersonService personService;
    @FXML
    private TableView<Person> personTable;
    @FXML
    private TableColumn<Person, Integer> idColumn;
    @FXML
    private TableColumn<Person, String> nameColumn;
    @FXML
    private TableColumn<Person, String> locationColumn;
    @FXML
    private TableColumn<Person, String> typeColumn;
    @FXML
    public TableColumn<Person, BigDecimal> remainingBalanceColumn;
    @FXML
    private TableColumn<Person, BigDecimal> balanceColumn;
    @FXML
    private TableColumn<Person, String> phonesColumn;
    @FXML
    private TableColumn<Person, BigDecimal> transactionAmountColumn;
    @FXML
    private TableColumn<Person, BigDecimal> paymentAmountColumn;
    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField balanceField;
    @FXML
    private Label customerCountLabel;
    @FXML
    private Label totalOpenBalanceLabel;
    @FXML
    private Label totalBalanceLabel;
    @FXML
    private TableColumn<Person, Void> actionColumn; // For buttons

    private ObservableList<Person> personList;
    private PhoneService phoneService;

    @FXML
    public void initialize() {
        // Manually wire dependencies using SpringFXMLLoader
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
        this.phoneService = SpringFXMLLoader.loadController(PhoneService.class);
        personTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Initialize table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("openBalance"));
//        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("transactionAmount"));
//        paymentAmountColumn.setCellValueFactory(new PropertyValueFactory<>("paymentAmount"));
//        remainingBalanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));

        // Add a column for phones
        phonesColumn.setCellValueFactory(cellData -> {
            Person person = cellData.getValue();
            List<Phone> phones = phoneService.findPhonesByPersonId(person.getId());
            String phoneNumbers = phones.stream().map(Phone::getPhoneNumber).collect(Collectors.joining(", "));
            return new javafx.beans.property.SimpleStringProperty(phoneNumbers);
        });

        // Add a column for remaining balance
        remainingBalanceColumn.setCellValueFactory(cellData -> {
            Person person = cellData.getValue();

            // Get the remaining balance for the person
            BigDecimal remainingBalance = personService.calculateRemainingBalance(person.getId());
            BigDecimal obenBalance = person.getOpenBalance();
            remainingBalance = remainingBalance.subtract(obenBalance);

            return new javafx.beans.property.SimpleObjectProperty<>(remainingBalance);
        });

        // Load all persons into the table
        loadPersons();

        // Add buttons to each row in the "Actions" column
        addActionButtonsToTable();
    }

    // Method to add action buttons to the "Actions" column
    private void addActionButtonsToTable() {
        Callback<TableColumn<Person, Void>, TableCell<Person, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Person, Void> call(final TableColumn<Person, Void> param) {
                final TableCell<Person, Void> cell = new TableCell<>() {

                    private final Button deleteButton = new Button("Delete");
                    private final Button editButton = new Button("Edit");
                    private final Button viewButton = new Button("View");
                    {
                        // Add action handlers
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
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(editButton, viewButton, deleteButton);
                            buttons.setSpacing(10);
                            setGraphic(buttons);
                        }
                    }
                };
                return cell;
            }
        };
        actionColumn.setCellFactory(cellFactory);
    }
    // Handlers for popups
    public void handleDeletePopup(Person person) {
        // Logic for showing a delete confirmation popup
        showPopup("Delete Person", "/fxml/deletePerson.fxml", person);
    }

    public void handleEditPopup(Person person) {
        // Logic for showing an edit form popup
        showPopup("Edit Person", "/fxml/editPerson.fxml", person);
    }

    public void handleViewPopup(Person person) {
        // Logic for showing a view details popup
        showPopup("View Person", "/fxml/ViewPerson.fxml", person);
    }

    public void handleAddPersonPopup() {
        // Logic for showing a popup to add a new Customer
         showPopup("Add New Person", "/fxml/addCustomer.fxml", null);
//        addPerson();
    }

    @FXML
    // General method to show popups
    private void showPopup(String title, String fxmlPath, Person person) {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Use the common interface to handle the person object instead of using if statements for each action
            PersonHandler controller = loader.getController();
            controller.setPerson(person); // This works for both Edit , Delete , add and View


            // Create a new stage for the popup window
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);  // Block interactions with the main window until this one is closed
            stage.showAndWait();  // Wait until the popup is closed

            // Reload the table after editing
            loadPersons();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, title, "Failed to open " + title);
        }
    }
    // Helper method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void addPerson() {
        // Implement the logic to add a new Person
        String name = nameField.getText();
        String location = locationField.getText();
//        String type = typeField.getText();
        BigDecimal balance = new BigDecimal(balanceField.getText());


        // Automatically set the type as "Customer"
        Person person = new Person();
        person.setName(name);
        person.setLocation(location);
        person.setType("Customer"); // Set type to Customer
        person.setOpenBalance(balance);
        personService.savePerson(person);
        loadPersons();  // Reload table
    }

    @FXML
    public void deletePerson() {
        Person selectedPerson = personTable.getSelectionModel().getSelectedItem();
        if (selectedPerson != null) {
            personService.deletePerson(selectedPerson.getId());
            loadPersons();  // Reload table
        }
    }
    @FXML
    public void loadPersons() {
        // Fetch all persons from the service
        List<Person> persons = personService.findAllPersons();

        // Filter persons to show only those of type 'Customer'
        List<Person> customers = persons.stream()
                .filter(person -> "Customer".equals(person.getType()))
                .toList();



        // Update the customer count and total open balance
        int customerCount = customers.size();
        BigDecimal totalBalance = customers.stream()
                .map(Person::getOpenBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Update the أجمالى المديونية (Total Remaining Balance)
        BigDecimal totalBalanceAll = customers.stream()
                .map(customer -> personService.calculateRemainingBalance(customer.getId())
                        .subtract(customer.getOpenBalance()))  // remainingBalance = totalTransactions - openBalance
                .reduce(BigDecimal.ZERO, BigDecimal::add);  // Sum all remaining balances

        // Set the values in the text views
        customerCountLabel.setText("Number of Customers: " + customerCount);
        totalOpenBalanceLabel.setText("Total Open Balance: " + totalBalance);
        totalBalanceLabel.setText(": أجمالى المديونية" +totalBalanceAll);

//        // Fetch transaction details (transaction amount, payment amount, balance) for each customer
//        List<Object[]> transactionDetails = personService.getPersonRemainingBalance();
//
//        // Combine the transaction details with the customers
//        for (Object[] detail : transactionDetails) {
//            Integer personId = (Integer) detail[0];
//            BigDecimal transactionAmount = (BigDecimal) detail[1];
//            BigDecimal paymentAmount = (BigDecimal) detail[2];
//            BigDecimal balance = (BigDecimal) detail[3];
//
//            // Find the corresponding customer in the list and set the values
//            customers.stream()
//                    .filter(person -> person.getId().equals(personId))
//                    .forEach(person -> {
//                        person.setTransactionAmount(transactionAmount);
//                        person.setPaymentAmount(paymentAmount);
//                        person.setBalance(balance);
//                    });
//        }

        ObservableList<Person> customerData = FXCollections.observableArrayList(customers);
        personTable.setItems(customerData);


    }
}

