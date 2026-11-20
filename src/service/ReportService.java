package service;

import model.Order;
import model.OrderItem;
import model.StockItem;

import java.util.*;

// Task 7: builds the monthly sales & inventory report by looping over the orders list
public class ReportService {

    public static class ProductSales {
        public final String name;
        public int quantity;
        public double revenue;

        ProductSales(String name) { this.name = name; }
    }

    public static class MonthlyReport {
        public double revenue;
        public int orderCount;
        public int itemsSold;
        public final List<ProductSales> topProducts = new ArrayList<>();
        public final List<StockItem> lowStock = new ArrayList<>();
        public final List<StockItem> stockSummary = new ArrayList<>();
    }

    public MonthlyReport build(List<Order> orders, List<StockItem> stock, int year, int month) {
        MonthlyReport report = new MonthlyReport();
        String prefix = String.format("%04d-%02d", year, month);
        Map<String, ProductSales> sales = new LinkedHashMap<>();

        for (Order o : orders) {
            if (!o.getOrderDate().startsWith(prefix)) continue;
            report.orderCount++;
            report.revenue += o.getTotal();
            report.itemsSold += o.getItemCount();
            for (OrderItem item : o.getItems()) {
                ProductSales s = sales.computeIfAbsent(item.getProductId(), id -> new ProductSales(item.getProductName()));
                s.quantity += item.getQuantity();
                s.revenue += item.getLineTotal();
            }
        }
        report.topProducts.addAll(sales.values());
        report.topProducts.sort((a, b) -> Double.compare(b.revenue, a.revenue));

        report.stockSummary.addAll(stock);
        report.stockSummary.sort(Comparator.comparingInt(StockItem::getQuantityOnHand));
        for (StockItem s : stock) if (s.isLowStock()) report.lowStock.add(s);
        return report;
    }

    public String toText(MonthlyReport r, String monthName, int year) {
        StringBuilder t = new StringBuilder();
        t.append("EcoNest - Sales & Inventory Report - ").append(monthName).append(' ').append(year).append("\n\n");
        t.append(String.format("Total Revenue : LKR %,.2f%n", r.revenue));
        t.append("Orders        : ").append(r.orderCount).append('\n');
        t.append("Items Sold    : ").append(r.itemsSold).append('\n');
        t.append("Low-Stock     : ").append(r.lowStock.size()).append("\n\n");

        t.append("Top Products\n");
        for (ProductSales s : r.topProducts)
            t.append(String.format("  %-30s %5d  %,12.2f%n", s.name, s.quantity, s.revenue));

        t.append("\nStock Summary\n");
        for (StockItem s : r.stockSummary)
            t.append(String.format("  %-30s on hand %4d  reorder at %4d%s%n", s.getProductName(),
                    s.getQuantityOnHand(), s.getReorderLevel(), s.isLowStock() ? "  LOW" : ""));
        return t.toString();
    }
}
