package com.example.handler;

import com.example.util.EmailUtil;
import com.example.util.PasswordUtil;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

public class AuthHandler {
    private final MongoClient client;

    public AuthHandler(MongoClient client) {
        this.client = client;
    }

    // USER REGISTRATION
    public void register(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String name = body.getString("name");
        String email = body.getString("email");
        String password = PasswordUtil.generateRandomPassword();

        JsonObject user = new JsonObject()
                .put("name", name)
                .put("email", email)
                .put("password", password);

        client.save("users", user, res -> {
            if (res.succeeded()) {
                EmailUtil.sendEmail(email, "Your Ticket Password", "Your password is: " + password);
                ctx.response().end("User registered and password emailed.");
            } else {
                ctx.response().setStatusCode(500).end("Registration failed.");
            }
        });
    }

    // USER LOGIN
    public void login(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String email = body.getString("email");
        String password = body.getString("password");

        client.findOne("users", new JsonObject().put("email", email).put("password", password), null, res -> {
            if (res.succeeded() && res.result() != null) {
                ctx.response().end("Login successful.");
            } else {
                ctx.response().setStatusCode(401).end("Invalid credentials.");
            }
        });
    }

    // ADMIN LOGIN
    public void adminLogin(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String email = body.getString("email");
        String password = body.getString("password");

        client.findOne("admins", new JsonObject().put("email", email).put("password", password), null, res -> {
            if (res.succeeded() && res.result() != null) {
                ctx.response().end("Admin login successful.");
            } else {
                ctx.response().setStatusCode(401).end("Invalid admin credentials.");
            }
        });
    }
}
