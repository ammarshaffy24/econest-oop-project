package econest.model;

// Task 2: a household or office client
public class Client extends Person {
    private String type;     // "Household" or "Office"
    private String address;

    public Client(int id, String name, String phone, String email, String type, String address) {
        super(id, name, phone, email);
        this.type = type;
        this.address = address;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
