package com.smartstay.smartstay.events;

import org.springframework.context.ApplicationEvent;

public class ReminderEvents extends ApplicationEvent {
    public ReminderEvents(Object source) {
        super(source);
    }
}
