package model;

public class StockItem {

    private String productId;
    private String productName;
    private int quantityOnHand;
    private int reorderLevel;
    private String supplier;
    private String lastStockIn;   // date text, e.g. 2026-11-19

    public StockItem(String productId, String productName, int quantityOnHand,
                     int reorderLevel, String supplier, String lastStockIn) {
        this.productId = productId;
        this.productName = productName;
        this.quantityOnHand = quantityOnHand;
        this.reorderLevel = reorderLevel;
        this.supplier = supplier;
        this.lastStockIn = lastStockIn;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantityOnHand() { return quantityOnHand; }
    public int getReorderLevel() { return reorderLevel; }
    public String getSupplier() { return supplier; }
    public String getLastStockIn() { return lastStockIn; }

    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public void setLastStockIn(String lastStockIn) { this.lastStockIn = lastStockIn; }


    public boolean isLowStock() {
        return quantityOnHand <= reorderLevel;
    }


    public String toFileString() {
        return clean(productId) + "|" + clean(productName) + "|" + quantityOnHand + "|"
                + reorderLevel + "|" + clean(supplier) + "|" + clean(lastStockIn);
    }

    public static StockItem fromFileString(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 6) {
            return null;
        }
        try {
            return new StockItem(p[0], p[1], Integer.parseInt(p[2]),
                    Integer.parseInt(p[3]), p[4], p[5]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String clean(String s) {
        return s == null ? "" : s.replace("|", "/").replace("\n", " ");
    }
}
