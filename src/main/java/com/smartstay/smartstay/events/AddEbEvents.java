package com.smartstay.smartstay.events;

import com.smartstay.smartstay.dao.ElectricityReadings;
import org.springframework.context.ApplicationEvent;

import java.util.Date;

public class AddEbEvents extends ApplicationEvent {
    private String hostelId;
    private Integer roomId;
    private Double currentReading;
    private Double chargePerUnits;
    private Date entryDate;
    private String createdBy;
    private ElectricityReadings electricityReadings;
    private Integer newReadingId;

    public AddEbEvents(Object source, String hostelId, Integer roomId, Double currentReading, Double chargePerUnits, Date entrtDate, String createdBy, ElectricityReadings electricityReadings, int newReadingId) {
        super(source);
        this.hostelId = hostelId;
        this.roomId = roomId;
        this.currentReading = currentReading;
        this.chargePerUnits = chargePerUnits;
        this.entryDate = entrtDate;
        this.createdBy = createdBy;
        this.electricityReadings = electricityReadings;
        this.newReadingId = newReadingId;
    }

    public String getHostelId() {
        return hostelId;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public Double getCurrentReading() {
        return currentReading;
    }

    public Double getChargePerUnits() {
        return chargePerUnits;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public ElectricityReadings getElectricityReadings() {
        return electricityReadings;
    }

    public Integer getNewReadingId() {
        return newReadingId;
    }
}
