package com.example.appointmentmanagement.model;

public class Appointment {
    private String id;
    private String userId;
    private String userEmail;
    private String service;
    private String date;
    private String time;
    private String status;

    public Appointment() {
    }

    public Appointment(String id, String userId, String service, String date, String time, String status) {
        this.id = id;
        this.userId = userId;
        this.service = service;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
