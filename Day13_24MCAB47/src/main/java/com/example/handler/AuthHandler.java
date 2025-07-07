package com.example.handler;

import com.example.util.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.RedisAPI;
import io.vertx.ext.mail.MailClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AuthHandler {

    private final MongoClient mongo;
    private final RedisAPI redis;
    private final MailClient mail;

    public AuthHandler(MongoClient mongo, RedisAPI redis, MailClient mail) {
        this.mongo = mongo;
        this.redis = redis;
        this.mail = mail;
    }

    // ✅ Registration
    public void register(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");
        String name = body.getString("name");

        if (email == null || name == null) {
            ctx.response().setStatusCode(400).end("Email and name are required.");
            return;
        }

        mongo.findOne("users", new JsonObject().put("email", email), null, res -> {
            if (res.succeeded() && res.result() != null) {
                ctx.response().setStatusCode(409).end("User already exists.");
            } else {
                String rawPassword = TokenGenerator.generatePassword(12);
                String hashedPassword = PasswordUtil.hashPassword(rawPassword);
                String userId = UUID.randomUUID().toString();

                JsonObject user = new JsonObject()
                        .put("_id", userId)
                        .put("email", email)
                        .put("name", name)
                        .put("password", hashedPassword)
                        .put("createdAt", Instant.now().toString());

                mongo.insert("users", user, insertRes -> {
                    if (insertRes.succeeded()) {
                        String subject = "Welcome to To-Do App!";
                        String msg = "<h3>Hello " + name + ",</h3>"
                                + "<p>Your account has been created.</p>"
                                + "<p><strong>Password:</strong> " + rawPassword + "</p>";
                        EmailUtil.sendEmailAsync(mail, email, subject, msg);
                        ctx.response().end("User registered and password emailed.");
                    } else {
                        ctx.response().setStatusCode(500).end("Registration failed.");
                    }
                });
            }
        });
    }

    // ✅ Login
    public void login(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");
        String password = body.getString("password");

        if (email == null || password == null) {
            ctx.response().setStatusCode(400).end("Email and password are required.");
            return;
        }

        mongo.findOne("users", new JsonObject().put("email", email), null, res -> {
            if (res.succeeded() && res.result() != null) {
                JsonObject user = res.result();
                String hash = user.getString("password");
                boolean valid = PasswordUtil.verifyPassword(password, hash);

                if (valid) {
                    String userId = user.getString("_id");

                    // ✅ Create access + refresh tokens
                    String accessToken = JwtUtil.createAccessToken(ctx.vertx(), userId, email);
                    String refreshToken = JwtUtil.createRefreshToken(ctx.vertx(), userId);

                    // ✅ Store refresh token in Redis (key: refresh:<userId>)
                    redis.setex("refresh:" + userId, String.valueOf(60 * 60 * 24 * 7), refreshToken, refreshRes -> {
                        if (refreshRes.succeeded()) {
                            // ✅ Optionally store access token too (optional)
                            redis.setex("jwt:" + userId, "900", accessToken, jwtRes -> {
                                JsonObject response = new JsonObject()
                                        .put("accessToken", accessToken)
                                        .put("refreshToken", refreshToken);
                                ctx.response()
                                        .putHeader("Content-Type", "application/json")
                                        .end(response.encode());
                            });
                        } else {
                            ctx.response().setStatusCode(500).end("Token storage failed.");
                        }
                    });

                } else {
                    ctx.response().setStatusCode(401).end("Invalid password.");
                }
            } else {
                ctx.response().setStatusCode(404).end("User not found.");
            }
        });
    }

    public void refreshToken(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String refreshToken = body.getString("refreshToken");

        if (refreshToken == null) {
            ctx.response().setStatusCode(400).end("Refresh token required.");
            return;
        }

        JWTAuth jwtAuth = JWTAuth.create(ctx.vertx(), JwtUtil.getJwtOptions());

        jwtAuth.authenticate(new JsonObject().put("token", refreshToken), authRes -> {
            if (authRes.succeeded()) {
                JsonObject payload = authRes.result().principal();
                String userId = payload.getString("userId");

                // ✅ Check if this refresh token matches Redis
                redis.get("refresh:" + userId, redisRes -> {
                    if (redisRes.succeeded() && redisRes.result() != null) {
                        String storedToken = redisRes.result().toString();

                        if (storedToken.equals(refreshToken)) {
                            String newAccessToken = JwtUtil.createAccessToken(ctx.vertx(), userId, payload.getString("email"));

                            redis.setex("jwt:" + userId, "900", newAccessToken, jwtSetRes -> {
                                ctx.response()
                                        .putHeader("Content-Type", "application/json")
                                        .end(new JsonObject().put("accessToken", newAccessToken).encode());
                            });
                        } else {
                            ctx.response().setStatusCode(403).end("Invalid refresh token.");
                        }
                    } else {
                        ctx.response().setStatusCode(401).end("Refresh token not found or expired.");
                    }
                });

            } else {
                ctx.response().setStatusCode(401).end("Invalid or expired refresh token.");
            }
        });
    }


    // ✅ Logout
    // ✅ Logout
    public void logout(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String refreshToken = body.getString("refreshToken");

        if (refreshToken == null) {
            ctx.response().setStatusCode(400).end("Refresh token required.");
            return;
        }

        JWTAuth jwtAuth = JWTAuth.create(ctx.vertx(), JwtUtil.getJwtOptions());

        jwtAuth.authenticate(new JsonObject().put("token", refreshToken), authRes -> {
            if (authRes.succeeded()) {
                JsonObject payload = authRes.result().principal();
                String userId = payload.getString("userId");

                // ✅ Delete refresh and access token from Redis
                redis.del(List.of("refresh:" + userId, "jwt:" + userId), delRes -> {
                    ctx.response().end("Logged out successfully.");
                });
            } else {
                ctx.response().setStatusCode(401).end("Invalid refresh token.");
            }
        });
    }


    // ✅ Initiate password reset
    public void initiateReset(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");

        if (email == null) {
            ctx.response().setStatusCode(400).end("Email required.");
            return;
        }

        mongo.findOne("users", new JsonObject().put("email", email), null, res -> {
            if (res.succeeded() && res.result() != null) {
                String token = TokenGenerator.generateSecureToken(24);
                redis.setex("reset:" + token, "600", email, r -> {
                    String link = "http://localhost:5500/reset.html?token=" + token;
                    String subject = "Password Reset Link";
                    String msg = "<p>Click to reset password: <a href=\"" + link + "\">Reset Password</a></p>";
                    EmailUtil.sendEmailAsync(mail, email, subject, msg);
                    ctx.response().end("Reset link sent to email.");
                });

            } else {
                ctx.response().setStatusCode(404).end("Email not found.");
            }
        });
    }

    // ✅ Complete password reset
    public void completeReset(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String token = body.getString("token");
        String newPass = body.getString("newPassword");

        if (token == null || newPass == null) {
            ctx.response().setStatusCode(400).end("Token and new password required.");
            return;
        }

        redis.get("reset:" + token, redisRes -> {
            if (redisRes.succeeded() && redisRes.result() != null) {
                String email = redisRes.result().toString();

                String hashed = PasswordUtil.hashPassword(newPass);
                mongo.updateCollection("users",
                        new JsonObject().put("email", email),
                        new JsonObject().put("$set", new JsonObject().put("password", hashed)),
                        updateRes -> {
                            redis.del(List.of("reset:" + token));
                            ctx.response().end("Password reset successful.");
                        });
            } else {
                ctx.response().setStatusCode(400).end("Invalid or expired token.");
            }
        });
    }
}
