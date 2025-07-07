package com.example.model;

import io.vertx.core.json.JsonObject;

public class User {
    private String id;
    private String name;
    private String email;
    private String password; // Hashed
    private String createdAt;

    public User(String id, String name, String email, String password, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("_id", id)
                .put("name", name)
                .put("email", email)
                .put("password", password)
                .put("createdAt", createdAt);
    }

    public static User fromJson(JsonObject json) {
        return new User(
                json.getString("_id"),
                json.getString("name"),
                json.getString("email"),
                json.getString("password"),
                json.getString("createdAt")
        );
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
