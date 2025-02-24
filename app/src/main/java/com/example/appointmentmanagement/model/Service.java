package com.example.appointmentmanagement.model;

public class Service {
    private String id;
    private String name;

    public Service() {}
    public Service(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public Service(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
