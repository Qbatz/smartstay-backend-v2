package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.bank.TransactionDto;
import com.smartstay.smartstay.dto.bills.PaymentSummary;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.transactions.AddPayment;
import com.smartstay.smartstay.repositories.TransactionV1Repository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class TransactionService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private InvoiceV1Service invoiceService;
    @Autowired
    private PaymentSummaryService paymentSummaryService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private TransactionV1Repository transactionRespository;

    /**
     * not using it
     * @param customer
     * @param amount
     * @return
     */
    public List<TransactionV1> addBookingAmount(Customers customer, double amount) {
        if (authentication.isAuthenticated()) {
            TransactionV1 transactionV1 = new TransactionV1();
            transactionV1.setCustomers(customer);
            transactionV1.setPaidAmount(amount);
            transactionV1.setType(TransactionType.BOOKING.name());
            transactionV1.setCreatedAt(new Date());
            transactionV1.setStatus(PaymentStatus.PENDING.name());
            transactionV1.setCreatedBy(authentication.getName());
            transactionRespository.save(transactionV1);

            return transactionRespository.findByCustomers(customer);
        }
        else {
            return null;
        }
    }

    public void addAdvanceAmount(Customers customer, double amount) {
        if (authentication.isAuthenticated()) {
            TransactionV1 transactionV1 = new TransactionV1();
            transactionV1.setCustomers(customer);
            transactionV1.setPaidAmount(amount);
            transactionV1.setType(TransactionType.ADVANCE.name());
            transactionV1.setCreatedAt(new Date());
            transactionV1.setStatus(PaymentStatus.PENDING.name());
            transactionV1.setCreatedBy(authentication.getName());

            transactionRespository.save(transactionV1);
        }
    }


    public ResponseEntity<?> recordPayment(String hostelId, String invoiceId, AddPayment payment) {
        String typeOfPayment = null;
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!Utils.checkNullOrEmpty(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (!Utils.checkNullOrEmpty(invoiceId)) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }

        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BILLS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!Utils.checkNullOrEmpty(payment.bankId())) {
            return new ResponseEntity<>(Utils.REQUIRED_TRANSACTION_MODE, HttpStatus.BAD_REQUEST);
        }
        if (!Utils.checkNullOrEmpty(payment.amount())) {
            return new ResponseEntity<>(Utils.AMOUNT_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (!bankingService.checkBankExist(payment.bankId())) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 invoicesV1 = invoiceService.findInvoiceDetails(invoiceId);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }

        double paidAmount = findPaidAmountForInvoice(invoiceId);

        TransactionV1 transactionV1 = new TransactionV1();

        if (Objects.equals(invoicesV1.getTotalAmount(), payment.amount())) {
            typeOfPayment = PaymentStatus.PAID.name();
            transactionV1.setStatus(PaymentStatus.PAID.name());
            transactionV1.setPaidAmount(payment.amount());
        }
        else if (invoicesV1.getTotalAmount() > payment.amount()) {
            if (paidAmount + payment.amount() == invoicesV1.getTotalAmount()) {
                transactionV1.setStatus(PaymentStatus.PAID.name());
                typeOfPayment = PaymentStatus.PAID.name();
                transactionV1.setPaidAmount(payment.amount());
            }
            else if (paidAmount + payment.amount() > invoicesV1.getTotalAmount()) {
                transactionV1.setPaidAmount(paidAmount + payment.amount());
                transactionV1.setStatus(PaymentStatus.ADVANCE_IN_HAND.name());
                typeOfPayment = PaymentStatus.PAID.name();
            }
            else {
                transactionV1.setPaidAmount(payment.amount());
                transactionV1.setStatus(PaymentStatus.PARTIAL_PAYMENT.name());
                typeOfPayment = PaymentStatus.PARTIAL_PAYMENT.name();
            }
        }
        if (Utils.checkNullOrEmpty(payment.paymentDate())) {
            transactionV1.setPaidAt(new Date());
        }
        else {
            transactionV1.setPaidAt(Utils.stringToDate(payment.paymentDate(), Utils.USER_INPUT_DATE_FORMAT));
        }


        transactionV1.setBankId(payment.bankId());
        transactionV1.setReferenceNumber(payment.referenceId());
        transactionV1.setUpdatedBy(authentication.getName());
        transactionV1.setInvoiceId(invoiceId);

        transactionRespository.save(transactionV1);

        PaymentSummary summary = new PaymentSummary(hostelId, invoicesV1.getCustomerId(), invoicesV1.getInvoiceNumber(), payment.amount(), invoicesV1.getCustomerMobile(), invoicesV1.getCustomerMailId(), "Active");
        int response = paymentSummaryService.addPayment(summary);

        if (response == 1) {
            invoiceService.recordPayment(invoiceId, typeOfPayment);

            TransactionDto transaction = new TransactionDto(payment.bankId(),
                    payment.referenceId(),
                    payment.amount(),
                    BankTransactionType.CREDIT.name(),
                    BankSource.INVOICE.name(),
                    hostelId,
                    payment.paymentDate());

            bankTransactionService.addTransaction(transaction);

            return new ResponseEntity<>(Utils.PAYMENT_SUCCESS, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
    }

    private Double findPaidAmountForInvoice(String invoiceId) {
        List<TransactionV1> listTransaction = transactionRespository.findByInvoiceId(invoiceId);
        double paidAmount = 0.0;
        if (!listTransaction.isEmpty()) {
            paidAmount = listTransaction.stream()
                    .mapToDouble(TransactionV1::getPaidAmount)
                    .sum();
        }
        return paidAmount;
    }
}
