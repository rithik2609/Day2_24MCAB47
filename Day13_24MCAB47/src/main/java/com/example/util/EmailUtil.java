package com.example.util;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailMessage;

public class EmailUtil {

    public static void sendEmailAsync(MailClient mailClient, String to, String subject, String content) {
        MailMessage message = new MailMessage()
                .setFrom("Todo App <noreply@todoapp.com>")
                .setTo(to)
                .setSubject(subject)
                .setHtml(content);

        mailClient.sendMail(message, result -> {
            if (result.succeeded()) {
                System.out.println("✅ Email sent to " + to);
            } else {
                result.cause().printStackTrace();
                System.err.println("❌ Failed to send email to " + to);
            }
        });
    }
}
