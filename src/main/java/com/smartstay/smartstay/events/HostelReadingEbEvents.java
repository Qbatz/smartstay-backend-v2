package com.smartstay.smartstay.events;

import com.smartstay.smartstay.dao.HostelReadings;
import org.springframework.context.ApplicationEvent;

public class HostelReadingEbEvents extends ApplicationEvent {
    private HostelReadings hostelReadings = null;
    public HostelReadingEbEvents(Object source, HostelReadings hostelReadings) {
        super(source);
        this.hostelReadings = hostelReadings;
    }

    public HostelReadings getHostelReadings() {
        return hostelReadings;
    }
}
