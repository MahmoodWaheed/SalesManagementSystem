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
import javafx.util.Duration;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.*;

@Controller
public class SupplierController {

    private static final String TYPE = "Supplier";

    private PersonService personService;
    private PhoneService phoneService;

    @FXML private StackPane root;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private Button refreshButton;
    @FXML private Button addButton;

    @FXML private ComboBox<Integer> pageSizeCombo;
    @FXML private ComboBox<String> sortCombo;

    @FXML private Label supplierCountLabel;
    @FXML private Label totalOpenBalanceLabel;
    @FXML private Label totalBalanceLabel;

    @FXML private Label statusLabel;

    @FXML private TableView<Person> personTable;
    @FXML private TableColumn<Person, Integer> idColumn;
    @FXML private TableColumn<Person, String> nameColumn;
    @FXML private TableColumn<Person, String> locationColumn;
    @FXML private TableColumn<Person, BigDecimal> balanceColumn;
    @FXML private TableColumn<Person, BigDecimal> remainingBalanceColumn;
    @FXML private TableColumn<Person, String> phonesColumn;
    @FXML private TableColumn<Person, Void> actionColumn;

    @FXML private Pagination pagination;

    private final ObservableList<Person> tableData = FXCollections.observableArrayList();
    private Map<Integer, String> phonesByPersonId = Map.of();
    private Map<Integer, BigDecimal> remainingByPersonId = Map.of();

    private PauseTransition searchDebounce;
    private Task<?> runningTask;

    private int pageSize = 50;
    private String sortField = "name";
    private boolean sortAsc = true;

    @FXML
    public void initialize() {
        this.personService = SpringFXMLLoader.loadController(PersonService.class);
        this.phoneService = SpringFXMLLoader.loadController(PhoneService.class);

        setupCombos();
        setupTable();
        setupSearch();
        setupPagination();

        personTable.setItems(tableData);
        loadPageAsync(0);
    }

