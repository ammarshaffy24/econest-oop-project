import java.math.BigDecimal;

public class Product {
    private int id;
    private String name;
    private String category;
    private BigDecimal unitPrice;
    private int ecoRating;

    public Product(int id, String name, String category, BigDecimal unitPrice, int ecoRating) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.ecoRating = ecoRating;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getEcoRating() { return ecoRating; }

    @Override
    public String toString() {
        return name;
    }
}