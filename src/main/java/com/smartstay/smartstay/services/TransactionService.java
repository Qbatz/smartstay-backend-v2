package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.bank.PaymentHistoryProjection;
import com.smartstay.smartstay.dto.bank.TransactionDto;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.bills.PaymentSummary;
import com.smartstay.smartstay.dto.transaction.PartialPaidInvoiceInfo;
import com.smartstay.smartstay.dto.transaction.Receipts;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.transactions.AddPayment;
import com.smartstay.smartstay.repositories.TransactionV1Repository;
import com.smartstay.smartstay.responses.invoices.AccountDetails;
import com.smartstay.smartstay.responses.invoices.CustomerInfo;
import com.smartstay.smartstay.responses.invoices.StayInfo;
import com.smartstay.smartstay.responses.receipt.ReceiptConfigInfo;
import com.smartstay.smartstay.responses.receipt.ReceiptDetails;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    private HostelService hostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private InvoiceV1Service invoiceService;

    @Autowired
    private TemplatesService templatesService;

    @Autowired
    private PaymentSummaryService paymentSummaryService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    @Lazy
    private CustomersService customersService;

    @Autowired
    private CustomersBedHistoryService customersBedHistoryService;

    @Autowired
    private BedsService bedService;
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
            transactionV1.setCustomerId(customer.getCustomerId());
            transactionV1.setPaidAmount(amount);
            transactionV1.setHostelId(customer.getHostelId());
            transactionV1.setType(TransactionType.BOOKING.name());
            transactionV1.setCreatedAt(new Date());
            transactionV1.setStatus(PaymentStatus.PENDING.name());
            transactionV1.setCreatedBy(authentication.getName());
            transactionRespository.save(transactionV1);

            return transactionRespository.findByCustomerId(customer.getCustomerId());
        }
        else {
            return null;
        }
    }

    public void addAdvanceAmount(Customers customer, double amount) {
        if (authentication.isAuthenticated()) {
            TransactionV1 transactionV1 = new TransactionV1();
            transactionV1.setCustomerId(customer.getCustomerId());
            transactionV1.setPaidAmount(amount);
            transactionV1.setType(TransactionType.ADVANCE.name());
            transactionV1.setCreatedAt(new Date());
            transactionV1.setHostelId(customer.getHostelId());
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

        transactionV1.setHostelId(hostelId);
        transactionV1.setBankId(payment.bankId());
        transactionV1.setReferenceNumber(payment.referenceId());
        transactionV1.setUpdatedBy(authentication.getName());
        transactionV1.setTransactionReferenceId(generateRandomNumber());
        transactionV1.setInvoiceId(invoiceId);
        transactionV1.setCustomerId(invoicesV1.getCustomerId());
        transactionV1.setCreatedAt(new Date());
        transactionV1.setCreatedBy(authentication.getName());
        transactionV1.setPaymentDate(Utils.stringToDate(payment.paymentDate(), Utils.USER_INPUT_DATE_FORMAT));


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

    public Double findPaidAmountForInvoice(String invoiceId) {
        List<TransactionV1> listTransaction = transactionRespository.findByInvoiceId(invoiceId);
        double paidAmount = 0.0;
        if (!listTransaction.isEmpty()) {
            paidAmount = listTransaction.stream()
                    .mapToDouble(TransactionV1::getPaidAmount)
                    .sum();
        }
        return paidAmount;
    }

    public List<PartialPaidInvoiceInfo> getTransactionInfo(List<String> partialPaymentInvoices) {
        return transactionRespository.findByInvoiceIdIn(partialPaymentInvoices)
                .stream()
                .map(item -> new PartialPaidInvoiceInfo(item.getInvoiceId(), item.getPaidAmount()))
                .toList();
    }

    public Double getAdvancePaidAmount(String invoiceNumber) {
        return transactionRespository.getTotalPaidAmountByInvoiceId(invoiceNumber);
    }

    public List<Receipts> getAllReceiptsByHostelId(String hostelId) {
        return transactionRespository.findByHostelId(hostelId);
    }

    public String generateRandomNumber() {
        String randomId = Utils.generateReference();
        if (transactionRespository.existsByTransactionReferenceId(randomId)) {
            return generateRandomNumber();
        }
        return randomId;
    }

    public List<PaymentHistoryProjection> getPaymentHistoryByInvoiceId(String invoiceId) {
        return transactionRespository.getPaymentHistoryByInvoiceId(invoiceId);
    }

    public ResponseEntity<?> getReceiptDetailsByTransactionId(String hostelId, String transactionId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_INVOICE, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

        TransactionV1 transactionV1 = transactionRespository.findById(transactionId).orElse(null);
        if (transactionV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_TRANSACTION_ID, HttpStatus.BAD_REQUEST);
        }
        InvoicesV1 invoicesV1 = invoiceService.findInvoiceDetails(transactionV1.getInvoiceId());

        String hostelPhone = null;
        String hostelEmail = null;
        String invoiceType = "Rent";
        StringBuilder hostelFullAddress = new StringBuilder();
        String receiptSignatureUrl = null;
        String hostelLogo = null;

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Advance";
        }

        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getHouseNo());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getStreet() != null && !hostelV1.getStreet().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getStreet());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getCity() != null && !hostelV1.getCity().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getCity());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getState() != null && !hostelV1.getState().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getState());
            hostelFullAddress.append("-");
        }
        if (hostelV1.getPincode() != 0) {
            hostelFullAddress.append(hostelV1.getPincode());
        }

        AccountDetails accountDetails = null;
        ReceiptConfigInfo receiptConfigInfo = null;
        com.smartstay.smartstay.dao.BillTemplates hostelTemplates = templatesService.getTemplateByHostelId(hostelId);
        if (hostelTemplates != null) {
            if (!hostelTemplates.isMobileCustomized()) {
                hostelPhone = hostelTemplates.getMobile();
            } else {
                hostelPhone = hostelTemplates.getTemplateTypes().stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name())).map(BillTemplateType::getReceiptPhoneNumber).toString();

            }

            if (!hostelTemplates.isEmailCustomized()) {
                hostelEmail = hostelTemplates.getEmailId();
            } else {
                hostelEmail = hostelTemplates.getTemplateTypes().stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name())).map(BillTemplateType::getReceiptMailId).toString();
            }

            BillTemplateType templateType = hostelTemplates.getTemplateTypes().stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name())).findAny().get();

            if (!hostelTemplates.isSignatureCustomized()) {
                receiptSignatureUrl = hostelTemplates.getDigitalSignature();
            } else {
                receiptSignatureUrl = templateType.getReceiptSignatureUrl();
            }
            if (!hostelTemplates.isLogoCustomized()) {
                hostelLogo = hostelTemplates.getHostelLogo();
            } else {
                hostelLogo = templateType.getReceiptLogoUrl();
            }

            if (templateType.getBankAccountId() != null) {
                BankingV1 bankingV1 = bankingService.getBankDetails(templateType.getBankAccountId());
                accountDetails = new AccountDetails(bankingV1.getAccountNumber(), bankingV1.getIfscCode(), bankingV1.getBankName(), bankingV1.getUpiId(), templateType.getQrCode());
            } else {
                accountDetails = new AccountDetails(null, null, null, null, templateType.getQrCode());
            }


            receiptConfigInfo = new ReceiptConfigInfo(templateType.getReceiptTermsAndCondition(), receiptSignatureUrl, hostelLogo, hostelFullAddress.toString(), templateType.getReceiptTemplateColor(), templateType.getReceiptNotes(), invoiceType);
        }

        Customers customers = customersService.getCustomerInformation(invoicesV1.getCustomerId());
        CustomerInfo customerInfo = null;
        if (customers != null) {
            StringBuilder fullName = new StringBuilder();
            StringBuilder fullAddress = new StringBuilder();
            if (customers.getFirstName() != null) {
                fullName.append(customers.getFirstName());
            }
            if (customers.getLastName() != null && !customers.getLastName().trim().equalsIgnoreCase("")) {
                fullName.append(", ");
                fullName.append(customers.getLastName());
            }
            if (customers.getHouseNo() != null) {
                fullAddress.append(customers.getHouseNo());
                fullAddress.append(", ");
            }
            if (customers.getStreet() != null) {
                fullAddress.append(customers.getStreet());
                fullAddress.append(", ");
            }
            if (customers.getCity() != null) {
                fullAddress.append(customers.getCity());
                fullAddress.append(", ");
            }
            if (customers.getState() != null) {
                fullAddress.append(customers.getState());
                fullAddress.append("-");
            }

            if (customers.getPincode() != 0) {
                fullAddress.append(customers.getPincode());
            }

            customerInfo = new CustomerInfo(customers.getFirstName(), customers.getLastName(), fullName.toString(), customers.getMobile(), "91", fullAddress.toString(), Utils.dateToString(customers.getJoiningDate()));
        }

        StayInfo stayInfo = new StayInfo(null, null, null, null);
        CustomersBedHistory bedHistory = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
           bedHistory = customersBedHistoryService.getCustomerBedByStartDate(customers.getCustomerId(), invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());
            BedDetails bedDetails = bedService.getBedDetails(bedHistory.getBedId());
            if (bedDetails != null) {
                stayInfo = new StayInfo(bedDetails.getBedName(), bedDetails.getFloorName(), bedDetails.getRoomName(), hostelV1.getHostelName());
            }
        }




        ReceiptDetails details = new ReceiptDetails(invoicesV1.getInvoiceNumber(), invoicesV1.getInvoiceId(), hostelEmail, hostelPhone, "91", customerInfo, stayInfo, accountDetails, receiptConfigInfo);
        return new ResponseEntity<>(details, HttpStatus.OK);

    }
}
