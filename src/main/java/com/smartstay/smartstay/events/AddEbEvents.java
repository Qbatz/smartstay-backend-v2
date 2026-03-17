package com.smartstay.smartstay.events;

import com.smartstay.smartstay.dao.ElectricityReadings;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

public class AddEbEvents extends ApplicationEvent {
    private String hostelId;
    private Integer roomId;
    private ElectricityReadings electricityReadings;

    public AddEbEvents(Object source, String hostelId, Integer roomId, ElectricityReadings electricityReadings) {
        super(source);
        this.hostelId = hostelId;
        this.roomId = roomId;
        this.electricityReadings = electricityReadings;
    }

    public String getHostelId() {
        return hostelId;
    }

    public Integer getRoomId() {
        return roomId;
    }


    public ElectricityReadings getElectricityReadings() {
        return electricityReadings;
    }

    public void setElectricityReadings(ElectricityReadings electricityReadings) {
        this.electricityReadings = electricityReadings;
    }

}
