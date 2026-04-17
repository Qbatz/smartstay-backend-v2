package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.events.ReminderEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderSchedulers {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

//    @Scheduled(cron = "0 0 8 * * *") for production
//    @Scheduled(cron = "0 0 7 * * *") for dev
    @Scheduled(cron = "0 0 8 * * *")
    public void sendReminderNotifications() {
        applicationEventPublisher.publishEvent(new ReminderEvents(this));
    }
}
