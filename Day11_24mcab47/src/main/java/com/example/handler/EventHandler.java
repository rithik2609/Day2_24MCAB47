package com.example.handler;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

public class EventHandler {
    private final MongoClient client;

    public EventHandler(MongoClient client) {
        this.client = client;
    }

    // LIST EVENTS
    public void listEvents(RoutingContext ctx) {
        client.find("events", new JsonObject(), res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonArray(res.result()).encode());
            } else {
                ctx.response().setStatusCode(500).end("Failed to fetch events.");
            }
        });
    }

    // CREATE EVENT (admin)
    public void createEvent(RoutingContext ctx) {
        JsonObject event = ctx.getBodyAsJson();
        client.save("events", event, res -> {
            if (res.succeeded()) {
                ctx.response().end("Event created.");
            } else {
                ctx.response().setStatusCode(500).end("Failed to create event.");
            }
        });
    }

    // UPDATE EVENT (admin)
    public void updateEvent(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        JsonObject update = ctx.getBodyAsJson();

        client.updateCollection("events",
                new JsonObject().put("_id", id),
                new JsonObject().put("$set", update),
                res -> {
                    if (res.succeeded()) {
                        ctx.response().end("Event updated.");
                    } else {
                        ctx.response().setStatusCode(500).end("Update failed.");
                    }
                });
    }

    // DELETE EVENT (admin)
    public void deleteEvent(RoutingContext ctx) {
        String id = ctx.pathParam("id");

        client.removeDocument("events", new JsonObject().put("_id", id), res -> {
            if (res.succeeded()) {
                ctx.response().end("Event deleted.");
            } else {
                ctx.response().setStatusCode(500).end("Failed to delete.");
            }
        });
    }
}
