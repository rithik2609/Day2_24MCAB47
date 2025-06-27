package in.edu.kristujayanti;

import in.edu.kristujayanti.handlers.OrderHandler;
import in.edu.kristujayanti.services.OrderService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class Main extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Main());
    }

    @Override
    public void start() {
        // Create the router
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // MongoDB configuration
        JsonObject config = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "ecommerce");

        // Create MongoDB client
        MongoClient client = MongoClient.createShared(vertx, config);

        // Initialize Service and Handler
        OrderService service = new OrderService(client);
        OrderHandler handler = new OrderHandler(service);

        // Define Routes
        router.post("/orders").handler(handler::placeOrder);
        router.put("/orders/:id").handler(handler::updateStatus);
        router.get("/orders/user/:userId").handler(handler::getOrdersByUser);
        router.get("/orders/sales/aggregate").handler(handler::getSalesAggregate);

        // ✅ Health check endpoint
        router.get("/health").handler(ctx -> {
            ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end("{\"status\":\"ok\"}");
        });

        // Serve static files
        router.route("/*").handler(StaticHandler.create("src/main/resources/public"));

        // Start the server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888)
                .onSuccess(server -> System.out.println("✅ Server running on http://localhost:8888"))
                .onFailure(err -> System.err.println("❌ Failed to start server: " + err.getMessage()));
    }
}
