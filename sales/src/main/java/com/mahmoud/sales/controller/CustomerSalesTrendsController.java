package com.mahmoud.sales.controller;

import com.mahmoud.sales.entity.Person;
import com.mahmoud.sales.entity.Transaction;
import com.mahmoud.sales.entity.Transactiondetail;
import com.mahmoud.sales.service.PersonService;
import com.mahmoud.sales.service.TransactionService;
import com.mahmoud.sales.service.TransactiondetailService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CustomerSalesTrendsController {

    // Services - Spring managed
    @Autowired
    private PersonService personService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactiondetailService transactiondetailService;

    // Reference to contentPane for navigation
    private AnchorPane contentPane;

    // FILTERS
    @FXML private ComboBox<Person> customerComboBox;
    @FXML private ComboBox<String> periodComboBox;
    @FXML private DatePicker dateFromPicker;
    @FXML private DatePicker dateToPicker;

    // SUMMARY LABELS
    @FXML private Label totalSalesLabel;
    @FXML private Label transactionCountLabel;
    @FXML private Label avgTransactionLabel;
    @FXML private Label growthTrendLabel;

    // CHARTS
    @FXML private LineChart<String, Number> salesTrendChart;
    @FXML private CategoryAxis chartXAxis;
    @FXML private NumberAxis chartYAxis;

    @FXML private BarChart<String, Number> monthlyBarChart;
    @FXML private CategoryAxis barChartXAxis;
    @FXML private NumberAxis barChartYAxis;

    // TOP PRODUCTS TABLE
    @FXML private TableView<ProductSalesData> topProductsTable;
    @FXML private TableColumn<ProductSalesData, Integer> rankColumn;
    @FXML private TableColumn<ProductSalesData, String> productNameColumn;
    @FXML private TableColumn<ProductSalesData, Double> quantitySoldColumn;
    @FXML private TableColumn<ProductSalesData, String> totalRevenueColumn;
    @FXML private TableColumn<ProductSalesData, String> percentageColumn;
    @FXML private Label tableCountLabel;

    // TRANSACTIONS TABLE
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> colDate;
    @FXML private TableColumn<Transaction, String> colInvoiceNo;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, BigDecimal> colAmount;
    @FXML private TableColumn<Transaction, BigDecimal> colPayment;
    @FXML private TableColumn<Transaction, BigDecimal> colBalance;
    @FXML private TableColumn<Transaction, String> colNote;

    // TOP PERIODS TABLE
    @FXML private TableView<PeriodData> topPeriodsTable;
    @FXML private TableColumn<PeriodData, String> colPeriod;
    @FXML private TableColumn<PeriodData, String> colPeriodSales;
    @FXML private TableColumn<PeriodData, Integer> colPeriodCount;
    @FXML private TableColumn<PeriodData, String> colPeriodAvg;

    // DATA
    private ObservableList<Person> customersList;
    private List<Transaction> currentTransactions;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "US"));
    private final DecimalFormat percentFormat = new DecimalFormat("#0.00%");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Set the content pane reference (called by parent controller)
     */
    public void setContentPane(AnchorPane contentPane) {
        this.contentPane = contentPane;
    }

    @FXML
    public void initialize() {
        System.out.println("CustomerSalesTrendsController: initialize() called");

        // CRITICAL: Verify services are injected
        if (personService == null) {
            showError("CRITICAL: PersonService not injected by Spring!");
            return;
        }
        if (transactionService == null) {
            showError("CRITICAL: TransactionService not injected by Spring!");
            return;
        }
        if (transactiondetailService == null) {
            showError("CRITICAL: TransactiondetailService not injected by Spring!");
            return;
        }

        System.out.println("All services successfully injected");

        setupCustomerComboBox();
        setupPeriodComboBox();
        setupDatePickers();
        setupTopProductsTable();
        setupTransactionsTable();
        setupTopPeriodsTable();
        setupCharts();

        // Load initial data
        loadCustomers();
    }

    /**
     * Setup customer combo box
     */
    private void setupCustomerComboBox() {
        if (customerComboBox == null) {
            System.err.println("WARNING: customerComboBox is null");
            return;
        }

        customerComboBox.setConverter(new StringConverter<Person>() {
            @Override
            public String toString(Person person) {
                return person == null ? "" : person.getName();
            }

            @Override
            public Person fromString(String string) {
                return customersList != null ? customersList.stream()
                        .filter(p -> p.getName().equals(string))
                        .findFirst()
                        .orElse(null) : null;
            }
        });
    }

    /**
     * Setup period combo box
     */
    private void setupPeriodComboBox() {
        if (periodComboBox == null) {
            System.err.println("WARNING: periodComboBox is null");
            return;
        }

        ObservableList<String> periods = FXCollections.observableArrayList(
                "Daily",
                "Weekly",
                "Monthly",
                "Yearly"
        );
        periodComboBox.setItems(periods);
        periodComboBox.setValue("Monthly");
    }

    /**
     * Setup date pickers
     */
    private void setupDatePickers() {
        if (dateFromPicker == null || dateToPicker == null) {
            System.err.println("WARNING: Date pickers are null");
            return;
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(6);

        dateFromPicker.setValue(startDate);
        dateToPicker.setValue(endDate);
    }

    /**
     * Setup top products table
     */
    private void setupTopProductsTable() {
        if (topProductsTable == null) return;

        rankColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRank()));

        productNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));

        quantitySoldColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantitySold()));

        totalRevenueColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.2f EGP", cellData.getValue().getTotalRevenue())));

        percentageColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.2f%%", cellData.getValue().getPercentageOfTotal())));
    }

    /**
     * Setup transactions table
     */
    private void setupTransactionsTable() {
        if (transactionsTable == null) return;

        colDate.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getTransactionDate() != null ?
                        dateFormatter.format(cell.getValue().getTransactionDate().atZone(ZoneId.systemDefault()).toLocalDate()) : ""
        ));

        colInvoiceNo.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getId() != null ? String.valueOf(cell.getValue().getId()) : ""
        ));

        colType.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getTransactionType() != null ?
                        cell.getValue().getTransactionType().getArabicValue() : ""
        ));

        colAmount.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                cell.getValue().getTotalAmount()
        ));

        colPayment.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                BigDecimal.ZERO // TODO: Calculate actual payments
        ));

        colBalance.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                cell.getValue().getTotalAmount() // Simplified
        ));

        colNote.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getNote() != null ? cell.getValue().getNote() : ""
        ));
    }

    /**
     * Setup top periods table
     */
    private void setupTopPeriodsTable() {
        if (topPeriodsTable == null) return;

        colPeriod.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPeriod()));

        colPeriodSales.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.2f EGP", cellData.getValue().getTotalSales())));

        colPeriodCount.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTransactionCount()));

        colPeriodAvg.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.2f EGP", cellData.getValue().getAverageSale())));
    }

    /**
     * Setup charts
     */
    private void setupCharts() {
        if (salesTrendChart != null) {
            salesTrendChart.setAnimated(true);
            salesTrendChart.setCreateSymbols(true);
        }

        if (monthlyBarChart != null) {
            monthlyBarChart.setAnimated(true);
        }
    }

    /**
     * Load customers from database
     */
    private void loadCustomers() {
        try {
            List<Person> customers = personService.findByType("Customer");
            customersList = FXCollections.observableArrayList(customers);
            if (customerComboBox != null) {
                customerComboBox.setItems(customersList);
            }
            System.out.println("Loaded " + customers.size() + " customers");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load customers: " + e.getMessage());
        }
    }

    /**
     * Generate report
     */
    @FXML
    public void onGenerateReport() {
        try {
            Person selectedCustomer = customerComboBox.getValue();
            LocalDate startDate = dateFromPicker.getValue();
            LocalDate endDate = dateToPicker.getValue();

            if (selectedCustomer == null) {
                showWarning("Please select a customer");
                return;
            }

            if (startDate == null || endDate == null) {
                showWarning("Please select date range");
                return;
            }

            if (startDate.isAfter(endDate)) {
                showWarning("Start date cannot be after end date");
                return;
            }

            // Load transactions
            currentTransactions = loadCustomerTransactions(selectedCustomer, startDate, endDate);

            if (currentTransactions.isEmpty()) {
                showInfo("No transactions found for the selected period");
                clearAllData();
                return;
            }

            // Generate all reports
            updateSummaryCards();
            updateLineChart();
            updateBarChart();
            updateTopProductsTable();
            updateTransactionsTable();
            updateTopPeriodsTable();

            showInfo("Report generated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error generating report: " + e.getMessage());
        }
    }

    /**
     * Load transactions for customer within date range
     */
    private List<Transaction> loadCustomerTransactions(Person customer, LocalDate startDate, LocalDate endDate) {
        List<Transaction> allTransactions = transactionService.findAllTransactions();

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return allTransactions.stream()
                .filter(t -> t.getPerson() != null && t.getPerson().getId().equals(customer.getId()))
                .filter(t -> t.getTransactionDate() != null)
                .filter(t -> !t.getTransactionDate().isBefore(startInstant) &&
                        t.getTransactionDate().isBefore(endInstant))
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .collect(Collectors.toList());
    }

    /**
     * Update summary cards
     */
    private void updateSummaryCards() {
        BigDecimal totalSales = currentTransactions.stream()
                .map(t -> t.getTotalAmount() != null ? t.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int transactionCount = currentTransactions.size();

        BigDecimal avgTransaction = transactionCount > 0
                ? totalSales.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        totalSalesLabel.setText(String.format("%.2f EGP", totalSales));
        transactionCountLabel.setText(String.valueOf(transactionCount));
        avgTransactionLabel.setText(String.format("%.2f EGP", avgTransaction));
        growthTrendLabel.setText("Stable"); // Simplified
    }

    /**
     * Update line chart
     */
    private void updateLineChart() {
        if (salesTrendChart == null) return;

        salesTrendChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales Amount");

        Map<String, BigDecimal> periodSales = groupTransactionsByPeriod();

        periodSales.forEach((period, amount) -> {
            series.getData().add(new XYChart.Data<>(period, amount));
        });

        salesTrendChart.getData().add(series);
    }

    /**
     * Group transactions by period
     */
    private Map<String, BigDecimal> groupTransactionsByPeriod() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (Transaction transaction : currentTransactions) {
            LocalDate date = transaction.getTransactionDate()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            String period = date.format(formatter);

            result.merge(period, transaction.getTotalAmount(), BigDecimal::add);
        }

        return result;
    }

    /**
     * Update bar chart
     */
    private void updateBarChart() {
        if (monthlyBarChart == null) return;

        monthlyBarChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Monthly Sales");

        Map<String, BigDecimal> monthlySales = groupByMonth();

        monthlySales.forEach((month, amount) -> {
            series.getData().add(new XYChart.Data<>(month, amount));
        });

        monthlyBarChart.getData().add(series);
    }

    /**
     * Group by month
     */
    private Map<String, BigDecimal> groupByMonth() {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (Transaction transaction : currentTransactions) {
            LocalDate date = transaction.getTransactionDate()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            String month = date.format(formatter);

            result.merge(month, transaction.getTotalAmount(), BigDecimal::add);
        }

        return result;
    }

    /**
     * Update top products table
     */
    private void updateTopProductsTable() {
        if (topProductsTable == null) return;

        Map<String, ProductSalesData> productMap = new HashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (Transaction transaction : currentTransactions) {
            List<Transactiondetail> details = transactiondetailService
                    .findTransactionDetailsByTransactionId(transaction.getId());

            for (Transactiondetail detail : details) {
                if (detail.getItem() != null) {
                    String itemName = detail.getItem().getName();
                    Double quantity = detail.getQuantity() != null ? detail.getQuantity() : 0.0;
                    BigDecimal revenue = detail.getComulativePrice() != null
                            ? detail.getComulativePrice() : BigDecimal.ZERO;

                    productMap.computeIfAbsent(itemName, k -> new ProductSalesData(itemName))
                            .addSale(quantity, revenue);

                    grandTotal = grandTotal.add(revenue);
                }
            }
        }

        final BigDecimal finalGrandTotal = grandTotal;
        List<ProductSalesData> productList = productMap.values().stream()
                .peek(p -> p.calculatePercentage(finalGrandTotal))
                .sorted(Comparator.comparing(ProductSalesData::getTotalRevenue).reversed())
                .limit(10)
                .collect(Collectors.toList());

        for (int i = 0; i < productList.size(); i++) {
            productList.get(i).setRank(i + 1);
        }

        topProductsTable.setItems(FXCollections.observableArrayList(productList));
        if (tableCountLabel != null) {
            tableCountLabel.setText(String.format("Showing %d products", productList.size()));
        }
    }

    /**
     * Update transactions table
     */
    private void updateTransactionsTable() {
        if (transactionsTable == null) return;
        transactionsTable.setItems(FXCollections.observableArrayList(currentTransactions));
    }

    /**
     * Update top periods table
     */
    private void updateTopPeriodsTable() {
        if (topPeriodsTable == null) return;

        Map<String, PeriodData> periodMap = new HashMap<>();

        for (Transaction transaction : currentTransactions) {
            LocalDate date = transaction.getTransactionDate()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            String period = date.format(DateTimeFormatter.ofPattern("MMM yyyy"));

            periodMap.computeIfAbsent(period, k -> new PeriodData(period))
                    .addTransaction(transaction.getTotalAmount());
        }

        List<PeriodData> periodList = periodMap.values().stream()
                .sorted(Comparator.comparing(PeriodData::getTotalSales).reversed())
                .limit(5)
                .collect(Collectors.toList());

        topPeriodsTable.setItems(FXCollections.observableArrayList(periodList));
    }

    /**
     * Clear all data
     */
    private void clearAllData() {
        if (totalSalesLabel != null) totalSalesLabel.setText("0.00 EGP");
        if (transactionCountLabel != null) transactionCountLabel.setText("0");
        if (avgTransactionLabel != null) avgTransactionLabel.setText("0.00 EGP");
        if (growthTrendLabel != null) growthTrendLabel.setText("--");

        if (salesTrendChart != null) salesTrendChart.getData().clear();
        if (monthlyBarChart != null) monthlyBarChart.getData().clear();
        if (topProductsTable != null) topProductsTable.getItems().clear();
        if (transactionsTable != null) transactionsTable.getItems().clear();
        if (topPeriodsTable != null) topPeriodsTable.getItems().clear();
    }

    /**
     * Print report
     */
    @FXML
    public void onPrintReport() {
        try {
            if (salesTrendChart == null) return;

            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null) {
                boolean proceed = job.showPrintDialog(salesTrendChart.getScene().getWindow());
                if (proceed) {
                    Node printNode = salesTrendChart.getScene().getRoot();
                    boolean success = job.printPage(printNode);
                    if (success) {
                        job.endJob();
                        showInfo("Report sent to printer successfully");
                    }
                }
            }
        } catch (Exception e) {
            showError("Print failed: " + e.getMessage());
        }
    }

    /**
     * Back to Customer Reports
     */
    @FXML
    public void onBackToCustomerReports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CustomerReportsSubMenu.fxml"));
            loader.setControllerFactory(com.mahmoud.sales.util.SpringFXMLLoader.getContext()::getBean);
            Node node = loader.load();

            CustomerReportsSubMenuController controller = loader.getController();
            controller.setContentPane(contentPane);

            if (contentPane != null) {
                AnchorPane.setTopAnchor(node, 0.0);
                AnchorPane.setBottomAnchor(node, 0.0);
                AnchorPane.setLeftAnchor(node, 0.0);
                AnchorPane.setRightAnchor(node, 0.0);

                contentPane.getChildren().setAll(node);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to navigate back");
        }
    }

    // Alert helpers
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Product sales data class
     */
    public static class ProductSalesData {
        private int rank;
        private String productName;
        private double quantitySold;
        private BigDecimal totalRevenue;
        private double percentageOfTotal;

        public ProductSalesData(String productName) {
            this.productName = productName;
            this.quantitySold = 0.0;
            this.totalRevenue = BigDecimal.ZERO;
        }

        public void addSale(double quantity, BigDecimal revenue) {
            this.quantitySold += quantity;
            this.totalRevenue = this.totalRevenue.add(revenue);
        }

        public void calculatePercentage(BigDecimal grandTotal) {
            if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                this.percentageOfTotal = totalRevenue
                        .divide(grandTotal, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }
        }

        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
        public String getProductName() { return productName; }
        public double getQuantitySold() { return quantitySold; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public double getPercentageOfTotal() { return percentageOfTotal; }
    }

    /**
     * Period data class
     */
    public static class PeriodData {
        private String period;
        private BigDecimal totalSales;
        private int transactionCount;

        public PeriodData(String period) {
            this.period = period;
            this.totalSales = BigDecimal.ZERO;
            this.transactionCount = 0;
        }

        public void addTransaction(BigDecimal amount) {
            this.totalSales = this.totalSales.add(amount != null ? amount : BigDecimal.ZERO);
            this.transactionCount++;
        }

        public BigDecimal getAverageSale() {
            return transactionCount > 0
                    ? totalSales.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
        }

        public String getPeriod() { return period; }
        public BigDecimal getTotalSales() { return totalSales; }
        public int getTransactionCount() { return transactionCount; }
    }
}
