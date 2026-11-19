package dao;

import model.DeliveryAgent;

public class DeliveryAgentDAO extends BaseDAO<DeliveryAgent> {
    public DeliveryAgentDAO() { super("agents.txt"); }

    protected String toLine(DeliveryAgent a) {
        return a.getId() + "|" + a.getName() + "|" + a.getPhone() + "|"
             + a.getEmail() + "|" + a.getVehicleType() + "|" + a.getVehicleNo();
    }

    protected DeliveryAgent fromLine(String[] p) {
        return new DeliveryAgent(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4], p[5]);
    }
}
