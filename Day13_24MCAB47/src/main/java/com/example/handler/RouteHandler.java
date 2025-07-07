package com.example.handler;

import com.example.util.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.mail.MailClient;
import io.vertx.redis.client.RedisAPI;

import java.util.Properties;

public class RouteHandler extends AbstractVerticle {
    private final MongoClient mongo;
    private final RedisAPI redis;
    private final MailClient mail;
    private final Properties config;

    public RouteHandler(MongoClient mongo, RedisAPI redis, MailClient mail, Properties config) {
        this.mongo = mongo;
        this.redis = redis;
        this.mail = mail;
        this.config = config;
    }

    @Override
    public void start() {
        Router router = Router.router(vertx);

        // Init all routes here
        initRoutes(vertx, router, mongo, redis, mail, config);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8888, http -> {
                    if (http.succeeded()) {
                        System.out.println("✅ HTTP server started on port 8888");
                    } else {
                        System.err.println("❌ Failed to start server: " + http.cause());
                    }
                });
    }

    // Static helper method to register all routes
    public static void initRoutes(Vertx vertx, Router router, MongoClient mongo, RedisAPI redis, MailClient mail, Properties config) {
        router.route().handler(BodyHandler.create());

        // JWT setup
        JWTAuth jwtAuth = JWTAuth.create(vertx, JwtUtil.getJwtOptions());

        // Handlers
        AuthHandler authHandler = new AuthHandler(mongo, redis, mail);
        TaskHandler taskHandler = new TaskHandler(mongo);
        ReminderHandler reminderHandler = new ReminderHandler(mongo, mail, vertx);

        // Public Routes
        router.post("/api/register").handler(authHandler::register);
        router.post("/api/login").handler(authHandler::login);
        router.post("/api/token/refresh").handler(authHandler::refreshToken);
        router.post("/api/reset/init").handler(authHandler::initiateReset);
        router.post("/api/reset/complete").handler(authHandler::completeReset);

        // Manual reminder trigger (dev/debug use)
        router.get("/api/reminders").handler(ctx -> {
            reminderHandler.sendReminders();
            ctx.response().end("Reminders triggered.");
        });

        // JWT-protected routes
        router.route("/api/*").handler(JWTAuthHandler.create(jwtAuth));

        router.post("/api/logout").handler(authHandler::logout);
        router.post("/api/tasks").handler(taskHandler::createTask);
        router.get("/api/tasks").handler(taskHandler::listTasks);
        router.put("/api/tasks/:id").handler(taskHandler::updateTask);
        router.delete("/api/tasks/:id").handler(taskHandler::deleteTask);
        router.put("/api/tasks/:id/toggle").handler(taskHandler::toggleComplete);

        // Static frontend support
        router.route("/*").handler(StaticHandler.create("public"));

        // Reminder schedule every 15 mins
        reminderHandler.scheduleReminders(15 * 60 * 1000);
    }
}
