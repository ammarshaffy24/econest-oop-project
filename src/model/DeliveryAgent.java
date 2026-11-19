package model;

// Task 4: a delivery person and their vehicle
public class DeliveryAgent extends Person {
    private String vehicleType;
    private String vehicleNo;

    public DeliveryAgent(int id, String name, String phone, String email,
                         String vehicleType, String vehicleNo) {
        super(id, name, phone, email);
        this.vehicleType = vehicleType;
        this.vehicleNo = vehicleNo;
    }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }
}
