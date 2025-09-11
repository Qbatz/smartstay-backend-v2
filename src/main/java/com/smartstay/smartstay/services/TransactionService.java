package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.TransactionV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.ennum.TransactionType;
import com.smartstay.smartstay.payloads.transactions.AddPayment;
import com.smartstay.smartstay.repositories.TransactionV1Repository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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
    private TransactionV1Repository transactionRespository;

    public List<TransactionV1> addBookingAmount(Customers customer, double amount) {
        if (authentication.isAuthenticated()) {
            TransactionV1 transactionV1 = new TransactionV1();
            transactionV1.setCustomers(customer);
            transactionV1.setAmount(amount);
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
            transactionV1.setAmount(amount);
            transactionV1.setType(TransactionType.ADVANCE.name());
            transactionV1.setCreatedAt(new Date());
            transactionV1.setStatus(PaymentStatus.PENDING.name());
            transactionV1.setCreatedBy(authentication.getName());

            transactionRespository.save(transactionV1);
        }
    }


    public ResponseEntity<?> recordPayment(String hostelId, String transactionId, AddPayment payment) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!Utils.checkNullOrEmpty(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (!Utils.checkNullOrEmpty(transactionId)) {
            return new ResponseEntity<>(Utils.INVALID_TRANSACTION_ID, HttpStatus.BAD_REQUEST);
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
        if (!Utils.checkNullOrEmpty(payment.modeOfTransaction())) {
            return new ResponseEntity<>(Utils.REQUIRED_TRANSACTION_MODE, HttpStatus.BAD_REQUEST);
        }
        if (!Utils.checkNullOrEmpty(payment.amount())) {
            return new ResponseEntity<>(Utils.AMOUNT_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        TransactionV1 transactionV1 = transactionRespository.findById(transactionId).orElse(null);
        if (transactionV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_TRANSACTION_ID, HttpStatus.BAD_REQUEST);
        }
        InvoicesV1 invoicesV1 = invoiceService.findInvoiceDetails(transactionV1.getInvoiceId());
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }


        if (invoicesV1.getAmount() == payment.amount() || invoicesV1.getAmount() < payment.amount()) {
            transactionV1.setStatus(PaymentStatus.PAID.name());
        }
        if (invoicesV1.getAmount() > payment.amount()) {
            transactionV1.setStatus(PaymentStatus.PARTIAL_PAYMENT.name());
            transactionV1.setBalanceAmount(invoicesV1.getAmount() - payment.amount());
        }
        if (Utils.checkNullOrEmpty(payment.paymentDate())) {
            transactionV1.setPaidAt(new Date());
        }
        else {
            transactionV1.setPaidAt(Utils.stringToDate(payment.paymentDate(), Utils.USER_INPUT_DATE_FORMAT));
        }
        transactionV1.setUpdatedBy(authentication.getName());


        transactionRespository.save(transactionV1);

        return null;
    }
}
