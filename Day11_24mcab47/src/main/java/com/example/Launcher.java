package com.example;

import io.vertx.core.Vertx;

public class Launcher {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle(), res -> {
            if (res.succeeded()) {
                System.out.println("✅ ✅ Server started at http://localhost:8888/");
            } else {
                System.out.println("❌ Failed to start: " + res.cause());
            }
        });
    }
}
