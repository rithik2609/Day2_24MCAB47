package com.example.model;

public class Booking {
    private String userEmail;
    private String eventId;
    private String token;

    public Booking() {}

    public Booking(String userEmail, String eventId, String token) {
        this.userEmail = userEmail;
        this.eventId = eventId;
        this.token = token;
    }

    // Getters and Setters
    public String getUserEmail() { return userEmail; }
    public String getEventId() { return eventId; }
    public String getToken() { return token; }

    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setToken(String token) { this.token = token; }
}
