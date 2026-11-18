package model;

public class GardeningKit extends Product {
    private int itemCount;

    public GardeningKit(String id, String name, double price, int ecoRating, int itemCount){
        super(id, name, price, ecoRating);
        setItemCount(itemCount);
    }
    public int getItemCount() {return itemCount; }

    public void setItemCount(int itemCount) {
        if(itemCount <= 0){
            throw new IllegalArgumentException("A kit must contain at least 1 item.");
        }
        this.itemCount = itemCount;
    }
    @Override
    public  String getCategory() {return  "Gardening kit";}

    @Override
    public String getDetails() { return String.valueOf(itemCount); }

    @Override
    public String getDetailsLabel() { return "No. of items:"; }
}
