//package com.mahmoud.sales.controller;
//
//import com.mahmoud.sales.entity.Person;
//import com.mahmoud.sales.service.PersonService;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.scene.control.Button;
//import javafx.scene.control.TableColumn;
//import javafx.scene.control.TableView;
//import javafx.scene.control.TextField;
//import javafx.scene.control.cell.PropertyValueFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Controller;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//@Controller
//@Component
//public class PersonController {
//
//    @Autowired
//    private PersonService personService;
//
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
//    private TableColumn<Person, BigDecimal> balanceColumn;
//
//    @FXML
//    private TextField nameField;
//    @FXML
//    private TextField locationField;
//    @FXML
//    private TextField typeField;
//    @FXML
//    private TextField balanceField;
//    @FXML
//    private Button addButton;
//    @FXML
//    private Button deleteButton;
//
//    private ObservableList<Person> personList;
//
//    @FXML
//    public void initialize() {
//        // Initialize table columns
//        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
//        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
//        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
//        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("openBalance"));
//
//        // Load all persons and display them
//        loadPersons();
//    }
//
//    private void loadPersons() {
//        List<Person> persons = personService.findAllPersons();
//        personList = FXCollections.observableArrayList(persons);
//        personTable.setItems(personList);
//    }
//
//    @FXML
//    public void addPerson() {
//        String name = nameField.getText();
//        String location = locationField.getText();
//        String type = typeField.getText();
//        BigDecimal openBalance = new BigDecimal(balanceField.getText());
//
//        Person person = new Person();
//        person.setName(name);
//        person.setLocation(location);
//        person.setType(type);
//        person.setOpenBalance(openBalance);
//
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
//}
package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.service.PersonService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller

public class PersonController {






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
    private TableColumn<Person, BigDecimal> balanceColumn;
    @FXML
    private TableColumn<Person, String> phonesColumn;

    @FXML
    private TextField nameField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField balanceField;


    private ObservableList<Person> personList;
    private  PersonService personService;


    @FXML
    public void initialize() {
        // Initialize table columns

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("openBalance"));

//        // Custom cell value factory to display phones as a comma-separated string
//        phonesColumn.setCellValueFactory(cellData -> {
//            Person person = cellData.getValue();
//            String phones = person.getPhones().stream()
//                    .map(phone -> phone.getPhoneNumber())
//                    .collect(Collectors.joining(", "));
//            return new javafx.beans.property.SimpleStringProperty(phones);
//        });

        // Load all persons and display them
        loadPersonData2();
    }

    @FXML
    private void loadPersons() {

        List<Person> persons = personService.findAllPersons();
        personList = FXCollections.observableArrayList(persons);
        personTable.setItems(personList);
    }



    // Event handler for the "Persons" button
    @FXML
    private void handleShowCustomers() {
        loadPersonData2();
    }


    @FXML
    public void loadPersonData2()
    {
            if (personService != null){
                personList = FXCollections.observableArrayList(personService.findAllPersons());
                personTable.setItems(personList);
            }
//            ObservableList<Person> persons = FXCollections.observableArrayList(personService.findAllPersons());
//            personTable.setItems(persons);
            else {
                // Handle the case where personService is null, e.g., log an error
                System.err.println("personService is null. Please check your configuration.");
            }


    }


    @FXML
    public void addPerson() {
        // Implement the logic to add a new Person
        String name = nameField.getText();
        String location = locationField.getText();
        String type = typeField.getText();
        BigDecimal balance = new BigDecimal(balanceField.getText());

        Person person = new Person();
        person.setName(name);
        person.setLocation(location);
        person.setType(type);
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
}

