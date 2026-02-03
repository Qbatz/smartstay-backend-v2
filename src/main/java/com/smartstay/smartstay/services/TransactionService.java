package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.transactions.TransactionForCustomerDetailsMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.bank.PaymentHistoryProjection;
import com.smartstay.smartstay.dto.bank.TransactionDto;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.bills.PaymentSummary;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.receipts.DeleteReceipts;
import com.smartstay.smartstay.dto.transaction.PartialPaidInvoiceInfo;
import com.smartstay.smartstay.dto.transaction.Receipts;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.invoice.RefundInvoice;
import com.smartstay.smartstay.payloads.transactions.AddPayment;
import com.smartstay.smartstay.dto.transaction.BankCollectionProjection;
import com.smartstay.smartstay.repositories.TransactionV1Repository;
import com.smartstay.smartstay.responses.invoices.AccountDetails;
import com.smartstay.smartstay.responses.invoices.CustomerInfo;
import com.smartstay.smartstay.responses.invoices.StayInfo;
import com.smartstay.smartstay.responses.receipt.ReceiptConfigInfo;
import com.smartstay.smartstay.responses.receipt.ReceiptDetails;
import com.smartstay.smartstay.responses.receipt.ReceiptInfo;
import com.smartstay.smartstay.responses.receiptForReport.ReceiptReportResponse;
import com.smartstay.smartstay.responses.expenseForReport.ExpenseReportResponse;
import com.smartstay.smartstay.repositories.ExpensesRepository;
import com.smartstay.smartstay.util.Utils;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    private CreditDebitNoteService creditDebitNoteService;
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
    @Autowired
    private ExpensesRepository expensesRepository;
    @Autowired
    private ExpenseCategoryService expenseCategoryService;
    @Autowired
    private VendorService vendorService;

    /**
     * not using it
     * 
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
            // transactionV1
            transactionV1.setType(TransactionType.BOOKING.name());
            transactionV1.setCreatedAt(new Date());
            transactionV1.setStatus(PaymentStatus.PENDING.name());
            transactionV1.setCreatedBy(authentication.getName());
            transactionRespository.save(transactionV1);

            return transactionRespository.findByCustomerId(customer.getCustomerId());
        } else {
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

    public ResponseEntity<?> recordPaymentForBooking(String hostelId, String invoiceId, AddPayment payment) {
        // String typeOfPayment = null;
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

        Date paymentDate = null;
        if (payment.paymentDate() == null) {
            paymentDate = new Date();
        } else {
            paymentDate = Utils.stringToDate(payment.paymentDate(), Utils.USER_INPUT_DATE_FORMAT);
        }

        TransactionV1 transactionV1 = new TransactionV1();
        InvoicesV1 invoicesV1 = invoiceService.findInvoiceDetails(invoiceId);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.BAD_REQUEST);
        }
        if (Utils.checkNullOrEmpty(payment.paymentDate())) {
            transactionV1.setPaidAt(new Date());
        } else {
            transactionV1.setPaidAt(Utils.stringToDate(payment.paymentDate(), Utils.USER_INPUT_DATE_FORMAT));
        }
        String transactionRandomNo = generateRandomNumber();
        transactionV1.setStatus(PaymentStatus.PAID.name());
        transactionV1.setPaidAmount(payment.amount());
        transactionV1.setHostelId(hostelId);
        transactionV1.setBankId(payment.bankId());
        transactionV1.setReferenceNumber(payment.referenceId());
        transactionV1.setUpdatedBy(authentication.getName());
        transactionV1.setTransactionReferenceId(transactionRandomNo);
        transactionV1.setInvoiceId(invoiceId);
        transactionV1.setCustomerId(invoicesV1.getCustomerId());
        transactionV1.setCreatedAt(new Date());
        transactionV1.setTransactionMode(ReceiptMode.MANUAL.name());
        transactionV1.setCreatedBy(authentication.getName());
        transactionV1.setPaymentDate(Utils.convertToTimeStamp(paymentDate));

        bankingService.updateBankBalance(payment.amount(), BankTransactionType.CREDIT.name(), payment.bankId(),
                payment.paymentDate());

        TransactionV1 trns = transactionRespository.save(transactionV1);

        PaymentSummary summary = new PaymentSummary(hostelId, invoicesV1.getCustomerId(), invoicesV1.getInvoiceNumber(),
                payment.amount(), invoicesV1.getCustomerMobile(), invoicesV1.getCustomerMailId(), "Active");
        int response = paymentSummaryService.addPayment(summary);

        if (response == 1) {
            //dont have to add the amount. For booking invoice is already created.
            invoiceService.recordPayment(invoiceId, PaymentStatus.PAID.name(), 0);

            TransactionDto transaction = new TransactionDto(payment.bankId(),
                    payment.referenceId(),
                    payment.amount(),
                    BankTransactionType.CREDIT.name(),
                    BankSource.INVOICE.name(),
                    hostelId,
                    payment.paymentDate(),
                    trns.getTransactionId());

            bankTransactionService.addTransaction(transaction);

            return new ResponseEntity<>(Utils.PAYMENT_SUCCESS, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
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
        if (invoicesV1.isCancelled()) {
            return new ResponseEntity<>(Utils.CANNOT_MAKE_PAYMENT_CANCELLED_INVOICES, HttpStatus.BAD_REQUEST);
        }

        double paidAmount = findPaidAmountForInvoice(invoiceId);

        TransactionV1 transactionV1 = new TransactionV1();
        Double gstAmount = invoicesV1.getGst();
        if (gstAmount == null) {
            gstAmount = 0.0;
        }

        if (Objects.equals((invoicesV1.getTotalAmount() + gstAmount), payment.amount())) {
            typeOfPayment = PaymentStatus.PAID.name();
            transactionV1.setStatus(PaymentStatus.PAID.name());
            transactionV1.setPaidAmount(payment.amount());
        } else if ((invoicesV1.getTotalAmount() + gstAmount) > payment.amount()) {
            if (paidAmount + payment.amount() == invoicesV1.getTotalAmount()) {
                transactionV1.setStatus(PaymentStatus.PAID.name());
                typeOfPayment = PaymentStatus.PAID.name();
                transactionV1.setPaidAmount(payment.amount());
            } else if (paidAmount + payment.amount() > (invoicesV1.getTotalAmount() + gstAmount)) {
                transactionV1.setPaidAmount(paidAmount + payment.amount());
                transactionV1.setStatus(PaymentStatus.ADVANCE_IN_HAND.name());
                typeOfPayment = PaymentStatus.PAID.name();
            } else {
                transactionV1.setPaidAmount(payment.amount());
                transactionV1.setStatus(PaymentStatus.PARTIAL_PAYMENT.name());
                typeOfPayment = PaymentStatus.PARTIAL_PAYMENT.name();
            }
        }
        if (Utils.checkNullOrEmpty(payment.paymentDate())) {
            transactionV1.setPaidAt(new Date());
        } else {
            transactionV1.setPaidAt(Utils.stringToDate(payment.paymentDate(), Utils.USER_INPUT_DATE_FORMAT));
        }

        Date paymentDate = null;
        if (payment.paymentDate() == null) {
            paymentDate = new Date();
        } else {
            paymentDate = Utils.stringToDate(payment.paymentDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }

        String transactionNumber = generateRandomNumber();

        transactionV1.setTransactionMode(ReceiptMode.MANUAL.name());
        transactionV1.setHostelId(hostelId);
        transactionV1.setBankId(payment.bankId());
        transactionV1.setReferenceNumber(payment.referenceId());
        transactionV1.setUpdatedBy(authentication.getName());
        transactionV1.setTransactionReferenceId(transactionNumber);
        transactionV1.setInvoiceId(invoiceId);
        transactionV1.setCustomerId(invoicesV1.getCustomerId());
        transactionV1.setCreatedAt(new Date());
        transactionV1.setCreatedBy(authentication.getName());
        transactionV1.setPaymentDate(Utils.convertToTimeStamp(paymentDate));

        bankingService.updateBankBalance(payment.amount(), BankTransactionType.CREDIT.name(), payment.bankId(),
                payment.paymentDate());

        TransactionV1 trnsV1 = transactionRespository.save(transactionV1);

        PaymentSummary summary = new PaymentSummary(hostelId, invoicesV1.getCustomerId(), invoicesV1.getInvoiceNumber(),
                payment.amount(), invoicesV1.getCustomerMobile(), invoicesV1.getCustomerMailId(), "Active");
        int response = paymentSummaryService.addPayment(summary);

        if (response == 1) {
            invoiceService.recordPayment(invoiceId, typeOfPayment, payment.amount());

            TransactionDto transaction = new TransactionDto(payment.bankId(),
                    payment.referenceId(),
                    payment.amount(),
                    BankTransactionType.CREDIT.name(),
                    BankSource.INVOICE.name(),
                    hostelId,
                    payment.paymentDate(),
                    trnsV1.getTransactionId());

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
                    .mapToDouble(i -> {
                        if (i.getPaidAmount() == null) {
                            return 0.0;
                        }
                        return i.getPaidAmount();
                    })
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

    public List<Receipts> getAllReceiptsByHostelIdOld(String hostelId) {
        return transactionRespository.findAllByHostelId(hostelId);
    }

    public List<TransactionV1> getAllReceiptsByHostelId(String hostelId) {
        List<TransactionV1> listTransactions = transactionRespository.findByHostelId(hostelId);
        if (listTransactions == null) {
            listTransactions = new ArrayList<>();
        }
        return listTransactions;
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
        } else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            invoiceType = "Booking";
        } else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            invoiceType = "Settlement";
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

        String invoiceMonth = null;
        if (invoicesV1.getInvoiceStartDate() != null) {
            BillingDates billingDates = hostelService.getBillingRuleOnDate(invoicesV1.getHostelId(),
                    invoicesV1.getInvoiceStartDate());
            if (billingDates != null) {
                if (billingDates.currentBillStartDate() != null) {
                    invoiceMonth = Utils.dateToMonth(billingDates.currentBillStartDate());
                }
            }
        }

        BankingV1 bankingV1 = bankingService.getBankDetails(transactionV1.getBankId());
        String bankName = null;
        if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CASH.name())) {
            bankName = "Cash";
        } else if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.CARD.name())) {
            bankName = "Card";
        } else if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.BANK.name())) {
            bankName = bankingV1.getBankName() + " Bank";
        } else if (bankingV1.getAccountType().equalsIgnoreCase(BankAccountType.UPI.name())) {
            bankName = BankAccountType.UPI.name();
        }
        StringBuilder account = new StringBuilder();
        account.append(bankingV1.getAccountHolderName());
        account.append("-");
        account.append(bankName);
        AccountDetails accountDetails = new AccountDetails(bankingV1.getAccountNumber(),
                bankingV1.getIfscCode(),
                account.toString(), bankingV1.getUpiId(), null);
        ;
        ReceiptConfigInfo receiptConfigInfo = null;
        com.smartstay.smartstay.dao.BillTemplates hostelTemplates = templatesService.getTemplateByHostelId(hostelId);
        if (hostelTemplates != null) {
            if (!hostelTemplates.isMobileCustomized()) {
                hostelPhone = hostelTemplates.getMobile();
            } else {
                if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())
                        || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
                    hostelPhone = hostelTemplates.getTemplateTypes()
                            .stream()
                            .filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.ADVANCE.name()))
                            .map(BillTemplateType::getReceiptPhoneNumber)
                            .toList()
                            .getFirst();
                } else {
                    hostelPhone = hostelTemplates.getTemplateTypes()
                            .stream()
                            .filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                            .map(BillTemplateType::getReceiptPhoneNumber)
                            .toList()
                            .getFirst();
                }

            }

            if (!hostelTemplates.isEmailCustomized()) {
                hostelEmail = hostelTemplates.getEmailId();
            } else {
                if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())
                        || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
                    hostelEmail = hostelTemplates.getTemplateTypes()
                            .stream()
                            .filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.ADVANCE.name()))
                            .map(BillTemplateType::getReceiptMailId)
                            .toList()
                            .getFirst();
                } else {
                    hostelEmail = hostelTemplates.getTemplateTypes()
                            .stream()
                            .filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                            .map(BillTemplateType::getReceiptMailId)
                            .toList()
                            .getFirst();
                }

            }

            BillTemplateType templateType = null;
            if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())
                    || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
                templateType = hostelTemplates
                        .getTemplateTypes()
                        .stream()
                        .filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.ADVANCE.name()))
                        .toList()
                        .getFirst();
            } else {
                templateType = hostelTemplates
                        .getTemplateTypes()
                        .stream()
                        .filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                        .toList()
                        .getFirst();
            }

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

            receiptConfigInfo = new ReceiptConfigInfo(templateType.getReceiptTermsAndCondition(),
                    receiptSignatureUrl,
                    hostelLogo,
                    hostelFullAddress.toString(),
                    templateType.getReceiptTemplateColor(),
                    templateType.getReceiptNotes(),
                    invoiceType);
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
                fullName.append(" ");
                fullName.append(customers.getLastName());
            }
            if (customers.getHouseNo() != null && !customers.getHouseNo().trim().equalsIgnoreCase("")) {
                fullAddress.append(customers.getHouseNo());
                fullAddress.append(", ");
            }
            if (customers.getStreet() != null && !customers.getStreet().trim().equalsIgnoreCase("")) {
                fullAddress.append(customers.getStreet());
                fullAddress.append(", ");
            }
            if (customers.getCity() != null && !customers.getCity().trim().equalsIgnoreCase("")) {
                fullAddress.append(customers.getCity());
                fullAddress.append(", ");
            }
            if (customers.getState() != null && !customers.getState().trim().equalsIgnoreCase("")) {
                fullAddress.append(customers.getState());
                fullAddress.append("-");
            }

            if (customers.getPincode() != 0) {
                fullAddress.append(customers.getPincode());
            }

            customerInfo = new CustomerInfo(customers.getFirstName(),
                    customers.getLastName(),
                    fullName.toString(),
                    customers.getCustomerId(),
                    customers.getMobile(),
                    "91",
                    fullAddress.toString(),
                    Utils.dateToString(customers.getJoiningDate()));
        }

        StayInfo stayInfo = new StayInfo(null, null, null, null);
        CustomersBedHistory bedHistory = null;
        if (!invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            assert customers != null;
            bedHistory = customersBedHistoryService.getCustomerBedByStartDate(customers.getCustomerId(),
                    invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());
            if (bedHistory != null) {
                BedDetails bedDetails = bedService.getBedDetails(bedHistory.getBedId());
                if (bedDetails != null) {
                    stayInfo = new StayInfo(bedDetails.getBedName(), bedDetails.getFloorName(),
                            bedDetails.getRoomName(), hostelV1.getHostelName());
                }
            }

        } else {
            assert customers != null;
            bedHistory = customersBedHistoryService.getCustomerBookedBed(customers.getCustomerId());
            BedDetails bedDetails = bedService.getBedDetails(bedHistory.getBedId());
            if (bedDetails != null) {
                stayInfo = new StayInfo(bedDetails.getBedName(), bedDetails.getFloorName(), bedDetails.getRoomName(),
                        hostelV1.getHostelName());
            }
        }
        StringBuilder receiverfullName = new StringBuilder();
        Users createdBy = usersService.findUserByUserId(transactionV1.getCreatedBy());

        if (createdBy.getFirstName() != null) {
            receiverfullName.append(createdBy.getFirstName());
        }
        if (createdBy.getLastName() != null && !createdBy.getLastName().trim().equalsIgnoreCase("")) {
            if (createdBy.getFirstName() != null) {
                receiverfullName.append(" ");
            }
            receiverfullName.append(createdBy.getLastName());
        }

        ReceiptInfo receiptInfo = new ReceiptInfo(transactionV1.getTransactionReferenceId(),
                transactionV1.getTransactionId(),
                Utils.dateToString(transactionV1.getPaymentDate()),
                Utils.dateToTime(transactionV1.getPaymentDate()),
                transactionV1.getPaidAmount(),
                invoiceType,
                transactionV1.getReferenceNumber(),
                receiverfullName.toString(),
                invoiceMonth);

        double dueAmount = 0.0;
        if (invoicesV1.getPaidAmount() != null) {
            dueAmount = invoicesV1.getTotalAmount() - invoicesV1.getPaidAmount();
        }

        ReceiptDetails details = new ReceiptDetails(invoicesV1.getInvoiceNumber(),
                transactionV1.getTransactionReferenceId(),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                invoicesV1.getInvoiceId(),
                invoicesV1.getTotalAmount(),
                invoicesV1.getPaidAmount(),
                dueAmount,
                hostelEmail,
                hostelPhone, "91",
                invoicesV1.getHostelId(),
                receiptInfo,
                customerInfo,
                stayInfo,
                accountDetails,
                receiptConfigInfo);
        return new ResponseEntity<>(details, HttpStatus.OK);

    }

    public Double getFinalSettlementPaidAmount(String invoiceId) {
        List<TransactionV1> listTransactions = transactionRespository.findByInvoiceId(invoiceId);

        return listTransactions
                .stream()
                .mapToDouble(TransactionV1::getPaidAmount)
                .sum();
    }

    public ResponseEntity<?> refundForInvoice(String hostelId, String invoiceId, RefundInvoice refundInvoice) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!Utils.checkNullOrEmpty(invoiceId)) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.BAD_REQUEST);
        }

        Date transactionDate = Utils.stringToDate(refundInvoice.refundDate().replace("/", "-"),
                Utils.USER_INPUT_DATE_FORMAT);

        InvoicesV1 invoicesV1 = invoiceService.findInvoiceDetails(invoiceId);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }
        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.REFUNDED.name())) {
            return new ResponseEntity<>(Utils.REFUND_COMPLETED, HttpStatus.BAD_REQUEST);
        }
        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_REFUND_CANCELLED_INVOICE, HttpStatus.BAD_REQUEST);
        }
        if (!bankingService.checkBankExist(refundInvoice.bankId())) {
            return new ResponseEntity<>(Utils.INVALID_BANK_ID, HttpStatus.BAD_REQUEST);
        }
        if (Utils.compareWithTwoDates(invoicesV1.getInvoiceStartDate(), transactionDate) > 0) {
            return new ResponseEntity<>(Utils.REFUNDING_DATE_OLDER_THAN_INVOICE, HttpStatus.BAD_REQUEST);
        }
        if (Utils.compareWithTwoDates(new Date(), transactionDate) < 0) {
            return new ResponseEntity<>(Utils.FUTURE_DATES_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
        }

        String paymentStatus = null;
        Double refundedAmount = creditDebitNoteService.getRefundedAmount(invoiceId);

        double refundableAmount = 0.0;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            if (invoicesV1.getTotalAmount() != null && invoicesV1.getTotalAmount() < 0) {
                refundableAmount = -(invoicesV1.getTotalAmount());
            } else {
                return new ResponseEntity<>(Utils.CANNOT_INITIATE_REFUND, HttpStatus.BAD_REQUEST);
            }
        } else {
            BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelId, new Date());
            if (billingDates != null && billingDates.currentBillStartDate() != null) {
                if (Utils.compareWithTwoDates(invoicesV1.getInvoiceStartDate(),
                        billingDates.currentBillStartDate()) < 0) {
                    return new ResponseEntity<>(Utils.CANNOT_REFUND_FOR_OLD_INVOICES, HttpStatus.BAD_REQUEST);
                }
            }

            if (invoicesV1.getPaidAmount() != null && invoicesV1.getPaidAmount() > 0) {
                refundableAmount = invoicesV1.getPaidAmount();
            } else {
                return new ResponseEntity<>(Utils.CANNOT_REFUND_FOR_UNPAID_INVOICES, HttpStatus.BAD_REQUEST);
            }
        }

        if (bankTransactionService.refundInvoice(invoicesV1, refundInvoice)) {
            creditDebitNoteService.refunInvoice(invoiceId, invoicesV1, refundInvoice);
        } else {
            return new ResponseEntity<>(Utils.INSUFFICIENT_BALANCE, HttpStatus.BAD_REQUEST);
        }

        if ((refundedAmount + refundInvoice.refundAmount()) == refundableAmount) {
            invoicesV1.setPaymentStatus(PaymentStatus.REFUNDED.name());
            paymentStatus = PaymentStatus.REFUNDED.name();
        } else {
            if ((refundedAmount + refundInvoice.refundAmount()) < refundableAmount) {
                invoicesV1.setPaymentStatus(PaymentStatus.PARTIAL_REFUND.name());
                paymentStatus = PaymentStatus.PARTIAL_REFUND.name();
            } else {
                invoicesV1.setPaymentStatus(PaymentStatus.REFUNDED.name());
                paymentStatus = PaymentStatus.REFUNDED.name();
            }
        }

        invoicesV1.setInvoiceEndDate(new Date());
        invoicesV1.setPaidAmount(refundedAmount + refundInvoice.refundAmount());
        invoiceService.saveInvoice(invoicesV1);

        TransactionV1 transactionV1 = new TransactionV1();
        transactionV1.setType(TransactionType.REFUND.name());
        transactionV1.setPaidAmount(refundInvoice.refundAmount());
        transactionV1.setCreatedBy(authentication.getName());
        transactionV1.setCreatedAt(new Date());
        transactionV1.setStatus(paymentStatus);
        transactionV1.setInvoiceId(invoiceId);
        transactionV1.setHostelId(hostelId);
        // transactionV1.setIsInvoice(false);
        transactionV1.setCustomerId(invoicesV1.getCustomerId());
        transactionV1.setPaymentDate(Utils.convertToTimeStamp(transactionDate));
        transactionV1.setTransactionReferenceId(generateRandomNumber());
        transactionV1.setBankId(refundInvoice.bankId());
        transactionV1.setReferenceNumber(refundInvoice.referenceNumber());
        transactionV1.setPaidAt(Utils.convertToTimeStamp(transactionDate));
        transactionV1.setUpdatedBy(authentication.getName());
        transactionRespository.save(transactionV1);

        return new ResponseEntity<>(Utils.REFUND_PROCESSED_SUCCESSFULLY, HttpStatus.OK);
    }

    public List<TransactionV1> getTransactionsByInvoiceId(String invoiceId) {
        return transactionRespository.findByInvoiceId(invoiceId);
    }

    public List<com.smartstay.smartstay.dto.customer.TransactionDto> getTranactionInfoByCustomerId(String customerId) {
        if (authentication.isAuthenticated()) {
            List<TransactionV1> listTransactions = transactionRespository.findByCustomerId(customerId);

            return listTransactions.stream()
                    .map(i -> new TransactionForCustomerDetailsMapper().apply(i))
                    .toList();
        }
        return new ArrayList<>();
    }

    public TransactionV1 getReceiptByReceiptId(String receiptId) {
        return transactionRespository.findByTransactionId(receiptId);
    }

    public DeleteReceipts deleteReceipts(String receiptId) {
        TransactionV1 transactionV1 = transactionRespository.findByTransactionId(receiptId);

        transactionRespository.delete(transactionV1);

        return new DeleteReceipts(transactionV1.getInvoiceId(),
                transactionV1.getPaidAmount(),
                true);
    }

    public ResponseEntity<?> deleteReceipt(String hostelId, String receiptId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_RECEIPT, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        TransactionV1 transactionV1 = transactionRespository.findById(receiptId).orElse(null);
        if (transactionV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_RECEIPT_ID_PASSED, HttpStatus.BAD_REQUEST);
        }
        if (!transactionV1.getTransactionMode().equalsIgnoreCase(ReceiptMode.MANUAL.name())) {
            return new ResponseEntity<>(Utils.CANNOT_DELETE_OTHER_MODE_RECEIPTS, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 invoicesV1 = invoiceService.findInvoiceDetails(transactionV1.getInvoiceId());
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND_TRANSACTION, HttpStatus.BAD_REQUEST);
        }
        if (!transactionV1.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        Customers customers = customersService.getCustomerInformation(invoicesV1.getCustomerId());
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_DELETE_RECEIPT_SETTLMENT_GENERATED, HttpStatus.BAD_REQUEST);
        }

        InvoicesV1 inv = invoiceService.deleteReceipt(invoicesV1, transactionV1);
        if (inv == null) {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }

        bankingService.deleteReceipt(transactionV1.getPaidAmount(), BankTransactionType.DEBIT.name(),
                transactionV1.getBankId());

        PaymentSummary summary = new PaymentSummary(hostelId, invoicesV1.getCustomerId(), invoicesV1.getInvoiceNumber(),
                transactionV1.getPaidAmount(), invoicesV1.getCustomerMobile(), invoicesV1.getCustomerMailId(),
                "Active");
        int response = paymentSummaryService.deleteReceipt(summary);

        if (response == 1) {

            TransactionDto transaction = new TransactionDto(transactionV1.getBankId(),
                    transactionV1.getReferenceNumber(),
                    transactionV1.getPaidAmount(),
                    BankTransactionType.DEBIT.name(),
                    BankSource.INVOICE.name(),
                    hostelId,
                    null,
                    transactionV1.getTransactionId());

            bankTransactionService.deleteReceipt(transaction);

        }

        transactionRespository.delete(transactionV1);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public int countByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return transactionRespository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
    }

    public Double sumPaidAmountByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return transactionRespository.sumPaidAmountByHostelIdAndDateRange(hostelId, startDate, endDate);
    }

    public ReceiptReportResponse getReceiptReports(String hostelId,
            Date startDate, Date endDate, List<String> paymentStatus, int page, int size) {

        int offset = page * size;
        int limit = size;

        List<TransactionV1> transactions = transactionRespository.findTransactionsByFiltersNoJoin(hostelId, startDate,
                endDate, paymentStatus, limit, offset);

        int totalTransactions = transactionRespository.countByFiltersNoJoin(hostelId, startDate, endDate,
                paymentStatus);
        int totalPages = (int) Math.ceil((double) totalTransactions / size);

        Double totalCollection = transactionRespository.findTotalCollectionByFiltersNoJoin(hostelId, startDate, endDate,
                paymentStatus);

        List<BankCollectionProjection> highestCollectingBankData = transactionRespository
                .findHighestCollectingBankNoJoin(hostelId, startDate, endDate, paymentStatus);

        ReceiptReportResponse.BankCollectionInfo bankInfo = new com.smartstay.smartstay.responses.receiptForReport.ReceiptReportResponse.BankCollectionInfo();

        if (highestCollectingBankData != null && !highestCollectingBankData.isEmpty()) {
            BankCollectionProjection data = highestCollectingBankData.get(0);
            String bankId = data.getBankId();
            Double amount = data.getTotalAmount();
            bankInfo.setTotalAmount(amount);

            BankingV1 bank = bankingService.getBankDetails(bankId);
            if (bank != null) {
                bankInfo.setBankName(bank.getAccountHolderName() + "-" + bank.getBankName());
            } else {
                bankInfo.setBankName("Unknown Bank");
            }
        }

        List<String> customerIds = transactions.stream().map(TransactionV1::getCustomerId).filter(Objects::nonNull)
                .distinct().collect(Collectors.toList());
        List<String> bankIds = transactions.stream().map(TransactionV1::getBankId).filter(Objects::nonNull).distinct()
                .toList();

        List<Customers> customers = new ArrayList<>();
        if (!customerIds.isEmpty()) {
            customers = customersService.getCustomerDetails(customerIds);
        }

        Map<String, String> customerNameMap = customers.stream()
                .collect(Collectors.toMap(Customers::getCustomerId, c -> {
                    String name = c.getFirstName();
                    if (c.getLastName() != null)
                        name += " " + c.getLastName();
                    return name;
                }, (existing, replacement) -> existing));

        Map<String, String> bankNameMap = new HashMap<>();
        for (String bId : bankIds) {
            BankingV1 b = bankingService.getBankDetails(bId);
            if (b != null) {
                String bName = b.getAccountHolderName() == null ? b.getBankName()
                        : b.getAccountHolderName() + "-" + b.getBankName();
                bankNameMap.put(bId, bName);
            }
        }

        List<ReceiptReportResponse.ReceiptDetail> receiptDetails = transactions
                .stream().map(t -> {
                    return ReceiptReportResponse.ReceiptDetail
                            .builder()
                            .transactionId(t.getTransactionId())
                            .customerName(customerNameMap.getOrDefault(t.getCustomerId(), "Unknown"))
                            .paymentDate(Utils.dateToString(t.getPaidAt()))
                            .amount(t.getPaidAmount())
                            .bankName(bankNameMap.getOrDefault(t.getBankId(), "Unknown"))
                            .transactionType(t.getType())
                            .transactionReferenceId(t.getTransactionReferenceId())
                            .referenceNumber(t.getReferenceNumber())
                            .status(t.getStatus())
                            .build();
                }).collect(Collectors.toList());

        ReceiptReportResponse.BasicReceiptDetails basicDetails = com.smartstay.smartstay.responses.receiptForReport.ReceiptReportResponse.BasicReceiptDetails
                .builder()
                .totalReceipts(totalTransactions)
                .startDate(startDate != null ? Utils.dateToString(startDate) : null)
                .endDate(endDate != null ? Utils.dateToString(endDate) : null)
                .build();

        return ReceiptReportResponse.builder()
                .totalRentCollected(totalCollection != null ? totalCollection : 0.0)
                .highestCollectingBank(bankInfo)
                .currentPage(page)
                .totalPages(totalPages)
                .basicDetails(basicDetails)
                .receipts(receiptDetails)
                .build();
    }

        public ExpenseReportResponse getExpenseReports (String hostelId, Date startDate, Date endDate,int page, int size)
        {
            int offset = page * size;
            int limit = size;

            Double totalIncome = transactionRespository.sumIncomeByHostelIdAndDateRange(hostelId, startDate, endDate);
            Double totalRefunds = transactionRespository.sumRefundByHostelIdAndDateRange(hostelId, startDate, endDate);
            Double totalHostelExpenses = expensesRepository.sumAmountByHostelIdAndDateRange(hostelId, startDate, endDate);

            double totalExpenses = totalHostelExpenses + totalRefunds;
            double balanceAmount = totalIncome - totalExpenses;
            double profit = balanceAmount > 0 ? balanceAmount : 0;
            double loss = balanceAmount < 0 ? Math.abs(balanceAmount) : 0;

            // Paginated Expenses
            List<ExpensesV1> expensesList = expensesRepository.findExpensesByFiltersNoJoin(hostelId, startDate, endDate, limit, offset);
            int totalExpensesCount = expensesRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
            int totalPages = (int) Math.ceil((double) totalExpensesCount / size);

            List<ExpenseReportResponse.ExpenseDetail> details = expensesList.stream().map(e -> {
                String categoryName = "Unknown";
                String subCategoryName = "Unknown";
                String vendorName = "Unknown";
                String bankName = "Unknown";

                // Resolve names (no joins used)
                try {
                    if (e.getCategoryId() != null) {
                        // Assuming a simple way to get name. If service doesn't have it, we'd need another repo or method.
                        // For now, let's assume we can fetch it.
                        com.smartstay.smartstay.dao.ExpenseCategory cat = expenseCategoryService.getExpenseCategoryById(e.getCategoryId());
                        if (cat != null) {
                            categoryName = cat.getCategoryName();
                            if (e.getSubCategoryId() != null && cat.getListSubCategories() != null) {
                                subCategoryName = cat.getListSubCategories().stream()
                                        .filter(s -> s.getSubCategoryId().equals(e.getSubCategoryId()))
                                        .map(com.smartstay.smartstay.dao.ExpenseSubCategory::getSubCategoryName)
                                        .findFirst().orElse("Unknown");
                            }
                        }
                    }
                    if (e.getVendorId() != null) {
                        com.smartstay.smartstay.dao.VendorV1 vendor = vendorService.getVendorObjectById(Integer.parseInt(e.getVendorId()));
                        if (vendor != null) {
                            vendorName = vendor.getBusinessName() != null ? vendor.getBusinessName() : vendor.getFirstName();
                        }
                    }
                    if (e.getBankId() != null) {
                        BankingV1 bank = bankingService.getBankDetails(e.getBankId());
                        if (bank != null) {
                            bankName = bank.getBankName();
                        }
                    }
                } catch (Exception ex) {
                    // Keep defaults if error
                }

                return ExpenseReportResponse.ExpenseDetail.builder()
                        .expenseId(e.getExpenseId())
                        .expenseNumber(e.getExpenseNumber())
                        .categoryName(categoryName)
                        .subCategoryName(subCategoryName)
                        .description(e.getDescription())
                        .amount(e.getTransactionAmount())
                        .transactionDate(Utils.dateToString(e.getTransactionDate()))
                        .bankName(bankName)
                        .vendorName(vendorName)
                        .build();
            }).collect(Collectors.toList());

            ExpenseReportResponse.ExpenseSummary summary = ExpenseReportResponse.ExpenseSummary.builder()
                    .totalCount(totalExpensesCount)
                    .startDate(startDate != null ? Utils.dateToString(startDate) : null)
                    .endDate(endDate != null ? Utils.dateToString(endDate) : null)
                    .build();

            return ExpenseReportResponse.builder()
                    .totalExpenses(totalExpenses)
                    .balanceAmount(balanceAmount)
                    .profit(profit)
                    .loss(loss)
                    .currentPage(page)
                    .totalPages(totalPages)
                    .expenseSummary(summary)
                    .expenses(details)
                    .build();
        }


}
