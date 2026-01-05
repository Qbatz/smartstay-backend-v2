package com.smartstay.smartstay.schedulers;

import com.smartstay.smartstay.dao.ElectricityConfig;
import com.smartstay.smartstay.dao.ElectricityReadings;
import com.smartstay.smartstay.dao.HostelReadings;
import com.smartstay.smartstay.dao.HostelV1;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.EBReadingType;
import com.smartstay.smartstay.events.AddEbEvents;
import com.smartstay.smartstay.events.HostelReadingEbEvents;
import com.smartstay.smartstay.services.ElectricityService;
import com.smartstay.smartstay.services.HostelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EBBillingScheduler {
    @Autowired
    private HostelService hostelService;

    @Autowired
    private ElectricityService electricityService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 53 20 * * *")
    public void calculateEb() {
       List<HostelV1> hostelIds = hostelService.findAHostelsHavingBillingRuleEndingToday();
       hostelIds.forEach(i -> {
           if (i.getElectricityConfig().getTypeOfReading().equalsIgnoreCase(EBReadingType.ROOM_READING.name())) {
               calculateEbForRoomReading(i.getHostelId());
           }
           else {
               calculateEbForHostelReading(i.getHostelId());
           }
       });


    }


    private void calculateEbForRoomReading(String hostelId) {
        List<ElectricityReadings> listElectricityReading = electricityService.findAllInvoiceNotGenertedReadings(hostelId);
        if (listElectricityReading != null) {
            listElectricityReading.forEach(item -> {
                if (!item.isFirstEntry()) {
                    eventPublisher.publishEvent(new AddEbEvents(this, hostelId, item.getRoomId(), item ));
                }
            });
        }
    }


    private void calculateEbForHostelReading(String hostelId) {
        List<HostelReadings> listHostelReading = electricityService.findAllInvoiceNotGeneratedReadingsForHostel(hostelId);
        if (listHostelReading != null) {
            listHostelReading.forEach(item -> {
               if (!item.isFirstEntry()) {
                   eventPublisher.publishEvent(new HostelReadingEbEvents(this, item));
               }
            });
        }
    }
}
