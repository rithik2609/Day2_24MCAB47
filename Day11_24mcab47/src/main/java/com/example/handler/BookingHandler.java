package com.example.handler;

import com.example.util.EmailUtil;
import com.example.util.QRCodeUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class BookingHandler {
    private final MongoClient client;

    public BookingHandler(MongoClient client) {
        this.client = client;
    }

    // BOOK TOKEN
    public void bookToken(RoutingContext ctx) {
        JsonObject body = ctx.getBodyAsJson();
        String userEmail = body.getString("email");
        String eventId = body.getString("eventId");

        client.findOne("events", new JsonObject().put("_id", eventId), null, eventRes -> {
            if (eventRes.succeeded() && eventRes.result() != null) {
                JsonObject event = eventRes.result();
                int tokensAvailable = event.getInteger("tokensAvailable");

                if (tokensAvailable > 0) {
                    String token = UUID.randomUUID().toString();

                    JsonObject booking = new JsonObject()
                            .put("userEmail", userEmail)
                            .put("eventId", eventId)
                            .put("token", token);

                    client.save("bookings", booking, saveRes -> {
                        if (saveRes.succeeded()) {
                            // Reduce available tokens
                            client.updateCollection("events",
                                    new JsonObject().put("_id", eventId),
                                    new JsonObject().put("$set", new JsonObject().put("tokensAvailable", tokensAvailable - 1)),
                                    updateRes -> {
                                        if (updateRes.succeeded()) {
                                            // ✅ Generate QR code and send as HTML email
                                            String qrBase64 = QRCodeUtil.generateBase64QRCode(token);

                                            String html = "<h3>🎫 Your Booking Token</h3>" +
                                                    "<p><b>Token:</b> " + token + "</p>" +
                                                    "<img src='data:image/png;base64," + qrBase64 + "' alt='QR Code'><br>" +
                                                    "<p>Event: <b>" + event.getString("title") + "</b></p>";

                                            EmailUtil.sendEmail(userEmail,
                                                    "Your Event Booking Token",
                                                    html);

                                            ctx.response().end("Booking successful. Token + QR code sent.");
                                        } else {
                                            ctx.response().setStatusCode(500).end("Token booked but count update failed.");
                                        }
                                    });
                        } else {
                            ctx.response().setStatusCode(500).end("Booking failed.");
                        }
                    });
                } else {
                    ctx.response().setStatusCode(400).end("No tokens left for this event.");
                }
            } else {
                ctx.response().setStatusCode(404).end("Event not found.");
            }
        });
    }

    // VIEW ALL BOOKINGS (admin)
    public void getAllBookings(RoutingContext ctx) {
        client.find("bookings", new JsonObject(), res -> {
            if (res.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json")
                        .end(new JsonArray(res.result()).encode());
            } else {
                ctx.response().setStatusCode(500).end("Failed to fetch bookings.");
            }
        });
    }

    // DELETE BOOKING (admin)
    public void deleteBooking(RoutingContext ctx) {
        String token = ctx.pathParam("token");

        client.removeDocument("bookings", new JsonObject().put("token", token), res -> {
            if (res.succeeded()) {
                ctx.response().end("Booking deleted.");
            } else {
                ctx.response().setStatusCode(500).end("Delete failed.");
            }
        });
    }
}
