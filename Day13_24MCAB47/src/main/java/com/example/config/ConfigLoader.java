package com.example.config;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    public static Properties load() {
        Properties props = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                System.err.println("❌ config.properties not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return props;
    }
}
