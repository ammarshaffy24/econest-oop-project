package util;

// Returns an error message, or null if everything is valid
public class Validator {
    public static String check(String name, String phone, String email, String... others) {
        if (name.trim().isEmpty()) return "Name is required.";
        if (!phone.matches("0\\d{9}")) return "Phone must be 10 digits, e.g. 0771234567.";
        if (!email.matches("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+")) return "Email is not valid.";
        for (String o : others) {
            if (o.trim().isEmpty()) return "Please fill in all fields.";
            if (o.contains("|")) return "The | character is not allowed.";
        }
        if (name.contains("|")) return "The | character is not allowed.";
        return null;
    }
}
