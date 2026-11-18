package model;

public class Plant extends Product {
    private  String lightLevel;

    public Plant(String id, String name, double price, int ecoRating, String lightLevel){
        super(id, name, price, ecoRating);
        this.lightLevel = lightLevel;
    }

    public String getLightLevel() {return lightLevel; }

    @Override
    public  String getCategory() {return "plant"; }

    @Override
    public String getDetails() { return lightLevel; }

    @Override
    public String getDetailsLabel() { return "Light level:"; }
}
