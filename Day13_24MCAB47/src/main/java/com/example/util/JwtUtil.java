package com.example.util;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.JWTOptions;

public class JwtUtil {
    private static final String SECRET = "verysecretkey123";

    public static JWTAuthOptions getJwtOptions() {
        return new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer(SECRET));
    }

    // 🔐 Generate short-lived Access Token (15 min)
    public static String createAccessToken(Vertx vertx, String userId, String email) {
        JWTAuth jwt = JWTAuth.create(vertx, getJwtOptions());
        return jwt.generateToken(
                new JsonObject()
                        .put("userId", userId)
                        .put("email", email),
                new JWTOptions().setExpiresInMinutes(15)
        );
    }

    // 🔁 Generate long-lived Refresh Token (7 days)
    public static String createRefreshToken(Vertx vertx, String userId) {
        JWTAuth jwt = JWTAuth.create(vertx, getJwtOptions());
        return jwt.generateToken(
                new JsonObject().put("userId", userId),
                new JWTOptions().setExpiresInMinutes(60 * 24 * 7)  // 7 days
        );
    }
}
