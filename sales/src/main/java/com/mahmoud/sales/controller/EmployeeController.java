package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Employee;
import com.mahmoud.sales.handler.EmployeeHandler;
import com.mahmoud.sales.service.EmployeeService;
import com.mahmoud.sales.util.SpringFXMLLoader;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
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
import java.util.Objects;

@Controller
public class EmployeeController {

    private EmployeeService employeeService;

    @FXML private StackPane root;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private Button refreshButton;
    @FXML private Button addEmployeeButton;

    @FXML private ComboBox<Integer> pageSizeCombo;
    @FXML private ComboBox<String> sortCombo;

    @FXML private Label statusLabel;

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> idColumn;
    @FXML private TableColumn<Employee, String> nameColumn;
    @FXML private TableColumn<Employee, String> roleColumn;
    @FXML private TableColumn<Employee, String> emailColumn;
    @FXML private TableColumn<Employee, BigDecimal> salaryColumn;
    @FXML private TableColumn<Employee, Void> actionColumn;

    @FXML private Pagination pagination;

    private final ObservableList<Employee> tableData = FXCollections.observableArrayList();

    private PauseTransition searchDebounce;
    private Task<?> runningTask;

    private int pageSize = 50;
    private String sortField = "name";
    private boolean sortAsc = true;

    @FXML
    public void initialize() {
        this.employeeService = SpringFXMLLoader.loadController(EmployeeService.class);

        setupCombos();
        setupTable();
        setupSearch();
        setupPagination();

        employeeTable.setItems(tableData);
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
                "Role (A → Z)",
                "Role (Z → A)",
                "Email (A → Z)",
                "Email (Z → A)",
                "Salary (Low → High)",
                "Salary (High → Low)"
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
            case "Role (A → Z)" -> { sortField = "role"; sortAsc = true; }
            case "Role (Z → A)" -> { sortField = "role"; sortAsc = false; }
            case "Email (A → Z)" -> { sortField = "email"; sortAsc = true; }
            case "Email (Z → A)" -> { sortField = "email"; sortAsc = false; }
            case "Salary (Low → High)" -> { sortField = "salary"; sortAsc = true; }
            case "Salary (High → Low)" -> { sortField = "salary"; sortAsc = false; }
            default -> { sortField = "name"; sortAsc = true; }
        }
    }

    private void setupTable() {
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        salaryColumn.setCellValueFactory(new PropertyValueFactory<>("salary"));

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                viewButton.getStyleClass().addAll("btn", "btn-primary");
                editButton.getStyleClass().addAll("btn", "btn-warning");
                deleteButton.getStyleClass().addAll("btn", "btn-danger");

                viewButton.setOnAction(e -> handleViewEmployeePopup(getTableView().getItems().get(getIndex())));
                editButton.setOnAction(e -> handleEditEmployeePopup(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> handleDeleteEmployee(getTableView().getItems().get(getIndex())));
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
    private void handleAddEmployeePopup() {
        showPopup("Add New Employee", "/fxml/addEmployee.fxml", null);
    }

    private void handleViewEmployeePopup(Employee employee) {
        showPopup("View Employee", "/fxml/viewEmployee.fxml", employee);
    }

    private void handleEditEmployeePopup(Employee employee) {
        showPopup("Edit Employee", "/fxml/editEmployee.fxml", employee);
    }

    private void handleDeleteEmployee(Employee employee) {
        if (employee == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Employee");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete this employee?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        employeeService.deleteEmployee(employee.getId());
        loadPageAsync(pagination.getCurrentPageIndex());
    }

    private void showPopup(String title, String fxmlPath, Employee employee) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent ui = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof EmployeeHandler handler) {
                handler.setEmployee(employee);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(ui));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadPageAsync(pagination.getCurrentPageIndex());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, title, "Failed to open window.");
        }
    }

    private void loadPageAsync(int pageIndex) {
        if (runningTask != null && runningTask.isRunning()) runningTask.cancel();

        final String q = (searchField.getText() == null) ? "" : searchField.getText().trim();
        final int safeIndex = Math.max(pageIndex, 0);

        Task<PageBundle> task = new Task<>() {
            @Override
            protected PageBundle call() {
                var page = employeeService.findEmployeesPaged(q, safeIndex, pageSize, sortField, sortAsc);
                return new PageBundle(
                        page.getContent(),
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
            tableData.setAll(b.rows);
            updatePaginationUI(b.totalPages, safeIndex);
            updateStatusBar(b.totalElements, b.pageNumber, b.pageSize, b.pageElements);
            setLoading(false);
        });

        task.setOnFailed(e -> {
            setLoading(false);
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Employees", "Failed to load employees.");
        });

        Thread t = new Thread(task, "employees-load-page");
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
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record PageBundle(
            java.util.List<Employee> rows,
            int totalPages,
            long totalElements,
            int pageNumber,
            int pageSize,
            int pageElements
    ) {}
}
