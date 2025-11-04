package com.smartstay.smartstay;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.services.TemplatesService;
import com.smartstay.smartstay.util.BillingCycle;
import com.smartstay.smartstay.util.BillingCycleUtil;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
@EnableScheduling
public class SmartstayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartstayApplication.class, args);
	}

//	@Bean
//	public CommandLineRunner customersAmenityAmountMapping(CustomerAmenityRepository customerAmenityRepository, AmentityRepository amenityRepository) {
//		return args -> {
//			List<AmenitiesV1> listAmenities = amenityRepository.findAll();
//			List<CustomersAmenity> listCustomerAmenities = customerAmenityRepository.findAll()
//					.stream()
//					.map(i -> {
//						Double amount = listAmenities
//								.stream()
//								.filter(item -> Objects.equals(item.getAmenityId(), i.getAmenityId()))
//								.mapToDouble(AmenitiesV1::getAmenityAmount)
//								.sum();
//						i.setAmenityPrice(amount);
//						return i;
//					})
//					.toList();
//
//			customerAmenityRepository.saveAll(listCustomerAmenities);
//
//		};
//	}

}