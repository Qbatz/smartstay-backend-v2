package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.payloads.roles.Permission;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.services.TemplatesService;
import com.smartstay.smartstay.util.BillingCycle;
import com.smartstay.smartstay.util.BillingCycleUtil;
import com.smartstay.smartstay.util.Utils;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.yaml.snakeyaml.comments.CommentLine;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default")})
public class SmartstayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartstayApplication.class, args);
    }

    //This should be performed in prod environment

//    alter table smart_stay.electricity_config drop is_pro_rate;
//    ALTER TABLE smart_stay.bill_template_type MODIFY COLUMN invoice_terms_and_condition LONGTEXT;
//    ALTER TABLE smart_stay.bill_template_type MODIFY COLUMN invoice_notes LONGTEXT;
//    ALTER TABLE smart_stay.bill_template_type MODIFY COLUMN receipt_notes LONGTEXT;
//    ALTER TABLE smart_stay.bill_template_type MODIFY COLUMN receipt_terms_and_condition LONGTEXT;

//    @Bean
//    CommandLineRunner mapBillingRuleToPrepaid(BillingRuleRepository billingRuleRepository) {
//        return args -> {
//            List<BillingRules> listBillingRules = billingRuleRepository
//                    .findAll()
//                    .stream()
//                    .map(i -> {
//                        i.setBillingModel(BillingModel.PREPAID.name());
//                        return i;
//                    })
//                    .toList();
//
//            billingRuleRepository.saveAll(listBillingRules);
//        };
//    }

//    @Bean
//    CommandLineRunner updateRoomCount(RoomRepository roomRepository, BedsRepository bedsRepository) {
//        return args -> {
//            List<Rooms> listRooms = roomRepository.findAll()
//                    .stream()
//                    .filter(i -> i.getIsDeleted()== null || !i.getIsDeleted())
//                    .toList();
//
//            List<Integer> listRoomIds = listRooms
//                    .stream()
//                    .map(Rooms::getRoomId)
//                    .toList();
//
//            List<Beds> listBeds = bedsRepository.findByRoomIdIn(listRoomIds);
//            List<Rooms> listRoomsForUpdate = listRooms
//                    .stream()
//                    .map(i -> {
//                        long counts = listBeds
//                                .stream()
//                                .filter(j -> j.getRoomId().equals(i.getRoomId()))
//                                .filter(j -> i.getIsDeleted() == null || !i.getIsDeleted())
//                                .count();
//                        i.setSharingType((int) counts);
//                        return i;
//                    })
//                    .toList();
//
//            roomRepository.saveAll(listRoomsForUpdate);
//        };
//    }
}