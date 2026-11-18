package model;

public class Planter extends Product {
    private String material;

    public Planter(String id, String name, double price, int ecoRating, String material){
        super(id, name, price, ecoRating);
        this.material = material;
    }
    public String getMaterial() {return material; }

    @Override
    public String getCategory() {return  "Planter"; }

    @Override
    public String getDetails() { return material; }

    @Override
    public String getDetailsLabel() { return "Material:"; }
}
