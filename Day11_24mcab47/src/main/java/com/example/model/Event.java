package com.example.model;

public class Event {
    private String id;
    private String title;
    private String date;
    private int tokensAvailable;

    public Event() {}

    public Event(String id, String title, String date, int tokensAvailable) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.tokensAvailable = tokensAvailable;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public int getTokensAvailable() { return tokensAvailable; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDate(String date) { this.date = date; }
    public void setTokensAvailable(int tokensAvailable) { this.tokensAvailable = tokensAvailable; }
}
