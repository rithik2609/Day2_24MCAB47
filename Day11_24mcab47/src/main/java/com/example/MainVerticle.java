package com.example;

import com.example.handler.*;
import io.vertx.core.*;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) {
        MongoClient client = MongoClient.createShared(vertx,
                new io.vertx.core.json.JsonObject()
                        .put("connection_string", "mongodb://localhost:27017")
                        .put("db_name", "eventdb"));

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // USER ROUTES
        router.post("/register").handler(new AuthHandler(client)::register);
        router.post("/login").handler(new AuthHandler(client)::login);
        router.get("/events").handler(new EventHandler(client)::listEvents);
        router.post("/book").handler(new BookingHandler(client)::bookToken);

        // ADMIN ROUTES
        router.post("/admin/login").handler(new AuthHandler(client)::adminLogin);
        router.post("/admin/event").handler(new EventHandler(client)::createEvent);
        router.put("/admin/event/:id").handler(new EventHandler(client)::updateEvent);
        router.delete("/admin/event/:id").handler(new EventHandler(client)::deleteEvent);
        router.get("/admin/bookings").handler(new BookingHandler(client)::getAllBookings);
        router.delete("/admin/booking/:token").handler(new BookingHandler(client)::deleteBooking);
        router.get("/").handler(ctx -> {
            ctx.response()
                    .putHeader("Location", "/index.html")
                    .setStatusCode(302)
                    .end();
        });
        router.route("/*").handler(StaticHandler.create("webroot"));
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888)
                .onSuccess(server -> startPromise.complete())
                .onFailure(startPromise::fail);
    }
}
