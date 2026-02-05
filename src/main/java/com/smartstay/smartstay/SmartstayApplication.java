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

//	@Bean
//	public CommandLineRunner mapSubscrions(SubscriptionRepository subscriptionRepository, HostelV1Repository hostelV1Repository, PlansRepository plansRepository) {
//		return args -> {
//			List<Subscription> listAllSubscriptions = subscriptionRepository.findAll();
//			Plans trialPlans = plansRepository.findPlanByPlanType(PlanType.TRIAL.name());
//			List<String> hostelIds = listAllSubscriptions.stream()
//					.map(Subscription::getHostelId)
//					.toList();
//
//			List<Subscription> listNewSubscriptions = hostelV1Repository.findAllHostelsNoIncludeIds(hostelIds)
//					.stream()
//					.map(i -> {
//							Date endingDate =  Utils.addDaysToDate(i.getCreatedAt(), trialPlans.getDuration().intValue());
//
//							Subscription history = new Subscription();
//							history.setHostelId(i.getHostelId());
//							history.setPlanAmount(trialPlans.getPrice());
//							history.setPaidAmount(0.0);
//							history.setPlanCode(trialPlans.getPlanCode());
//							history.setPlanName(trialPlans.getPlanName());
//							history.setPlanStartsAt(i.getCreatedAt());
//							history.setPlanEndsAt(endingDate);
//							history.setActivatedAt(i.getCreatedAt());
//							history.setCreatedAt(i.getCreatedAt());
//						return history;
//					})
//					.toList();
//
//			subscriptionRepository.saveAll(listNewSubscriptions);
//
//		};
//	}

//	@Bean
//	CommandLineRunner findSubscriptionEndedHostels(HostelPlanRepository hostelPlanRepository, SubscriptionRepository subscriptionRepository) {
//		return args -> {
//			List<HostelPlan> listHostelPlan = hostelPlanRepository.findNotActiveHostels(new Date());
//			List<Subscription> listSubscriptionWithNewDate = listHostelPlan
//					.stream()
//					.map(i -> {
//						Date planStartDate = Utils.addDaysToDate(i.getCurrentPlanEndsAt(), 1);
//						Date planEndDate = Utils.addDaysToDate(planStartDate, 30);
//						Date nextBillingAt = Utils.addDaysToDate(planEndDate, 1);
//
//						Subscription subscription = new Subscription();
//						subscription.setHostelId(i.getHostel().getHostelId());
//						subscription.setPaidAmount(0.0);
//						subscription.setPlanCode(i.getCurrentPlanCode());
//						subscription.setPlanStartsAt(planStartDate);
//						subscription.setPlanEndsAt(planEndDate);
//						subscription.setActivatedAt(planStartDate);
//						subscription.setPlanAmount(0.0);
//						subscription.setDiscount(0.0);
//						subscription.setDiscountAmount(0.0);
//						subscription.setNextBillingAt(nextBillingAt);
//						subscription.setCreatedAt(new Date());
//						return subscription;
//					})
//					.toList();
//
//
//			subscriptionRepository.saveAll(listSubscriptionWithNewDate);
//
//			List<HostelPlan> newPlans = listHostelPlan
//					.stream()
//					.map(i -> {
//						Subscription currentSub = listSubscriptionWithNewDate
//								.stream()
//								.filter(j -> j.getHostelId().equalsIgnoreCase(i.getHostel().getHostelId()))
//								.findFirst()
//								.orElse(null);
//
//						if (currentSub != null) {
//							i.setCurrentPlanStartsAt(currentSub.getPlanStartsAt());
//							i.setCurrentPlanEndsAt(currentSub.getPlanEndsAt());
//						}
//						return i;
//					})
//					.toList();
//
//			hostelPlanRepository.saveAll(newPlans);
//		};
//	}


	/**
	 *
	 * no need in production anymore
	 *
	 */

//	@Bean
//	CommandLineRunner mapBookingDateToBookingInvoice(InvoicesV1Repository invoicesV1Repository) {
//		return args -> {
//			List<InvoicesV1> listInvoices = invoicesV1Repository.findAllBookingInvoices()
//					.stream()
//					.map(i -> {
//						i.setInvoiceStartDate(i.getInvoiceDueDate());
//						i.setInvoiceEndDate(i.getInvoiceDueDate());
//						return i;
//					})
//					.toList();
//
//			invoicesV1Repository.saveAll(listInvoices);
//
//		};
//	}

}