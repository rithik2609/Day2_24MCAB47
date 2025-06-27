package in.edu.kristujayanti.model;

import io.vertx.core.json.JsonObject;

public class Order {
    public String id;
    public String userId;
    public String productId;
    public int quantity;
    public String status;
    public String timestamp;

    public Order() {}

    public Order(JsonObject json) {
        this.id = json.getString("_id");
        this.userId = json.getString("userId");
        this.productId = json.getString("productId");
        this.quantity = json.getInteger("quantity");
        this.status = json.getString("status");
        this.timestamp = json.getString("timestamp");
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject()
                .put("userId", userId)
                .put("productId", productId)
                .put("quantity", quantity)
                .put("status", status)
                .put("timestamp", timestamp);
        if (id != null) json.put("_id", id);
        return json;
    }
}