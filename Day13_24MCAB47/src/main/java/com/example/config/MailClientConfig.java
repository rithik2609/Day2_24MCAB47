package com.example.config;

import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.StartTLSOptions;

import java.util.Properties;

public class MailClientConfig {
    public static MailClient createMailClient(Vertx vertx) {
        Properties props = ConfigLoader.load();

        MailConfig config = new MailConfig()
                .setHostname(props.getProperty("mail.host"))
                .setPort(Integer.parseInt(props.getProperty("mail.port")))
                .setStarttls(StartTLSOptions.REQUIRED)
                .setUsername(props.getProperty("mail.username"))
                .setPassword(props.getProperty("mail.password"));

        return MailClient.createShared(vertx, config);
    }
}
