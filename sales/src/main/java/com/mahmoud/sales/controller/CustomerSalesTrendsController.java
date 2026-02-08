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
//package com.mahmoud.sales.controller;
//
//import com.mahmoud.sales.entity.Person;
//import com.mahmoud.sales.entity.Transaction;
//import com.mahmoud.sales.entity.Transactiondetail;
//import com.mahmoud.sales.service.PersonService;
//import com.mahmoud.sales.service.TransactionService;
//import com.mahmoud.sales.service.TransactiondetailService;
//import com.mahmoud.sales.util.SpringFXMLLoader;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.print.PrinterJob;
//import javafx.scene.Node;
//import javafx.scene.chart.*;
//import javafx.scene.control.*;
//import javafx.scene.layout.AnchorPane;
//import javafx.scene.layout.VBox;
//import javafx.util.StringConverter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.text.DecimalFormat;
//import java.text.NumberFormat;
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.ChronoUnit;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Controller
//public class CustomerSalesTrendsController {
//
//    // Services
//    @Autowired
//    private PersonService personService;
//
//    @Autowired
//    private TransactionService transactionService;
//
//    @Autowired
//    private TransactiondetailService transactiondetailService;
//
//    // Reference to contentPane for navigation
//    private AnchorPane contentPane;
//
//    // Filters
//    @FXML private ComboBox<Person> customerComboBox;
//    @FXML private ComboBox<String> timePeriodComboBox;
//    @FXML private DatePicker startDatePicker;
//    @FXML private DatePicker endDatePicker;
//
//    // Summary Labels
//    @FXML private Label totalSalesLabel;
//    @FXML private Label totalSalesChangeLabel;
//    @FXML private Label transactionCountLabel;
//    @FXML private Label transactionCountChangeLabel;
//    @FXML private Label averageTransactionLabel;
//    @FXML private Label averageChangeLabel;
//    @FXML private Label growthRateLabel;
//    @FXML private Label growthPeriodLabel;
//
//    // Charts
//    @FXML private LineChart<String, Number> salesLineChart;
//    @FXML private CategoryAxis lineChartXAxis;
//    @FXML private NumberAxis lineChartYAxis;
//
//    @FXML private BarChart<String, Number> monthlyBarChart;
//    @FXML private CategoryAxis barChartXAxis;
//    @FXML private NumberAxis barChartYAxis;
//
//    @FXML private PieChart categoryPieChart;
//
//    // Top Products Table
//    @FXML private TableView<ProductSalesData> topProductsTable;
//    @FXML private TableColumn<ProductSalesData, Integer> rankColumn;
//    @FXML private TableColumn<ProductSalesData, String> productNameColumn;
//    @FXML private TableColumn<ProductSalesData, Double> quantitySoldColumn;
//    @FXML private TableColumn<ProductSalesData, String> totalRevenueColumn;
//    @FXML private TableColumn<ProductSalesData, String> percentageColumn;
//    @FXML private Label topProductsInfoLabel;
//
//    // Insights
//    @FXML private TextArea insightsTextArea;
//
//    // Data
//    private ObservableList<Person> customersList;
//    private List<Transaction> currentTransactions;
//    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("ar", "EG"));
//    private DecimalFormat percentFormat = new DecimalFormat("#0.00%");
//
//    /**
//     * Set the content pane reference (called by SidebarController or parent)
//     */
//    public void setContentPane(AnchorPane contentPane) {
//        this.contentPane = contentPane;
//    }
//
//    @FXML
//    public void initialize() {
//
////        // Wire Services
////        this.personService = SpringFXMLLoader.loadController(PersonService.class);
////        this.transactionService = SpringFXMLLoader.loadController(TransactionService.class);
////        this.transactiondetailService = SpringFXMLLoader.loadController(TransactiondetailService.class);
//
//        setupCustomerComboBox();
//        setupTimePeriodComboBox();
//        setupDatePickers();
//        setupTopProductsTable();
//        setupCharts();
//
//        // Load initial data
//        loadCustomers();
//    }
//
//    /**
//     * Setup customer combo box with autocomplete
//     */
//    private void setupCustomerComboBox() {
//        customerComboBox.setConverter(new StringConverter<Person>() {
//            @Override
//            public String toString(Person person) {
//                return person == null ? "" : person.getName();
//            }
//
//            @Override
//            public Person fromString(String string) {
//                return customersList.stream()
//                        .filter(p -> p.getName().equals(string))
//                        .findFirst()
//                        .orElse(null);
//            }
//        });
//    }
//
//    /**
//     * Setup time period combo box
//     */
//    private void setupTimePeriodComboBox() {
//        ObservableList<String> periods = FXCollections.observableArrayList(
//                "Last 7 Days",
//                "Last 30 Days",
//                "Last 3 Months",
//                "Last 6 Months",
//                "Last Year",
//                "Year to Date",
//                "All Time",
//                "Custom Range"
//        );
//        timePeriodComboBox.setItems(periods);
//        timePeriodComboBox.setValue("Last 30 Days");
//
//        // Listen for period changes to auto-update date pickers
//        timePeriodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
//            if (newVal != null && !newVal.equals("Custom Range")) {
//                updateDateRangeFromPeriod(newVal);
//            }
//        });
//    }
//
//    /**
//     * Setup date pickers with default values
//     */
//    private void setupDatePickers() {
//        LocalDate endDate = LocalDate.now();
//        LocalDate startDate = endDate.minusDays(30);
//
//        startDatePicker.setValue(startDate);
//        endDatePicker.setValue(endDate);
//
//        // Limit end date to today
//        endDatePicker.setDayCellFactory(picker -> new DateCell() {
//            @Override
//            public void updateItem(LocalDate date, boolean empty) {
//                super.updateItem(date, empty);
//                setDisable(empty || date.isAfter(LocalDate.now()));
//            }
//        });
//    }
//
//    /**
//     * Update date range based on selected period
//     */
//    private void updateDateRangeFromPeriod(String period) {
//        LocalDate endDate = LocalDate.now();
//        LocalDate startDate;
//
//        switch (period) {
//            case "Last 7 Days":
//                startDate = endDate.minusDays(7);
//                break;
//            case "Last 30 Days":
//                startDate = endDate.minusDays(30);
//                break;
//            case "Last 3 Months":
//                startDate = endDate.minusMonths(3);
//                break;
//            case "Last 6 Months":
//                startDate = endDate.minusMonths(6);
//                break;
//            case "Last Year":
//                startDate = endDate.minusYears(1);
//                break;
//            case "Year to Date":
//                startDate = LocalDate.of(endDate.getYear(), 1, 1);
//                break;
//            case "All Time":
//                startDate = LocalDate.of(2020, 1, 1); // Or earliest transaction date
//                break;
//            default:
//                return; // Custom range - don't update
//        }
//
//        startDatePicker.setValue(startDate);
//        endDatePicker.setValue(endDate);
//    }
//
//    /**
//     * Setup top products table
//     */
//    private void setupTopProductsTable() {
//        rankColumn.setCellValueFactory(cellData ->
//                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRank()));
//
//        productNameColumn.setCellValueFactory(cellData ->
//                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
//
//        quantitySoldColumn.setCellValueFactory(cellData ->
//                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getQuantitySold()));
//
//        totalRevenueColumn.setCellValueFactory(cellData ->
//                new javafx.beans.property.SimpleStringProperty(
//                        currencyFormat.format(cellData.getValue().getTotalRevenue())));
//
//        percentageColumn.setCellValueFactory(cellData ->
//                new javafx.beans.property.SimpleStringProperty(
//                        String.format("%.2f%%", cellData.getValue().getPercentageOfTotal())));
//    }
//
//    /**
//     * Setup chart formatting
//     */
//    private void setupCharts() {
//        // Line chart setup
//        salesLineChart.setAnimated(true);
//        salesLineChart.setCreateSymbols(true);
//
//        // Bar chart setup
//        monthlyBarChart.setAnimated(true);
//
//        // Pie chart setup
//        categoryPieChart.setAnimated(true);
//        categoryPieChart.setLabelLineLength(20);
//    }
//
//    /**
//     * Load customers from database
//     */
//    private void loadCustomers() {
//        try {
//            List<Person> customers = personService.findByType("Customer");
//            customersList = FXCollections.observableArrayList(customers);
//            customerComboBox.setItems(customersList);
//        } catch (Exception e) {
//            showError("Failed to load customers: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Generate report based on filters
//     */
//    @FXML
//    public void onGenerateReport() {
//        try {
//            // Validate inputs
//            Person selectedCustomer = customerComboBox.getValue();
//            LocalDate startDate = startDatePicker.getValue();
//            LocalDate endDate = endDatePicker.getValue();
//
//            if (selectedCustomer == null) {
//                showWarning("Please select a customer");
//                return;
//            }
//
//            if (startDate == null || endDate == null) {
//                showWarning("Please select date range");
//                return;
//            }
//
//            if (startDate.isAfter(endDate)) {
//                showWarning("Start date cannot be after end date");
//                return;
//            }
//
//            // Load transactions for selected customer and date range
//            currentTransactions = loadCustomerTransactions(selectedCustomer, startDate, endDate);
//
//            if (currentTransactions.isEmpty()) {
//                showInfo("No transactions found for the selected period");
//                clearAllData();
//                return;
//            }
//
//            // Generate all reports
//            updateSummaryCards();
//            updateLineChart();
//            updateBarChart();
//            updatePieChart();
//            updateTopProductsTable();
//            generateInsights();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            showError("Error generating report: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Load transactions for customer within date range
//     */
//    private List<Transaction> loadCustomerTransactions(Person customer, LocalDate startDate, LocalDate endDate) {
//        List<Transaction> allTransactions = transactionService.findAllTransactions();
//
//        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
//        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
//
//        return allTransactions.stream()
//                .filter(t -> t.getPerson() != null && t.getPerson().getId().equals(customer.getId()))
//                .filter(t -> t.getTransactionDate() != null)
//                .filter(t -> !t.getTransactionDate().isBefore(startInstant) &&
//                        t.getTransactionDate().isBefore(endInstant))
//                .sorted(Comparator.comparing(Transaction::getTransactionDate))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Update summary cards with statistics
//     */
//    private void updateSummaryCards() {
//        BigDecimal totalSales = currentTransactions.stream()
//                .map(t -> t.getTotalAmount() != null ? t.getTotalAmount() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        int transactionCount = currentTransactions.size();
//
//        BigDecimal avgTransaction = transactionCount > 0
//                ? totalSales.divide(BigDecimal.valueOf(transactionCount), 2, RoundingMode.HALF_UP)
//                : BigDecimal.ZERO;
//
//        // Calculate growth rate (compare to previous period)
//        LocalDate startDate = startDatePicker.getValue();
//        LocalDate endDate = endDatePicker.getValue();
//        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
//
//        LocalDate prevStartDate = startDate.minusDays(daysBetween);
//        LocalDate prevEndDate = startDate.minusDays(1);
//
//        List<Transaction> previousTransactions = loadCustomerTransactions(
//                customerComboBox.getValue(), prevStartDate, prevEndDate);
//
//        BigDecimal previousSales = previousTransactions.stream()
//                .map(t -> t.getTotalAmount() != null ? t.getTotalAmount() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        double growthRate = calculateGrowthRate(totalSales, previousSales);
//
//        // Update labels
//        totalSalesLabel.setText(currencyFormat.format(totalSales));
//        transactionCountLabel.setText(String.valueOf(transactionCount));
//        averageTransactionLabel.setText(currencyFormat.format(avgTransaction));
//        growthRateLabel.setText(String.format("%.2f%%", growthRate));
//
//        // Update change labels
//        updateChangeLabel(totalSalesChangeLabel, totalSales, previousSales);
//        updateChangeLabel(transactionCountChangeLabel, transactionCount, previousTransactions.size());
//
//        growthPeriodLabel.setText(growthRate >= 0 ? "▲ vs previous period" : "▼ vs previous period");
//        growthPeriodLabel.getStyleClass().removeAll("card-change", "positive-change", "negative-change");
//        growthPeriodLabel.getStyleClass().add("card-change");
//        growthPeriodLabel.getStyleClass().add(growthRate >= 0 ? "positive-change" : "negative-change");
//    }
//
//    /**
//     * Calculate growth rate percentage
//     */
//    private double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
//        if (previous.compareTo(BigDecimal.ZERO) == 0) {
//            return current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
//        }
//        return current.subtract(previous)
//                .divide(previous, 4, RoundingMode.HALF_UP)
//                .multiply(BigDecimal.valueOf(100))
//                .doubleValue();
//    }
//
//    /**
//     * Update change label with formatting
//     */
//    private void updateChangeLabel(Label label, Object current, Object previous) {
//        double change;
//        String prefix;
//
//        if (current instanceof BigDecimal && previous instanceof BigDecimal) {
//            change = calculateGrowthRate((BigDecimal) current, (BigDecimal) previous);
//            prefix = currencyFormat.format(((BigDecimal) current).subtract((BigDecimal) previous));
//        } else {
//            int currentInt = (Integer) current;
//            int previousInt = (Integer) previous;
//            change = previousInt == 0 ? 0 : ((currentInt - previousInt) * 100.0 / previousInt);
//            prefix = String.valueOf(currentInt - previousInt);
//        }
//
//        String text = String.format("%s (%.1f%%) from previous period",
//                change >= 0 ? "+" + prefix : prefix, change);
//
//        label.setText(text);
//        label.getStyleClass().removeAll("card-change", "positive-change", "negative-change");
//        label.getStyleClass().add("card-change");
//        label.getStyleClass().add(change >= 0 ? "positive-change" : "negative-change");
//    }
//
//    /**
//     * Update line chart with sales over time
//     */
//    private void updateLineChart() {
//        salesLineChart.getData().clear();
//
//        XYChart.Series<String, Number> series = new XYChart.Series<>();
//        series.setName("Sales Amount");
//
//        // Group transactions by period (daily, weekly, or monthly based on range)
//        Map<String, BigDecimal> periodSales = groupTransactionsByPeriod();
//
//        periodSales.forEach((period, amount) -> {
//            series.getData().add(new XYChart.Data<>(period, amount));
//        });
//
//        salesLineChart.getData().add(series);
//    }
//
//    /**
//     * Group transactions by appropriate time period
//     */
//    private Map<String, BigDecimal> groupTransactionsByPeriod() {
//        Map<String, BigDecimal> result = new LinkedHashMap<>();
//        DateTimeFormatter formatter;
//
//        long daysBetween = ChronoUnit.DAYS.between(
//                startDatePicker.getValue(), endDatePicker.getValue());
//
//        if (daysBetween <= 31) {
//            formatter = DateTimeFormatter.ofPattern("MMM dd");
//        } else if (daysBetween <= 180) {
//            formatter = DateTimeFormatter.ofPattern("MMM yyyy");
//        } else {
//            formatter = DateTimeFormatter.ofPattern("MMM yyyy");
//        }
//
//        for (Transaction transaction : currentTransactions) {
//            LocalDate date = transaction.getTransactionDate()
//                    .atZone(ZoneId.systemDefault()).toLocalDate();
//            String period = date.format(formatter);
//
//            result.merge(period, transaction.getTotalAmount(), BigDecimal::add);
//        }
//
//        return result;
//    }
//
//    /**
//     * Update bar chart with monthly sales
//     */
//    private void updateBarChart() {
//        monthlyBarChart.getData().clear();
//
//        XYChart.Series<String, Number> series = new XYChart.Series<>();
//        series.setName("Monthly Sales");
//
//        Map<String, BigDecimal> monthlySales = groupByMonth();
//
//        monthlySales.forEach((month, amount) -> {
//            series.getData().add(new XYChart.Data<>(month, amount));
//        });
//
//        monthlyBarChart.getData().add(series);
//    }
//
//    /**
//     * Group transactions by month
//     */
//    private Map<String, BigDecimal> groupByMonth() {
//        Map<String, BigDecimal> result = new LinkedHashMap<>();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
//
//        for (Transaction transaction : currentTransactions) {
//            LocalDate date = transaction.getTransactionDate()
//                    .atZone(ZoneId.systemDefault()).toLocalDate();
//            String month = date.format(formatter);
//
//            result.merge(month, transaction.getTotalAmount(), BigDecimal::add);
//        }
//
//        return result;
//    }
//
//    /**
//     * Update pie chart with category distribution
//     */
//    private void updatePieChart() {
//        categoryPieChart.getData().clear();
//
//        // Get all transaction details and group by item
//        Map<String, BigDecimal> itemSales = new HashMap<>();
//
//        for (Transaction transaction : currentTransactions) {
//            List<Transactiondetail> details = transactiondetailService
//                    .findTransactionDetailsByTransactionId(transaction.getId());
//
//            for (Transactiondetail detail : details) {
//                if (detail.getItem() != null) {
//                    String itemName = detail.getItem().getName();
//                    BigDecimal amount = detail.getComulativePrice() != null
//                            ? detail.getComulativePrice() : BigDecimal.ZERO;
//                    itemSales.merge(itemName, amount, BigDecimal::add);
//                }
//            }
//        }
//
//        // Get top 5 items, group rest as "Others"
//        List<Map.Entry<String, BigDecimal>> sortedItems = itemSales.entrySet().stream()
//                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
//                .collect(Collectors.toList());
//
//        BigDecimal othersTotal = BigDecimal.ZERO;
//        int count = 0;
//
//        for (Map.Entry<String, BigDecimal> entry : sortedItems) {
//            if (count < 5) {
//                PieChart.Data slice = new PieChart.Data(
//                        entry.getKey(), entry.getValue().doubleValue());
//                categoryPieChart.getData().add(slice);
//            } else {
//                othersTotal = othersTotal.add(entry.getValue());
//            }
//            count++;
//        }
//
//        if (othersTotal.compareTo(BigDecimal.ZERO) > 0) {
//            PieChart.Data othersSlice = new PieChart.Data("Others", othersTotal.doubleValue());
//            categoryPieChart.getData().add(othersSlice);
//        }
//    }
//
//    /**
//     * Update top products table
//     */
//    private void updateTopProductsTable() {
//        Map<String, ProductSalesData> productMap = new HashMap<>();
//        BigDecimal grandTotal = BigDecimal.ZERO;
//
//        for (Transaction transaction : currentTransactions) {
//            List<Transactiondetail> details = transactiondetailService
//                    .findTransactionDetailsByTransactionId(transaction.getId());
//
//            for (Transactiondetail detail : details) {
//                if (detail.getItem() != null) {
//                    String itemName = detail.getItem().getName();
//                    Double quantity = detail.getQuantity() != null ? detail.getQuantity() : 0.0;
//                    BigDecimal revenue = detail.getComulativePrice() != null
//                            ? detail.getComulativePrice() : BigDecimal.ZERO;
//
//                    productMap.computeIfAbsent(itemName, k -> new ProductSalesData(itemName))
//                            .addSale(quantity, revenue);
//
//                    grandTotal = grandTotal.add(revenue);
//                }
//            }
//        }
//
//        // Calculate percentages and sort
//        final BigDecimal finalGrandTotal = grandTotal;
//        List<ProductSalesData> productList = productMap.values().stream()
//                .peek(p -> p.calculatePercentage(finalGrandTotal))
//                .sorted(Comparator.comparing(ProductSalesData::getTotalRevenue).reversed())
//                .limit(10)
//                .collect(Collectors.toList());
//
//        // Set ranks
//        for (int i = 0; i < productList.size(); i++) {
//            productList.get(i).setRank(i + 1);
//        }
//
//        topProductsTable.setItems(FXCollections.observableArrayList(productList));
//        topProductsInfoLabel.setText(String.format("(Showing top %d products)", productList.size()));
//    }
//
//    /**
//     * Generate insights based on data analysis
//     */
//    private void generateInsights() {
//        StringBuilder insights = new StringBuilder();
//        Person customer = customerComboBox.getValue();
//
//        insights.append(String.format("📊 Analysis for %s\n\n", customer.getName()));
//
//        // Transaction frequency
//        long days = ChronoUnit.DAYS.between(startDatePicker.getValue(), endDatePicker.getValue());
//        double avgTransactionsPerDay = currentTransactions.size() / (double) Math.max(days, 1);
//        insights.append(String.format("• Average %.1f transactions per day\n", avgTransactionsPerDay));
//
//        // Peak month
//        Map<String, BigDecimal> monthlyData = groupByMonth();
//        if (!monthlyData.isEmpty()) {
//            String peakMonth = monthlyData.entrySet().stream()
//                    .max(Map.Entry.comparingByValue())
//                    .map(Map.Entry::getKey)
//                    .orElse("N/A");
//            insights.append(String.format("• Peak sales month: %s\n", peakMonth));
//        }
//
//        // Top product
//        if (!topProductsTable.getItems().isEmpty()) {
//            ProductSalesData topProduct = topProductsTable.getItems().get(0);
//            insights.append(String.format("• Most purchased: %s (%.1f%% of total)\n",
//                    topProduct.getProductName(), topProduct.getPercentageOfTotal()));
//        }
//
//        // Growth trend
//        BigDecimal totalSales = currentTransactions.stream()
//                .map(t -> t.getTotalAmount() != null ? t.getTotalAmount() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        LocalDate prevStart = startDatePicker.getValue().minusDays(days);
//        LocalDate prevEnd = startDatePicker.getValue().minusDays(1);
//        List<Transaction> prevTransactions = loadCustomerTransactions(customer, prevStart, prevEnd);
//        BigDecimal prevSales = prevTransactions.stream()
//                .map(t -> t.getTotalAmount() != null ? t.getTotalAmount() : BigDecimal.ZERO)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        double growth = calculateGrowthRate(totalSales, prevSales);
//        if (growth > 10) {
//            insights.append(String.format("• ⬆️ Strong growth trend: %.1f%% increase\n", growth));
//        } else if (growth < -10) {
//            insights.append(String.format("• ⬇️ Declining trend: %.1f%% decrease\n", growth));
//        } else {
//            insights.append("• ➡️ Stable purchasing pattern\n");
//        }
//
//        insightsTextArea.setText(insights.toString());
//    }
//
//    /**
//     * Clear all data displays
//     */
//    private void clearAllData() {
//        totalSalesLabel.setText("0.00 EGP");
//        transactionCountLabel.setText("0");
//        averageTransactionLabel.setText("0.00 EGP");
//        growthRateLabel.setText("0%");
//
//        totalSalesChangeLabel.setText("--");
//        transactionCountChangeLabel.setText("--");
//        averageChangeLabel.setText("--");
//        growthPeriodLabel.setText("--");
//
//        salesLineChart.getData().clear();
//        monthlyBarChart.getData().clear();
//        categoryPieChart.getData().clear();
//        topProductsTable.getItems().clear();
//        insightsTextArea.clear();
//    }
//
//    /**
//     * Clear filters
//     */
//    @FXML
//    public void onClearFilters() {
//        customerComboBox.setValue(null);
//        timePeriodComboBox.setValue("Last 30 Days");
//        updateDateRangeFromPeriod("Last 30 Days");
//        clearAllData();
//    }
//
//    /**
//     * Print report
//     */
//    @FXML
//    public void onPrintReport() {
//        try {
//            PrinterJob job = PrinterJob.createPrinterJob();
//            if (job != null) {
//                boolean proceed = job.showPrintDialog(salesLineChart.getScene().getWindow());
//                if (proceed) {
//                    // Print the entire content
//                    VBox printContent = (VBox) salesLineChart.getParent().getParent();
//                    boolean success = job.printPage(printContent);
//                    if (success) {
//                        job.endJob();
//                        showInfo("Report sent to printer successfully");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            showError("Print failed: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Navigate back to Customer Reports submenu
//     */
//    @FXML
//    public void onBackToCustomerReports() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CustomerReportsSubMenu.fxml"));
//            Node node = loader.load();
//
//            CustomerReportsSubMenuController controller = loader.getController();
//            controller.setContentPane(contentPane);
//
//            if (contentPane != null) {
//                AnchorPane.setTopAnchor(node, 0.0);
//                AnchorPane.setBottomAnchor(node, 0.0);
//                AnchorPane.setLeftAnchor(node, 0.0);
//                AnchorPane.setRightAnchor(node, 0.0);
//
//                contentPane.getChildren().setAll(node);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            showError("Failed to navigate back");
//        }
//    }
//
//    // Alert helpers
//    private void showError(String message) {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setTitle("Error");
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    private void showWarning(String message) {
//        Alert alert = new Alert(Alert.AlertType.WARNING);
//        alert.setTitle("Warning");
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    private void showInfo(String message) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Information");
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//
//    /**
//     * Inner class for product sales data
//     */
//    public static class ProductSalesData {
//        private int rank;
//        private String productName;
//        private double quantitySold;
//        private BigDecimal totalRevenue;
//        private double percentageOfTotal;
//
//        public ProductSalesData(String productName) {
//            this.productName = productName;
//            this.quantitySold = 0.0;
//            this.totalRevenue = BigDecimal.ZERO;
//        }
//
//        public void addSale(double quantity, BigDecimal revenue) {
//            this.quantitySold += quantity;
//            this.totalRevenue = this.totalRevenue.add(revenue);
//        }
//
//        public void calculatePercentage(BigDecimal grandTotal) {
//            if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
//                this.percentageOfTotal = totalRevenue
//                        .divide(grandTotal, 4, RoundingMode.HALF_UP)
//                        .multiply(BigDecimal.valueOf(100))
//                        .doubleValue();
//            }
//        }
//
//        // Getters and setters
//        public int getRank() { return rank; }
//        public void setRank(int rank) { this.rank = rank; }
//        public String getProductName() { return productName; }
//        public double getQuantitySold() { return quantitySold; }
//        public BigDecimal getTotalRevenue() { return totalRevenue; }
//        public double getPercentageOfTotal() { return percentageOfTotal; }
//    }
//}