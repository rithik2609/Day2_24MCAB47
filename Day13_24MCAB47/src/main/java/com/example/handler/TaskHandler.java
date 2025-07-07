package com.example.handler;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import java.time.Instant;
import java.util.UUID;

public class TaskHandler {

    private final MongoClient mongo;

    public TaskHandler(MongoClient mongo) {
        this.mongo = mongo;
    }

    // ✅ Create Task
    public void createTask(RoutingContext ctx) {
        System.out.println("📥 createTask handler triggered");
        System.out.println("👤 Authenticated user: " + ctx.user().principal());

        JsonObject body = ctx.body().asJsonObject();
        String userId = ctx.user().principal().getString("userId");

        String taskId = UUID.randomUUID().toString();
        String now = Instant.now().toString();

        JsonObject task = new JsonObject()
                .put("_id", taskId)
                .put("userId", userId)
                .put("title", body.getString("title"))
                .put("description", body.getString("description"))
                .put("dueDate", body.getString("dueDate"))
                .put("priority", body.getString("priority"))
                .put("completed", false)
                .put("createdAt", now)
                .put("updatedAt", now);

        mongo.insert("tasks", task, res -> {
            if (res.succeeded()) {
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(task.put("id", taskId).encode());
            } else {
                ctx.response().setStatusCode(500).end("Failed to create task.");
            }
        });
    }

    // ✅ Get All Tasks (with optional filters)
    public void listTasks(RoutingContext ctx) {
        String userId = ctx.user().principal().getString("userId");

        // Query params
        String status = ctx.queryParams().get("completed"); // "true"/"false"
        String priority = ctx.queryParams().get("priority");
        String sort = ctx.queryParams().get("sort"); // "dueDate", "priority", "createdAt"

        JsonObject query = new JsonObject().put("userId", userId);
        if (status != null) query.put("completed", Boolean.parseBoolean(status));
        if (priority != null) query.put("priority", priority);

        JsonObject sortBy = new JsonObject();
        if (sort != null) sortBy.put(sort, 1);

        mongo.findWithOptions("tasks", query, new io.vertx.ext.mongo.FindOptions().setSort(sortBy), res -> {
            if (res.succeeded()) {
                JsonArray result = new JsonArray(res.result());
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(result.encode());
            } else {
                ctx.response().setStatusCode(500).end("Failed to fetch tasks.");
            }
        });
    }

    // ✅ Update Task
    public void updateTask(RoutingContext ctx) {
        String taskId = ctx.pathParam("id");
        String userId = ctx.user().principal().getString("userId");

        JsonObject updates = ctx.body().asJsonObject();
        updates.put("updatedAt", Instant.now().toString());

        mongo.updateCollection(
                "tasks",
                new JsonObject().put("_id", taskId).put("userId", userId),
                new JsonObject().put("$set", updates),
                res -> {
                    if (res.succeeded()) {
                        ctx.response().end("Task updated.");
                    } else {
                        ctx.response().setStatusCode(500).end("Update failed.");
                    }
                });
    }

    // ✅ Delete Task
    public void deleteTask(RoutingContext ctx) {
        String taskId = ctx.pathParam("id");
        String userId = ctx.user().principal().getString("userId");

        mongo.removeDocument("tasks", new JsonObject().put("_id", taskId).put("userId", userId), res -> {
            if (res.succeeded()) {
                ctx.response().end("Task deleted.");
            } else {
                ctx.response().setStatusCode(500).end("Delete failed.");
            }
        });
    }

    // ✅ Toggle Completion
    public void toggleComplete(RoutingContext ctx) {
        String taskId = ctx.pathParam("id");
        String userId = ctx.user().principal().getString("userId");
        JsonObject body = ctx.body().asJsonObject();
        boolean complete = body.getBoolean("completed", true);

        mongo.updateCollection(
                "tasks",
                new JsonObject().put("_id", taskId).put("userId", userId),
                new JsonObject().put("$set", new JsonObject()
                        .put("completed", complete)
                        .put("updatedAt", Instant.now().toString())),
                res -> {
                    if (res.succeeded()) {
                        ctx.response().end("Task status updated.");
                    } else {
                        ctx.response().setStatusCode(500).end("Failed to update status.");
                    }
                });
    }
}
