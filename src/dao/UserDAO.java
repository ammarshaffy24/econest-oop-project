package dao;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private static final String FILE_NAME = "users.txt";
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    // username -> password
    private final Map<String, String> users = new HashMap<>();

    public UserDAO() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            createDefaultFile();   // first run: make sure someone can log in
        }
        load();
    }

    public boolean isValidLogin(String username, String password) {
        String saved = users.get(username.trim());
        return saved != null && saved.equals(password);
    }

    // ---------- FILE HANDLING ----------

    // One line per user: username|password
    private void createDefaultFile() {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME))) {
            out.println(DEFAULT_USERNAME + "|" + DEFAULT_PASSWORD);
        } catch (IOException e) {
            System.out.println("Could not create users file: " + e.getMessage());
        }
    }

    private void load() {
        try (BufferedReader in = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load users: " + e.getMessage());
        }
    }
}
