package in.edu.kristujayanti.handlers;

import in.edu.kristujayanti.services.OrderService;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class OrderHandler {

    private final OrderService service;

    public OrderHandler(OrderService service) {
        this.service = service;
    }

    // POST /orders
    public void placeOrder(RoutingContext ctx) {
        service.placeOrder(ctx.getBodyAsJson())
                .onSuccess(result -> ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .setStatusCode(201)
                        .end(Json.encodePrettily(result)))
                .onFailure(err -> ctx.response()
                        .setStatusCode(500)
                        .end(Json.encodePrettily(new ErrorResponse("Failed to place order", err.getMessage()))));
    }

    // PUT /orders/:id
    public void updateStatus(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        service.updateStatus(id, ctx.getBodyAsJson())
                .onSuccess(result -> ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(Json.encodePrettily(result)))
                .onFailure(err -> ctx.response()
                        .setStatusCode(500)
                        .end(Json.encodePrettily(new ErrorResponse("Failed to update status", err.getMessage()))));
    }

    // GET /orders/user/:userId
    public void getOrdersByUser(RoutingContext ctx) {
        String userId = ctx.pathParam("userId");
        service.getOrdersByUser(userId)
                .onSuccess(result -> ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(Json.encodePrettily(result)))
                .onFailure(err -> ctx.response()
                        .setStatusCode(500)
                        .end(Json.encodePrettily(new ErrorResponse("Failed to fetch orders", err.getMessage()))));
    }

    // GET /orders/sales/aggregate
    public void getSalesAggregate(RoutingContext ctx) {
        service.getSalesAggregate()
                .onSuccess(result -> ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(Json.encodePrettily(result)))
                .onFailure(err -> ctx.response()
                        .setStatusCode(500)
                        .end(Json.encodePrettily(new ErrorResponse("Aggregation failed", err.getMessage()))));
    }

    // Error response formatter
    static class ErrorResponse {
        public String message;
        public String cause;

        public ErrorResponse(String message, String cause) {
            this.message = message;
            this.cause = cause;
        }
    }
}
