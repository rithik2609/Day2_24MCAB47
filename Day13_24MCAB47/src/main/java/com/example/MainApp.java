package com.example;

import com.example.config.ConfigLoader;
import com.example.config.MongoConfig;
import com.example.config.RedisConfig;
import com.example.config.MailClientConfig;
import com.example.handler.RouteHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.Promise;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.redis.client.RedisAPI;
import io.vertx.ext.mail.MailClient;

import java.util.Properties;

public class MainApp extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        Vertx vertx = Vertx.vertx();

        // ✅ Load application configuration
        Properties config = ConfigLoader.load();

        // ✅ MongoDB
        MongoClient mongoClient = MongoConfig.createMongoClient(vertx);

        // ✅ Redis
        RedisAPI redisAPI = RedisConfig.createRedisClient(vertx);

        // ✅ Mail
        MailClient mailClient = MailClientConfig.createMailClient(vertx);

        // ✅ Deploy RouteHandler
        vertx.deployVerticle(new RouteHandler(mongoClient, redisAPI, mailClient, config));

        System.out.println("✅ To-Do List Application Backend started.");
        startPromise.complete();
    }

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new MainApp());
    }
}
