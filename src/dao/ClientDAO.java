package dao;

import model.Client;

public class ClientDAO extends BaseDAO<Client> {
    public ClientDAO() { super("clients.txt"); }

    protected String toLine(Client c) {
        return c.getId() + "|" + c.getName() + "|" + c.getPhone() + "|"
             + c.getEmail() + "|" + c.getType() + "|" + c.getAddress();
    }

    protected Client fromLine(String[] p) {
        return new Client(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4], p[5]);
    }
}
