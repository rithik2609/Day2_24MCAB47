package com.example.config;

import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.Properties;

public class MongoConfig {
    public static MongoClient createMongoClient(Vertx vertx) {
        Properties props = ConfigLoader.load();
        String uri = props.getProperty("mongo.uri");
        String dbName = props.getProperty("mongo.db");

        JsonObject config = new JsonObject()
                .put("connection_string", uri)
                .put("db_name", dbName);

        return MongoClient.createShared(vertx, config);
    }
}
