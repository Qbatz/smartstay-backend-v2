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

	/**
	 *
	 * need to execute on production
	 */

//	@Bean
//	public CommandLineRunner cancelBookingInvoice(InvoicesV1Repository invoicesV1Repository) {
//		return args -> {
//			List<InvoicesV1> bookingCancelled = invoicesV1Repository.findBookingInvoiceWithCancelledPayment()
//					.stream()
//					.map(i -> {
//						i.setCancelled(true);
//						return i;
//					})
//					.toList();
//
//			invoicesV1Repository.saveAll(bookingCancelled);
//		};
//	}


//	@Bean
//	public CommandLineRunner mapHostelIdWithCreditDebitNotes(InvoicesV1Repository invoicesV1Repository, CreditDebitNotesRepository creditDebitNotesRepository) {
//		return args -> {
//			List<CreditDebitNotes> listCreditDebits = creditDebitNotesRepository.findAll();
//			List<String> invoicesId = listCreditDebits
//					.stream()
//					.map(CreditDebitNotes::getInvoiceId)
//					.toList();
//
//			List<InvoicesV1> listInvoices = invoicesV1Repository.findAllById(invoicesId);
//
//			List<CreditDebitNotes> creditDebitNotes = listCreditDebits
//					.stream()
//					.map(i -> {
//						InvoicesV1 invoicesV1 = listInvoices.stream().filter(a -> a.getInvoiceId().equalsIgnoreCase(i.getInvoiceId()))
//								.findFirst()
//								.orElse(null);
//						if (invoicesV1 != null){
//							i.setHostelId(invoicesV1.getHostelId());
//						}
//
//						return i;
//					})
//					.toList();
//
//			creditDebitNotesRepository.saveAll(creditDebitNotes);
//		};
//	}


//	@Bean
//	public CommandLineRunner createReceiptForCancelledBooking(InvoicesV1Repository invoicesV1Repository, TransactionV1Repository transactionV1Repository, CreditDebitNotesRepository creditDebitNotesRepository, BookingsRepository bookingsRepository) {
//		return args -> {
//			List<InvoicesV1> listInvoices = invoicesV1Repository.findCancelledBooking();
//			List<String> invoiceIds = listInvoices
//					.stream()
//					.map(InvoicesV1::getInvoiceId)
//					.toList();
//			List<String> customerIds = listInvoices
//					.stream()
//					.map(InvoicesV1::getCustomerId)
//					.toList();
//			List<BookingsV1> listBookings = bookingsRepository.findByCustomerIdIn(customerIds);
//
//			List<CreditDebitNotes> listCreditDebitNotes = creditDebitNotesRepository.findByInvoiceIdIn(invoiceIds);
//			List<TransactionV1> listTransactions = listCreditDebitNotes
//					.stream()
//					.map(i -> {
//						BookingsV1 bookingsV1 = listBookings
//								.stream()
//								.filter(i2 -> i2.getCustomerId().equalsIgnoreCase(i.getCustomerId()))
//								.findFirst()
//								.orElse(null);
//						TransactionV1 transactionV1 = new TransactionV1();
//						transactionV1.setType(TransactionType.REFUND.name());
//						transactionV1.setPaidAmount(i.getAmount());
//						transactionV1.setCreatedBy(i.getCreatedBy());
//						transactionV1.setCreatedAt(new Date());
//						transactionV1.setStatus(PaymentStatus.REFUNDED.name());
//						transactionV1.setInvoiceId(i.getInvoiceId());
//						transactionV1.setHostelId(i.getHostelId());
//						transactionV1.setTransactionMode(ReceiptMode.MANUAL.name());
////						 transactionV1.setIsInvoice(false);
//						transactionV1.setCustomerId(i.getCustomerId());
//						if (bookingsV1 != null) {
//							transactionV1.setPaymentDate(Utils.convertToTimeStamp(bookingsV1.getCancelDate()));
//						}
//						else {
//							transactionV1.setPaymentDate(Utils.convertToTimeStamp(i.getCreatedAt()));
//						}
//						transactionV1.setTransactionReferenceId(generateRandomNumber(transactionV1Repository));
//						transactionV1.setBankId(i.getBookingId());
//						transactionV1.setReferenceNumber(null);
//						transactionV1.setPaidAt(i.getCreatedAt());
//						transactionV1.setUpdatedBy(i.getCreatedBy());
//
//						return transactionV1;
//					})
//					.toList();
//
//			transactionV1Repository.saveAll(listTransactions);
//		};
//
//	}

//	public String generateRandomNumber(TransactionV1Repository transactionRespository) {
//		String randomId = Utils.generateReference();
//		if (transactionRespository.existsByTransactionReferenceId(randomId)) {
//			return generateRandomNumber(transactionRespository);
//		}
//		return randomId;
//	}
}