package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Item;
import com.mahmoud.sales.handler.ItemHandler;
import com.mahmoud.sales.service.ItemService;
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
public class ItemController {

    private ItemService itemService;

    @FXML private StackPane root;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private Button refreshButton;
    @FXML private Button addItemButton;

    @FXML private ComboBox<Integer> pageSizeCombo;
    @FXML private ComboBox<String> sortCombo;

    @FXML private Label statusLabel;

    @FXML private TableView<Item> itemTable;
    @FXML private TableColumn<Item, Integer> idColumn;
    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, String> descriptionColumn;
    @FXML private TableColumn<Item, Double> balanceColumn;
    @FXML private TableColumn<Item, BigDecimal> sellingPriceColumn;
    @FXML private TableColumn<Item, BigDecimal> purchasingPriceColumn;
    @FXML private TableColumn<Item, Void> actionColumn;

    @FXML private Pagination pagination;

    private final ObservableList<Item> tableData = FXCollections.observableArrayList();

    private PauseTransition searchDebounce;
    private Task<?> runningTask;

    private int pageSize = 50;
    private String sortField = "name";
    private boolean sortAsc = true;

    @FXML
    public void initialize() {
        this.itemService = SpringFXMLLoader.loadController(ItemService.class);

        setupCombos();
        setupTable();
        setupSearch();
        setupPagination();

        itemTable.setItems(tableData);
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
                "Balance (Low → High)",
                "Balance (High → Low)",
                "Selling Price (Low → High)",
                "Selling Price (High → Low)",
                "Purchasing Price (Low → High)",
                "Purchasing Price (High → Low)"
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
            case "Balance (Low → High)" -> { sortField = "itemBalance"; sortAsc = true; }
            case "Balance (High → Low)" -> { sortField = "itemBalance"; sortAsc = false; }
            case "Selling Price (Low → High)" -> { sortField = "sellingPrice"; sortAsc = true; }
            case "Selling Price (High → Low)" -> { sortField = "sellingPrice"; sortAsc = false; }
            case "Purchasing Price (Low → High)" -> { sortField = "purchasingPrice"; sortAsc = true; }
            case "Purchasing Price (High → Low)" -> { sortField = "purchasingPrice"; sortAsc = false; }
            default -> { sortField = "name"; sortAsc = true; }
        }
    }

    private void setupTable() {
        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("itemBalance"));
        sellingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("sellingPrice"));
        purchasingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasingPrice"));

        // Tooltip preview + truncate long description
        descriptionColumn.setCellFactory(col -> new TableCell<>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(String desc, boolean empty) {
                super.updateItem(desc, empty);
                if (empty || desc == null || desc.isBlank()) {
                    setText(null);
                    setTooltip(null);
                    return;
                }

                String trimmed = desc.trim();
                tooltip.setText(trimmed);
                setTooltip(tooltip);

                int max = 40;
                setText(trimmed.length() > max ? trimmed.substring(0, max) + "..." : trimmed);
            }
        });

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                viewButton.getStyleClass().addAll("btn", "btn-primary");
                editButton.getStyleClass().addAll("btn", "btn-warning");
                deleteButton.getStyleClass().addAll("btn", "btn-danger");

                viewButton.setOnAction(e -> handleViewItemPopup(getTableView().getItems().get(getIndex())));
                editButton.setOnAction(e -> handleEditItemPopup(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> handleDeleteItem(getTableView().getItems().get(getIndex())));
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
    private void handleAddItemPopup() {
        showPopup("Add New Item", "/fxml/addItem.fxml", null);
    }

    private void handleViewItemPopup(Item item) {
        showPopup("View Item", "/fxml/viewItem.fxml", item);
    }

    private void handleEditItemPopup(Item item) {
        showPopup("Edit Item", "/fxml/editItem.fxml", item);
    }

    private void handleDeleteItem(Item item) {
        if (item == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Archive Item");
        confirm.setHeaderText(null);
        confirm.setContentText("This will archive the item (soft delete). It will disappear from the list but history remains.\n\nContinue?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            itemService.softDeleteItem(item.getId());
            loadPageAsync(pagination.getCurrentPageIndex());
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Archive Failed", ex.getMessage());
        }
    }

    private void showPopup(String title, String fxmlPath, Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent ui = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof ItemHandler handler) {
                handler.setItem(item);
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
                var page = itemService.findItemsPaged(q, safeIndex, pageSize, sortField, sortAsc);
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
            showAlert(Alert.AlertType.ERROR, "Load Items", "Failed to load items.");
        });

        Thread t = new Thread(task, "items-load-page");
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
            java.util.List<Item> rows,
            int totalPages,
            long totalElements,
            int pageNumber,
            int pageSize,
            int pageElements
    ) {}
}
