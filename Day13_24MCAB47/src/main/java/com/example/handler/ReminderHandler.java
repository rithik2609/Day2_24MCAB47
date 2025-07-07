package com.example.handler;

import com.example.util.EmailUtil;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mail.MailClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class ReminderHandler {

    private final MongoClient mongo;
    private final MailClient mail;
    private final Vertx vertx;

    public ReminderHandler(MongoClient mongo, MailClient mail, Vertx vertx) {
        this.mongo = mongo;
        this.mail = mail;
        this.vertx = vertx;
    }

    // ✅ Run reminders every X milliseconds
    public void scheduleReminders(long intervalMs) {
        vertx.setPeriodic(intervalMs, id -> sendReminders());
    }

    // ✅ Find tasks due in next 1 hour and send reminders
    public void sendReminders() {
        Instant now = Instant.now();
        Instant oneHourLater = now.plus(1, ChronoUnit.HOURS);

        JsonObject query = new JsonObject()
                .put("completed", false)
                .put("dueDate", new JsonObject()
                        .put("$gte", now.toString())
                        .put("$lte", oneHourLater.toString()));

        mongo.find("tasks", query, taskRes -> {
            if (taskRes.succeeded()) {
                taskRes.result().forEach(task -> {
                    String userId = task.getString("userId");
                    String title = task.getString("title");
                    String due = task.getString("dueDate");

                    // Fetch user email
                    mongo.findOne("users", new JsonObject().put("_id", userId), null, userRes -> {
                        if (userRes.succeeded() && userRes.result() != null) {
                            String email = userRes.result().getString("email");
                            String subject = "⏰ Task Reminder: " + title;
                            String msg = "<p>Your task <strong>" + title + "</strong> is due by <strong>" + due + "</strong>.</p>";
                            EmailUtil.sendEmailAsync(mail, email, subject, msg);
                        }
                    });
                });
            }
        });
    }
}
