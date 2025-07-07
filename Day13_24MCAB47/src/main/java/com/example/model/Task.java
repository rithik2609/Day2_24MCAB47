package com.example.model;

import io.vertx.core.json.JsonObject;

public class Task {
    private String id;
    private String userId;
    private String title;
    private String description;
    private String dueDate;
    private String priority;
    private boolean completed;
    private String createdAt;
    private String updatedAt;

    public Task(String id, String userId, String title, String description, String dueDate,
                String priority, boolean completed, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = completed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("_id", id)
                .put("userId", userId)
                .put("title", title)
                .put("description", description)
                .put("dueDate", dueDate)
                .put("priority", priority)
                .put("completed", completed)
                .put("createdAt", createdAt)
                .put("updatedAt", updatedAt);
    }

    public static Task fromJson(JsonObject json) {
        return new Task(
                json.getString("_id"),
                json.getString("userId"),
                json.getString("title"),
                json.getString("description"),
                json.getString("dueDate"),
                json.getString("priority"),
                json.getBoolean("completed", false),
                json.getString("createdAt"),
                json.getString("updatedAt")
        );
    }
}
