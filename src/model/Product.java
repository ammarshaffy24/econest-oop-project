package model;

public abstract class Product {
    private String id;
    private String name;
    private double price;
    private int ecoRating;

    public Product(String id, String name, double price, int ecoRating){
        this.id = id;
        setName(name);
        setPrice(price);
        setEcoRating(ecoRating);
    }

    public String getId () {return id; }
    public String getName() {return name; }
    public double getPrice() {return price; }
    public int getEcoRating() {return  ecoRating; }

    public void setName(String name){
        if(name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        this.name = name.trim().replace("|", "/");
    }

    public void setPrice(double price) {
        if (price <= 0){
            throw new IllegalArgumentException("price must be greater than 0.");
        }
        this.price = price;
    }

    public void setEcoRating(int ecoRating) {
        if (ecoRating < 1 || ecoRating > 5) {
            throw new IllegalArgumentException("echo rating must be between 1 and 5.");
        }
        this.ecoRating = ecoRating;
    }

    public abstract String getCategory();
    public abstract String getDetails();
    public abstract String getDetailsLabel();
}


