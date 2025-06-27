package in.edu.kristujayanti.services;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.core.Promise;


import java.util.ArrayList;
import java.util.List;

public class OrderService {

    private final MongoClient mongo;

    public OrderService(MongoClient mongo) {
        this.mongo = mongo;
    }

    // Insert a new order document
    public Future<JsonObject> placeOrder(JsonObject order) {
        Promise<JsonObject> promise = Promise.promise();
        mongo.insert("orders", order, res -> {
            if (res.succeeded()) {
                order.put("_id", res.result());
                promise.complete(order);
            } else {
                promise.fail(res.cause());
            }
        });
        return promise.future();
    }

    // Update order status by _id
    public Future<JsonObject> updateStatus(String id, JsonObject updateData) {
        Promise<JsonObject> promise = Promise.promise();
        JsonObject query = new JsonObject().put("_id", id);
        JsonObject update = new JsonObject().put("$set", updateData);

        mongo.findOneAndUpdate("orders", query, update, res -> {
            if (res.succeeded()) {
                promise.complete(res.result());
            } else {
                promise.fail(res.cause());
            }
        });
        return promise.future();
    }

    // Get all orders by userId
    public Future<List<JsonObject>> getOrdersByUser(String userId) {
        Promise<List<JsonObject>> promise = Promise.promise();
        JsonObject query = new JsonObject().put("userId", userId);

        mongo.find("orders", query, res -> {
            if (res.succeeded()) {
                promise.complete(res.result());
            } else {
                promise.fail(res.cause());
            }
        });

        return promise.future();
    }

    // Perform MongoDB aggregation to compute total quantity/count by productId
    public Future<List<JsonObject>> getSalesAggregate() {
        Promise<List<JsonObject>> promise = Promise.promise();

        JsonArray pipeline = new JsonArray()
                .add(new JsonObject()
                        .put("$group", new JsonObject()
                                .put("_id", "$productId")
                                .put("quantity", new JsonObject().put("$sum", "$quantity"))
                                .put("count", new JsonObject().put("$sum", 1))
                        ));

        List<JsonObject> resultList = new ArrayList<>();

        mongo.aggregate("orders", pipeline)
                .handler(resultList::add)
                .endHandler(v -> promise.complete(resultList))
                .exceptionHandler(promise::fail);

        return promise.future();
    }
}
