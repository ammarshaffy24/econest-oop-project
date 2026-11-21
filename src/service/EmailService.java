package service;

import model.Email;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Tasks 8 & 9: "sends" an email by saving it to data/emails.txt, which the Email Log screen shows.
// Simulated on purpose: no mail library or password is needed to run the project.
public class EmailService {
    private static final File FILE = new File("data", "emails.txt");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String NEW_LINE_MARK = "\\n";

    public Email send(int orderId, String to, String subject, String body) {
        if (to == null || to.isBlank()) throw new IllegalArgumentException("Recipient has no email address.");
        Email email = new Email(LocalDateTime.now().format(TIME), orderId, to, subject, body);
        FILE.getParentFile().mkdirs();
        try (PrintWriter w = new PrintWriter(new FileWriter(FILE, true))) {
            w.println(email.getSentAt() + "|" + orderId + "|" + clean(to) + "|" + clean(subject)
                    + "|" + clean(body).replace("\n", NEW_LINE_MARK));
        } catch (IOException e) {
            throw new IllegalStateException("Could not save email to " + FILE + ": " + e.getMessage(), e);
        }
        return email;
    }

    public List<Email> getAll() {
        List<Email> emails = new ArrayList<>();
        if (!FILE.exists()) return emails;
        try (BufferedReader r = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|", -1);
                emails.add(new Email(p[0], Integer.parseInt(p[1]), p[2], p[3], p[4].replace(NEW_LINE_MARK, "\n")));
            }
        } catch (IOException e) {
            System.err.println("Could not read " + FILE + ": " + e.getMessage());
        }
        return emails;
    }

    private static String clean(String s) {
        return s.replace("|", "/");
    }
}
