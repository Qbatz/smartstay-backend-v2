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

	/**
	 * need to execute on production
	 *
	 *
	 */

//	@Bean
//	CommandLineRunner addSharingType(RoomRepository roomRepository, BedsRepository bedsRepository) {
//		return args -> {
//			List<Rooms> allRooms = roomRepository.findAll();
//			List<Integer> roomId = allRooms
//					.stream()
//					.map(Rooms::getRoomId)
//					.toList();
//			List<Beds> listBeds = bedsRepository.findByRoomIdIn(roomId);
//
//			List<Rooms> roomsWithSharing = new ArrayList<>();
//			allRooms.forEach(item -> {
//				long cout = listBeds
//						.stream()
//						.filter(i -> i.getRoomId().equals(item.getRoomId()))
//						.count();
//
//				item.setSharingType(Integer.parseInt(String.valueOf(cout)));
//				roomsWithSharing.add(item);
//			});
//
//				roomRepository.saveAll(roomsWithSharing);
//
//		};
//	}



}