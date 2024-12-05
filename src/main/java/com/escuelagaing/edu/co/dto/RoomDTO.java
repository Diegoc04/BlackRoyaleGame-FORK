package com.escuelagaing.edu.co.dto;

import com.escuelagaing.edu.co.model.RoomStatus;


public class RoomDTO {
    private String id;
    private RoomStatus status;

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }
}


