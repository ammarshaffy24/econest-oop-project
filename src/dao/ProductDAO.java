package dao;

import model.GardeningKit;
import model.Plant;
import model.Planter;
import model.Product;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private static final String FILE_NAME = "products.txt";
    private final List<Product> products = new ArrayList<>();

    public ProductDAO() {
        load();   // read saved products when the app starts
    }

    // ADD
    public void add(Product p) {
        if (findById(p.getId()) != null) {
            throw new IllegalArgumentException("Product ID " + p.getId() + " already exists.");
        }
        products.add(p);
        save();
    }

    // UPDATE: replace the product that has the same id
    public void update(Product updated) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(updated.getId())) {
                products.set(i, updated);
                save();
                return;
            }
        }
        throw new IllegalArgumentException("Product " + updated.getId() + " not found.");
    }

    // REMOVE
    public void remove(String id) {
        boolean removed = products.removeIf(p -> p.getId().equals(id));
        if (!removed) {
            throw new IllegalArgumentException("Product " + id + " not found.");
        }
        save();
    }

    // FIND ONE
    public Product findById(String id) {
        for (Product p : products) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        return null;
    }

    // GET ALL (returns a copy so outside code can't mess up our list)
    public List<Product> getAll() {
        return new ArrayList<>(products);
    }

    // SEARCH by id, name or category
    public List<Product> search(String keyword) {
        String k = keyword.trim().toLowerCase();
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getId().toLowerCase().contains(k)
                    || p.getName().toLowerCase().contains(k)
                    || p.getCategory().toLowerCase().contains(k)) {
                result.add(p);
            }
        }
        return result;
    }

    // Makes the next id: P001, P002, P003...
    public String nextId() {
        int max = 0;
        for (Product p : products) {
            int number = Integer.parseInt(p.getId().substring(1));
            if (number > max) {
                max = number;
            }
        }
        return String.format("P%03d", max + 1);
    }

    // ---------- FILE HANDLING ----------

    // Writes one line per product: category|id|name|price|eco|details
    private void save() {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Product p : products) {
                out.println(p.getCategory() + "|" + p.getId() + "|" + p.getName() + "|"
                        + p.getPrice() + "|" + p.getEcoRating() + "|" + p.getDetails());
            }
        } catch (IOException e) {
            System.out.println("Could not save products: " + e.getMessage());
        }
    }

    // Reads the file and rebuilds the product objects
    private void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;   // first run, nothing saved yet
        }
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|");
                String category = parts[0];
                String id = parts[1];
                String name = parts[2];
                double price = Double.parseDouble(parts[3]);
                int eco = Integer.parseInt(parts[4]);
                String details = parts[5];

                if (category.equalsIgnoreCase("Plant")) {
                    products.add(new Plant(id, name, price, eco, details));
                } else if (category.equalsIgnoreCase("Planter")) {
                    products.add(new Planter(id, name, price, eco, details));
                } else if (category.equalsIgnoreCase("Gardening Kit")) {
                    products.add(new GardeningKit(id, name, price, eco, Integer.parseInt(details)));
                }
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Could not load products: " + e.getMessage());
        }
    }
}