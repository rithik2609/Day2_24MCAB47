package com.example.config;

import io.vertx.core.Vertx;
import io.vertx.redis.client.*;

import java.util.Properties;

public class RedisConfig {
    public static RedisAPI createRedisClient(Vertx vertx) {
        Properties props = ConfigLoader.load();
        String host = props.getProperty("redis.host");
        int port = Integer.parseInt(props.getProperty("redis.port"));

        RedisOptions options = new RedisOptions()
                .setConnectionString("redis://" + host + ":" + port);

        Redis client = Redis.createClient(vertx, options);
        return RedisAPI.api(client);
    }
}
