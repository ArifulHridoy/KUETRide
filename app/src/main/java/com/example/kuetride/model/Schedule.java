package com.example.kuetride.model;

public class Schedule {
    private String id;
    private String routeId;
    private String busId;
    private String date;
    private String time;

    public Schedule(String id, String routeId, String busId, String date, String time) {
        this.id = id;
        this.routeId = routeId;
        this.busId = busId;
        this.date = date;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getBusId() {
        return busId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
