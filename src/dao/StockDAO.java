package dao;

import model.StockItem;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    private static final String FILE_NAME = "stock.txt";

    private final List<StockItem> items = new ArrayList<>();

    public StockDAO() {
        load();
    }

    // ---------- queries ----------

    public List<StockItem> getAll() {
        return new ArrayList<>(items);
    }

    public StockItem findByProductId(String productId) {
        for (StockItem s : items) {
            if (s.getProductId().equals(productId)) {
                return s;
            }
        }
        return null;
    }

    public List<StockItem> getLowStockItems() {
        List<StockItem> low = new ArrayList<>();
        for (StockItem s : items) {
            if (s.isLowStock()) {
                low.add(s);
            }
        }
        return low;
    }

    // ---------- changes ----------


    public void ensureRecord(String productId, String productName) {
        StockItem s = findByProductId(productId);
        if (s == null) {
            items.add(new StockItem(productId, productName, 0, 5, "", ""));
            save();
        } else if (!s.getProductName().equals(productName)) {
            s.setProductName(productName);   // product was renamed in Task 1
            save();
        }
    }


    public void removeMissing(List<String> validProductIds) {
        if (items.removeIf(s -> !validProductIds.contains(s.getProductId()))) {
            save();
        }
    }


    public boolean stockIn(String productId, int qty, String supplier) {
        StockItem s = findByProductId(productId);
        if (s == null || qty <= 0) {
            return false;
        }
        s.setQuantityOnHand(s.getQuantityOnHand() + qty);
        if (supplier != null && !supplier.trim().isEmpty()) {
            s.setSupplier(supplier.trim());
        }
        s.setLastStockIn(LocalDate.now().toString());
        save();
        return true;
    }

    public boolean updateReorderSettings(String productId, int reorderLevel, String supplier) {
        StockItem s = findByProductId(productId);
        if (s == null || reorderLevel < 0) {
            return false;
        }
        s.setReorderLevel(reorderLevel);
        if (supplier != null && !supplier.trim().isEmpty()) {
            s.setSupplier(supplier.trim());
        }
        save();
        return true;
    }

    public boolean reduceStock(String productId, int qty) {
        StockItem s = findByProductId(productId);
        if (s == null || qty <= 0 || s.getQuantityOnHand() < qty) {
            return false;
        }
        s.setQuantityOnHand(s.getQuantityOnHand() - qty);
        save();
        return true;
    }

    // ---------- file handling ----------

    private void load() {
        File f = new File(FILE_NAME);
        if (!f.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                StockItem s = StockItem.fromFileString(line);
                if (s != null) {
                    items.add(s);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read " + FILE_NAME + ": " + e.getMessage());
        }
    }

    private void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (StockItem s : items) {
                pw.println(s.toFileString());
            }
        } catch (IOException e) {
            System.err.println("Could not write " + FILE_NAME + ": " + e.getMessage());
        }
    }
}