    private void setupCombos() {
        pageSizeCombo.getItems().setAll(25, 50, 100);
        pageSizeCombo.setValue(50);
        pageSizeCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                pageSize = newV;
                loadPageAsync(0);
            }
        });

        sortCombo.getItems().setAll(
                "Name (A → Z)",
                "Name (Z → A)",
                "Open Balance (Low → High)",
                "Open Balance (High → Low)"
        );
        sortCombo.setValue("Name (A → Z)");
        sortCombo.valueProperty().addListener((obs, oldV, newV) -> {
            applySortSelection(newV);
            loadPageAsync(0);
        });
    }

    private void applySortSelection(String selection) {
        if (selection == null) selection = "Name (A → Z)";

        switch (selection) {
            case "Name (Z → A)" -> { sortField = "name"; sortAsc = false; }
            case "Open Balance (Low → High)" -> { sortField = "openBalance"; sortAsc = true; }
            case "Open Balance (High → Low)" -> { sortField = "openBalance"; sortAsc = false; }
            default -> { sortField = "name"; sortAsc = true; }
        }
    }

    private void setupTable() {
        personTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("openBalance"));

        phonesColumn.setCellValueFactory(cd -> {
            Person p = cd.getValue();
            if (p == null || p.getId() == null) return new SimpleStringProperty("");
            return new SimpleStringProperty(phonesByPersonId.getOrDefault(p.getId(), ""));
        });

        remainingBalanceColumn.setCellValueFactory(cd -> {
            Person p = cd.getValue();
            if (p == null || p.getId() == null) return new SimpleObjectProperty<>(BigDecimal.ZERO);

            BigDecimal baseRemaining = remainingByPersonId.getOrDefault(p.getId(), BigDecimal.ZERO); // trans - pay
            BigDecimal open = (p.getOpenBalance() == null) ? BigDecimal.ZERO : p.getOpenBalance();

            // Supplier rule: totalBalance = openBalance + (trans - pay)
            return new SimpleObjectProperty<>(open.add(baseRemaining));
        });

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            private final Button editButton = new Button("Edit");
            private final Button viewButton = new Button("View");

            {
                deleteButton.getStyleClass().addAll("btn", "btn-danger");
                editButton.getStyleClass().addAll("btn", "btn-warning");
                viewButton.getStyleClass().addAll("btn", "btn-primary");

                deleteButton.setOnAction(e -> handleDeletePopup(getTableView().getItems().get(getIndex())));
                editButton.setOnAction(e -> handleEditPopup(getTableView().getItems().get(getIndex())));
                viewButton.setOnAction(e -> handleViewPopup(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                HBox box = new HBox(viewButton, editButton, deleteButton);
                box.setSpacing(8);
                setGraphic(box);
            }
        });
    }

    private void setupSearch() {
        searchDebounce = new PauseTransition(Duration.millis(300));
        searchDebounce.setOnFinished(e -> loadPageAsync(0));
        searchField.textProperty().addListener((obs, oldV, newV) -> searchDebounce.playFromStart());
    }

    private void setupPagination() {
        pagination.setPageCount(1);
        pagination.setMaxPageIndicatorCount(7);
        pagination.currentPageIndexProperty().addListener((obs, oldV, newV) -> {
            if (!Objects.equals(oldV, newV)) loadPageAsync(newV.intValue());
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
        showPopup("Add New Supplier", "/fxml/addSupplier.fxml", null);
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

            loadPageAsync(pagination.getCurrentPageIndex());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, title, "Failed to open " + title);
        }
    }

    private void loadPageAsync(int pageIndex) {
        if (runningTask != null && runningTask.isRunning()) runningTask.cancel();

        final String q = (searchField.getText() == null) ? "" : searchField.getText().trim();
        final int safeIndex = Math.max(pageIndex, 0);

        Task<PageBundle> task = new Task<>() {
            @Override
            protected PageBundle call() {
                var page = personService.findByTypePaged(TYPE, q, safeIndex, pageSize, sortField, sortAsc);

                List<Person> persons = page.getContent();
                List<Integer> ids = persons.stream().map(Person::getId).filter(Objects::nonNull).toList();

                Map<Integer, String> phonesMap = phoneService.findPhoneNumbersByPersonIds(ids);
                Map<Integer, BigDecimal> remainingMap = personService.getRemainingBalanceByIds(ids);

                PersonService.Totals totals = personService.getTotalsByTypeAndSearch(TYPE, q);

                return new PageBundle(
                        persons,
                        phonesMap,
                        remainingMap,
                        totals,
                        page.getTotalPages(),
                        page.getTotalElements(),
                        page.getNumber(),
                        page.getSize(),
                        page.getNumberOfElements()
                );
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
            updateStatusBar(b.totalElements, b.pageNumber, b.pageSize, b.pageElements);

            setLoading(false);
        });

        task.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Suppliers", "Failed to load suppliers.");
        });

        Thread t = new Thread(task, "suppliers-load-page");
        t.setDaemon(true);
        t.start();
    }

    private void updatePaginationUI(int totalPages, int currentIndex) {
        int pages = Math.max(totalPages, 1);
        if (pagination.getPageCount() != pages) pagination.setPageCount(pages);
        if (pagination.getCurrentPageIndex() != currentIndex) {
            pagination.setCurrentPageIndex(Math.min(currentIndex, pages - 1));
        }
    }

    private void updateTotalsUI(PersonService.Totals totals) {
        long count = totals.count();
        BigDecimal sumOpen = totals.sumOpenBalance();
        BigDecimal sumRemaining = totals.sumRemainingBalance(); // trans - pay

        // Supplier total balance rule: total = sumRemaining + sumOpen
        BigDecimal totalBalanceAll = sumRemaining.add(sumOpen);

        supplierCountLabel.setText(String.valueOf(count));
        totalOpenBalanceLabel.setText(sumOpen.toString());
        totalBalanceLabel.setText(totalBalanceAll.toString());
    }

    private void updateStatusBar(long total, int pageNumber, int pageSize, int pageElements) {
        if (total <= 0 || pageElements <= 0) {
            statusLabel.setText("Showing 0–0 of 0");
            return;
        }
        long from = (long) pageNumber * pageSize + 1;
        long to = (long) pageNumber * pageSize + pageElements;
        statusLabel.setText("Showing " + from + "–" + to + " of " + total);
    }

    private void setLoading(boolean isLoading) {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(isLoading);
            root.setDisable(isLoading);
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
            int totalPages,
            long totalElements,
            int pageNumber,
            int pageSize,
            int pageElements
    ) {}
}
