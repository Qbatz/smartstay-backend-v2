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
	 *
	 * need to execute in production
	 *
	 */
//	@Bean
//	CommandLineRunner mapSourceIdTotransactionNumber(BankTransactionRepository bankTransactionRepository) {
//		return args -> {
//			List<BankTransactionsV1> bankTransactions = bankTransactionRepository.findByTransactionType()
//					.stream()
//					.map(i -> {
//						if (i.getTransactionNumber() == null) {
//							i.setTransactionNumber(i.getSourceId());
//						}
//						return i;
//					})
//					.toList();
//
//			bankTransactionRepository.saveAll(bankTransactions);
//
//		};
//	}

//	@Bean
//	CommandLineRunner mapTransactionsWithBankTransactions(BankTransactionRepository bankTransactionRepository, TransactionV1Repository transactionV1Repository) {
//		return args -> {
//			List<BankTransactionsV1> bankTransactions = bankTransactionRepository.findByTransactionType();
//			List<BankTransactionsV1> listBankTransactionHavingNull = bankTransactions
//					.stream().filter(i -> i.getTransactionNumber() == null)
//					.toList();
//			if (!listBankTransactionHavingNull.isEmpty()) {
//				listBankTransactionHavingNull.forEach(item -> {
//					List<TransactionV1> transactionV1s = transactionV1Repository
//							.mapTransactionV1WithBankTransactions(item.getHostelId(), item.getBankId(), "DEBIT", item.getCreatedBy(), String.valueOf(item.getAmount()));
//
//					if (transactionV1s.size() == 1) {
//						item.setTransactionNumber(transactionV1s.get(0).getTransactionId());
//						bankTransactionRepository.save(item);
//					}
//				});
//			}
//		};
//	}

//	@Bean
//	CommandLineRunner markTransactionIsDeletedFalse(BankTransactionRepository bankTransactionRepository) {
//		return args -> {
//			List<BankTransactionsV1> listBankResponses = bankTransactionRepository
//					.findAll()
//					.stream()
//					.filter(i -> i.getIsDeleted() == null)
//					.map(i -> {
//						i.setIsDeleted(false);
//						return i;
//					})
//					.toList();
//
//			bankTransactionRepository.saveAll(listBankResponses);
//		};
//	}

//	@Bean
//	CommandLineRunner deleteRoomForDeletedFloors(FloorRepository floorRepository, RoomRepository roomRepository, BedsRepository bedsRepository) {
//		return args -> {
//			List<Floors> listFloors = floorRepository.findByIsDeletedIsTrue();
//			if (listFloors != null && !listFloors.isEmpty()) {
//				List<Integer> floorIds = listFloors
//						.stream()
//						.map(Floors::getFloorId)
//						.distinct()
//						.toList();
//
//				List<Rooms> listRooms = roomRepository.findAllByFloorIdIn(floorIds);
//				if (!listRooms.isEmpty()) {
//					List<Integer> roomIds = listRooms
//							.stream()
//							.map(Rooms::getRoomId)
//							.toList();
//
//					List<Rooms> deletableRooms = listRooms
//							.stream()
//							.map(i -> {
//								i.setIsDeleted(true);
//								return i;
//							})
//							.toList();
//					roomRepository.saveAll(deletableRooms);
//
//					List<Beds> listBeds = bedsRepository.findByRoomIdIn(roomIds);
//					if (!listBeds.isEmpty()) {
//						List<Beds> deletableBeds = listBeds
//								.stream()
//								.map(i -> {
//									i.setIsDeleted(true);
//									return i;
//								})
//								.toList();
//
//						bedsRepository.saveAll(deletableBeds);
//					}
//				}
//			}
//		};
//	}

//	@Bean
//	CommandLineRunner updateBillingRule(BillingRuleRepository billingRuleRepository) {
//		return args -> {
//			List<BillingRules> listBillRules = billingRuleRepository.findAll()
//					.stream()
//					.map(i -> {
//						i.setHasGracePeriod(false);
//						i.setGracePeriodDays(0);
//						i.setShouldNotify(false);
//						i.setReminderDays(new ArrayList<>());
//						i.setTypeOfBilling(BillingTypeEnum.FIXED_DATE.name());
//						return i;
//					})
//					.toList();
//
//			billingRuleRepository.saveAll(listBillRules);
//		};
//	}

	/**
	 *
	 * no need production
	 *
	 */
//	@Bean
//	CommandLineRunner modifyBillingRuleShouldNotify(BillingRuleRepository billingRuleRepository) {
//		return args -> {
//			List<BillingRules> listBillingRules = billingRuleRepository.findAll()
//					.stream()
//					.map(i -> {
//						if (i.getReminderDays() != null && !i.getReminderDays().isEmpty()) {
//							i.setShouldNotify(true);
//						}
//						else {
//							i.setShouldNotify(false);
//						}
//						return i;
//					})
//					.toList();
//
//			billingRuleRepository.saveAll(listBillingRules);
//		};
//	}

}