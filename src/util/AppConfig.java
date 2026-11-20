package util;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not read config.properties in the project folder", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
