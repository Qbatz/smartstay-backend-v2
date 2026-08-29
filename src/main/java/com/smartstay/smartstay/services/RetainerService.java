package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.retainer.InvoiceRetainerItemMapper;
import com.smartstay.smartstay.Wrappers.retainer.InvoiceRetainerItemsMapper;
import com.smartstay.smartstay.Wrappers.retainer.InvoicesInvoiceInfoMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.customer.RetainerListItems;
import com.smartstay.smartstay.dto.retainer.RetainerInfo;
import com.smartstay.smartstay.dto.retainer.RetainerItems;
import com.smartstay.smartstay.dto.retainer.RetainerSummary;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.payloads.retainer.LoadBalance;
import com.smartstay.smartstay.payloads.retainer.RedeemAmount;
import com.smartstay.smartstay.payloads.retainer.RedeemInvoice;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.responses.InvoiceRedemption.CustomerInfo;
import com.smartstay.smartstay.responses.InvoiceRedemption.InvoiceInfo;
import com.smartstay.smartstay.responses.InvoiceRedemption.SelectedInvoiceInfo;
import com.smartstay.smartstay.responses.retainer.AvailableRetainerInvoices;
import com.smartstay.smartstay.util.CustomerUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class RetainerService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private InvoicesV1Repository invoicesV1Repository;
    @Autowired
    private UsersService usersService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TenantBankTransactionService tenantBankTransactionService;
    @Autowired
    private AdditionalContactService additionalContactService;
    @Autowired
    private RetainerRelationService retainerRelationService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private InvoiceRedemptionService invoiceRedemptionService;
    @Autowired
    private InvoiceDiscountService invoiceDiscountService;
    @Autowired
    private InvoiceNotesService invoiceNotesService;
    @Autowired
    private BankingService bankingService;

    public ResponseEntity<?> addMoney(String hostelId, String customerId, LoadBalance loadBalance) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Customers customers = customersService.getCustomerInformation(customerId);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_INVOICE, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!hostelId.equalsIgnoreCase(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (loadBalance == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (loadBalance.bankId() == null) {
            return new ResponseEntity<>(Utils.BANK_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (loadBalance.amount() == null) {
            return new ResponseEntity<>(Utils.AMOUNT_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        boolean isRegisteredRelation = false;
        if (loadBalance.relationId() == null) {
            if (loadBalance.relationName() == null) {
                return new ResponseEntity<>(Utils.RELATION_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
            }
        }
        else if (loadBalance.relationId() != null && loadBalance.relationId().trim().isEmpty() && loadBalance.relationName() == null) {
            return new ResponseEntity<>(Utils.RELATION_NAME_OR_ID_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        else if (!loadBalance.relationId().trim().isEmpty()) {
            Long relationalId = 0L;
            try {
                relationalId  = Long.parseLong(loadBalance.relationId());
            }
            catch (Exception e) {
                relationalId = 0L;
            }
            if (relationalId == 0) {
                return new ResponseEntity<>(Utils.INVALID_RELATION_ID, HttpStatus.BAD_REQUEST);
            }
            boolean isRelationExist = additionalContactService.checkRelationExistForCusotmer(customerId, relationalId);
            if (!isRelationExist) {
                isRegisteredRelation = false;
                return new ResponseEntity<>(Utils.INVALID_RELATION_ID, HttpStatus.BAD_REQUEST);
            }
            else {
                isRegisteredRelation = true;
            }
        }
        else {
            isRegisteredRelation = false;
        }

        Date paymentDate = new Date();
        if (loadBalance.paymentDate() != null) {
            paymentDate = Utils.stringToDate(loadBalance.paymentDate().replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            paymentDate = Utils.convertToTimeStamp(paymentDate);
        }

        String invoiceType = null;
        if (loadBalance.invoiceType() != null) {
            if (loadBalance.invoiceType().equalsIgnoreCase(InvoiceType.AMOUNT_HOLDING.name())) {
                invoiceType = InvoiceType.AMOUNT_HOLDING.name();
            }
            else if (loadBalance.invoiceType().equalsIgnoreCase(InvoiceType.EB_HOLDING.name())) {
                invoiceType = InvoiceType.EB_HOLDING.name();
            }
        }

        String invoiceNumber = getInvoiceNumber(hostelId);
        InvoicesV1 invoicesV1 = new InvoicesV1();
        invoicesV1.setCustomerId(customerId);
        invoicesV1.setHostelId(hostelId);
        invoicesV1.setInvoiceNumber(invoiceNumber);
        invoicesV1.setCustomerMobile(customers.getMobile());
        invoicesV1.setCustomerMailId(customers.getEmailId());
        invoicesV1.setInvoiceType(invoiceType);
        invoicesV1.setBasePrice(loadBalance.amount());
        invoicesV1.setTotalAmount(loadBalance.amount());
        invoicesV1.setPaidAmount(loadBalance.amount());
        invoicesV1.setBalanceAmount(loadBalance.amount());
        invoicesV1.setSubTotal(loadBalance.amount());
        invoicesV1.setGst(0.0);
        invoicesV1.setCgst(0.0);
        invoicesV1.setSgst(0.0);
        invoicesV1.setGstPercentile(0.0);
        invoicesV1.setPaymentStatus(PaymentStatus.PAID.name());
        invoicesV1.setDeductionAmount(0.0);
        invoicesV1.setOthersDescription(null);
        invoicesV1.setInvoiceMode(InvoiceMode.MANUAL.name());
        invoicesV1.setCancelled(false);
        invoicesV1.setDiscounted(false);
        invoicesV1.setCancelledInvoices(null);
        invoicesV1.setDeductions(null);
        invoicesV1.setCreatedBy(authentication.getName());
        invoicesV1.setInvoiceGeneratedDate(paymentDate);
        invoicesV1.setInvoiceDueDate(paymentDate);
        invoicesV1.setInvoiceDate(paymentDate);
        invoicesV1.setInvoiceStartDate(paymentDate);
        invoicesV1.setInvoiceEndDate(paymentDate);
        invoicesV1.setCreatedAt(new Date());

        InvoiceItems invoiceItems = new InvoiceItems();
        invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
        invoiceItems.setOtherItem(loadBalance.invoiceType());
        invoiceItems.setAmount(loadBalance.amount());
        invoiceItems.setInvoice(invoicesV1);

        List<InvoiceItems> listInvoiceItems = new ArrayList<>();
        listInvoiceItems.add(invoiceItems);
        invoicesV1.setInvoiceItems(listInvoiceItems);

        InvoicesV1 createdInvoice = invoicesV1Repository.save(invoicesV1);
        invoiceNotesService.addNotes(customerId, hostelId, createdInvoice.getInvoiceId(), loadBalance.description(), loadBalance.detailedDescription());
        retainerRelationService.addRelationForDeposit(customerId, hostelId, loadBalance, isRegisteredRelation, createdInvoice);
        transactionService.addRetainerTransaction(createdInvoice, loadBalance);
        tenantBankTransactionService.addRetainerTransaction(createdInvoice, loadBalance, paymentDate, isRegisteredRelation);
        usersService.addUserLog(hostelId, createdInvoice.getInvoiceId(), ActivitySource.RETAINER, ActivitySourceType.CREATE, users);


        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    private String getInvoiceNumber(String hostelId) {
        InvoicesV1 invoicesV1 = invoicesV1Repository.findByAdvanceHoldingByHostelId(hostelId);
        if (invoicesV1 == null) {
            return "RET-001";
        }
        String[] invoiceNumber = invoicesV1.getInvoiceNumber().split("-");
        if (invoiceNumber.length > 1 ) {
            String invoiceNum = invoiceNumber[1];
            Integer oldNum = 0;
            try {
               oldNum = Integer.parseInt(invoiceNum);
            }
            catch(Exception e) {

            }
            int newNum = oldNum + 1;
            if (newNum < 10) {
                return "RET-00" + newNum;
            }
            else if (newNum < 100) {
                return "RET-0" + newNum;
            }
            else if (newNum < 1000) {
                return "RET-" + newNum;
            }

            return "RET-" + String.valueOf(newNum);
        }
        else {
            return "RET-001";
        }

    }

    public ResponseEntity<?> getAllAvailableRetainers(String hostelId, String invoiceId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        InvoicesV1 invoicesV1 = invoicesV1Repository.findById(invoiceId).orElse(null);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_INVOICE, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!hostelId.equalsIgnoreCase(invoicesV1.getHostelId())) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.EB_HOLDING.name())) {
            return new ResponseEntity<>(Utils.CANNOT_APPLY_TO_EB_HOLDING, HttpStatus.BAD_REQUEST);
        }
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.AMOUNT_HOLDING.name())) {
            return new ResponseEntity<>(Utils.CANNOT_APPLY_TO_ADVANCE_HOLDING, HttpStatus.BAD_REQUEST);
        }

        List<String> invoiceTypes = new ArrayList<>();
        invoiceTypes.add(InvoiceType.EB_HOLDING.name());
        invoiceTypes.add(InvoiceType.AMOUNT_HOLDING.name());
        invoiceTypes.add(InvoiceType.ADVANCE.name());
        invoiceTypes.add(InvoiceType.BOOKING.name());

        Customers customers = customersService.getCustomerInformation(invoicesV1.getCustomerId());
        CustomerInfo customerInfo = null;
        //for bed details
        if (customers != null) {
            BookingsV1 bookingsV1 = bookingsService.getBookingsByCustomerId(customers.getCustomerId());
            if (bookingsV1 != null) {
                BedDetails bedDetails = bedsService.getBedDetails(bookingsV1.getBedId());
                if (bedDetails != null) {
                    customerInfo = new com.smartstay.smartstay.responses.InvoiceRedemption.CustomerInfo(customers.getFirstName(), customers.getLastName(), NameUtils.getFullName(customers.getFirstName(), customers.getLastName()), NameUtils.getInitials(customers.getFirstName(), customers.getLastName()), CustomerUtils.getProfilePic(customers), bedDetails.getBedName(), bedDetails.getRoomName(), bedDetails.getFloorName());
                }
            }
        }

        List<InvoiceInfo> listAvailableInvoices = null;
        SelectedInvoiceInfo selectedInvoiceInfo = null;
        AvailableRetainerInvoices availableRetainerInvoices = null;
        List<InvoicesV1> listInvoices = invoicesV1Repository.findRetainersByCustomerIdAndInvoiceTypes(invoicesV1.getCustomerId(), invoiceTypes);
        if (listInvoices != null) {
            Double totalAmount = 0.0;
            Double pendingAmount = 0.0;
            Double paidAmount = 0.0;
            if (invoicesV1.getTotalAmount() != null) {
                totalAmount = invoicesV1.getTotalAmount();
            }
            if (invoicesV1.getPaidAmount() != null) {
                paidAmount = invoicesV1.getPaidAmount();
            }

            pendingAmount = totalAmount - paidAmount;


            selectedInvoiceInfo = new SelectedInvoiceInfo(invoicesV1.getInvoiceId(),
                    invoicesV1.getInvoiceNumber(),
                    Utils.roundOffWithTwoDigit(paidAmount),
                    Utils.roundOffWithTwoDigit(pendingAmount),
                    Utils.roundOffWithTwoDigit(totalAmount),
                    invoicesV1.getPaymentStatus(),
                    invoicesV1.getInvoiceType(),
                    Utils.dateToString(invoicesV1.getInvoiceDate()),
                    Utils.dateToString(invoicesV1.getInvoiceDueDate()));

            listAvailableInvoices = listInvoices
                    .stream()
                    .map(i -> new InvoicesInvoiceInfoMapper().apply(i))
                    .toList();


        }
        availableRetainerInvoices = new AvailableRetainerInvoices(customerInfo,
                listAvailableInvoices,
                selectedInvoiceInfo);
        return new ResponseEntity<>(availableRetainerInvoices, HttpStatus.OK);
    }

    public ResponseEntity<?> redeemAmountToInvoice(String hostelId, String invoiceId, RedeemAmount redeemAmount) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        InvoicesV1 invoicesV1 = invoicesV1Repository.findById(invoiceId).orElse(null);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_INVOICE, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!hostelId.equalsIgnoreCase(invoicesV1.getHostelId())) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.EB_HOLDING.name())) {
            return new ResponseEntity<>(Utils.CANNOT_APPLY_TO_EB_HOLDING, HttpStatus.BAD_REQUEST);
        }
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.AMOUNT_HOLDING.name())) {
            return new ResponseEntity<>(Utils.CANNOT_APPLY_TO_ADVANCE_HOLDING, HttpStatus.BAD_REQUEST);
        }
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            return new ResponseEntity<>(Utils.CANNOT_APPLY_TO_ADVANCE_INVOICE, HttpStatus.BAD_REQUEST);
        }
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            return new ResponseEntity<>(Utils.CANNOT_APPLY_TO_BOOKING_INVOICE, HttpStatus.BAD_REQUEST);
        }
        if (invoicesV1.isCancelled()) {
            return new ResponseEntity<>(Utils.CANNOT_APPLY_TO_CANCELLED_INVOICES, HttpStatus.BAD_REQUEST);
        }

        double discountedAmount = 0.0;
        if (invoicesV1.isDiscounted()) {
            discountedAmount = invoiceDiscountService.getDiscountAmount(hostelId, invoiceId);
        }

        if (redeemAmount == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        Date redeemedAt = new Date();
        if (redeemAmount.redeemedOn() != null && !redeemAmount.redeemedOn().isEmpty()) {
            redeemedAt = Utils.stringToDate(redeemAmount.redeemedOn(), Utils.USER_INPUT_DATE_FORMAT);
        }

        Double appliedAmount = 0.0;
        if (redeemAmount.appliedAmount() != null) {
            appliedAmount = redeemAmount.appliedAmount();
            if (appliedAmount > 0) {
                return applyAmountSystematically(hostelId, invoicesV1, appliedAmount, redeemedAt);
            }

        }

        if (redeemAmount.retainersBreakup() == null) {
            return new ResponseEntity<>(Utils.SOURCE_INVOICES_REQUIRED_REDEMPTION, HttpStatus.BAD_REQUEST);
        }

        if (redeemAmount.retainersBreakup().isEmpty()) {
            return new ResponseEntity<>(Utils.SOURCE_INVOICES_REQUIRED_REDEMPTION, HttpStatus.BAD_REQUEST);
        }

        RedeemInvoice ri = redeemAmount.retainersBreakup()
                .stream()
                .filter(i -> i.amount() == null || i.amount() == 0)
                .findFirst()
                .orElse(null);
        if (ri != null) {
            return new ResponseEntity<>(Utils.APPLIED_AMOUNT_REQUIRED_FOR_REDEEM_LIST, HttpStatus.BAD_REQUEST);
        }

        List<String> sourceInvoiceIds = redeemAmount
                .retainersBreakup()
                .stream()
                .map(RedeemInvoice::invoiceId)
                .toList();

        if (sourceInvoiceIds == null) {
            return new ResponseEntity<>(Utils.SOURCE_INVOICES_REQUIRED_REDEMPTION, HttpStatus.BAD_REQUEST);
        }
        if (sourceInvoiceIds.isEmpty()) {
            return new ResponseEntity<>(Utils.SOURCE_INVOICES_REQUIRED_REDEMPTION, HttpStatus.BAD_REQUEST);
        }

        List<InvoicesV1> listSourceInvoices = invoicesV1Repository.findByInvoiceIdIn(sourceInvoiceIds);

        AtomicBoolean shouldThrowInvalidInvoiceError = new AtomicBoolean(false);
        AtomicBoolean shouldThrowInvalidAmountError = new AtomicBoolean(false);
        HashMap<String, Double> appliedInvoicesList = new HashMap<>();
        listSourceInvoices.forEach(item -> {
            RedeemInvoice ri2 = redeemAmount
                    .retainersBreakup()
                    .stream()
                    .filter(i -> i.invoiceId().equalsIgnoreCase(item.getInvoiceId()))
                    .findFirst()
                    .orElse(null);
            if (ri2 == null) {
                shouldThrowInvalidInvoiceError.set(true);
            }
            if (item.getBalanceAmount() < ri2.amount()) {
                shouldThrowInvalidAmountError.set(true);
            }
            if (ri2 != null) {
                appliedInvoicesList.put(item.getInvoiceId(), ri2.amount());
            }

        });

        if (shouldThrowInvalidInvoiceError.get()) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID_ON_SOURCE_LIST, HttpStatus.BAD_REQUEST);
        }
        if (shouldThrowInvalidAmountError.get()) {
            return new ResponseEntity<>(Utils.APPLIED_AMOUNT_REQUIRED_FOR_REDEEM_LIST, HttpStatus.BAD_REQUEST);
        }

        double appliedAmountFromList = redeemAmount.retainersBreakup()
                .stream()
                .mapToDouble(RedeemInvoice::amount)
                .sum();
        if (appliedAmountFromList == 0) {
            return new ResponseEntity<>(Utils.APPLIED_AMOUNT_REQUIRED_FOR_REDEEM_LIST, HttpStatus.BAD_REQUEST);
        }

        double paidAmount = 0.0;
        if (invoicesV1.getPaidAmount() != null) {
            paidAmount = invoicesV1.getPaidAmount();
        }
        double newPaidAmount = paidAmount + appliedAmountFromList;

        invoicesV1.setPaidAmount(newPaidAmount);
        if (newPaidAmount == invoicesV1.getTotalAmount()) {
            invoicesV1.setPaymentStatus(PaymentStatus.PAID.name());
        }
        else {
            invoicesV1.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
        }

        invoiceRedemptionService.applyInvoiceFromRetainer(hostelId, invoicesV1.getInvoiceId(), appliedInvoicesList, redeemedAt);
        tenantBankTransactionService.addRetainerTransactionForRedemption(hostelId, invoicesV1.getInvoiceId(), invoicesV1.getCustomerId(), appliedInvoicesList, appliedAmount, redeemedAt);
        invoicesV1Repository.save(invoicesV1);

        List<InvoicesV1> newInvoices = new ArrayList<>();
        if (!appliedInvoicesList.isEmpty()) {
            appliedInvoicesList.keySet().forEach(item -> {
                InvoicesV1 updateInvoice = listSourceInvoices
                        .stream()
                        .filter(i -> i.getInvoiceId().equalsIgnoreCase(item))
                        .findFirst()
                        .orElse(null);
                if (updateInvoice != null) {
                    double balance = 0.0;
                    if (updateInvoice.getBalanceAmount() != null) {
                        balance = updateInvoice.getBalanceAmount() - appliedInvoicesList.get(item);
                    }

                    updateInvoice.setBalanceAmount(balance);

                    newInvoices.add(updateInvoice);
                }
            });
        }

        if (!newInvoices.isEmpty()) {
            invoicesV1Repository.saveAll(newInvoices);
            usersService.addUserLog(hostelId, invoicesV1.getCustomerId(), ActivitySource.RETAINER, ActivitySourceType.REDEEMED, users);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<?> applyAmountSystematically(String hostelId, InvoicesV1 invoicesV1, Double appliedAmount, Date redeemedAt) {
        List<String> retainerInvoices = new ArrayList<>();
        retainerInvoices.add(InvoiceType.ADVANCE.name());
        retainerInvoices.add(InvoiceType.AMOUNT_HOLDING.name());
        retainerInvoices.add(InvoiceType.EB_HOLDING.name());

        List<InvoicesV1> listAvailableInvoices = invoicesV1Repository.findRetainersByCustomerIdAndInvoiceTypes(invoicesV1.getCustomerId(), retainerInvoices);
        if (listAvailableInvoices == null) {
            return new ResponseEntity<>(Utils.NO_RETAINER_INVOICE_AVAILABLE, HttpStatus.BAD_REQUEST);
        }
        if (listAvailableInvoices.isEmpty()) {
            return new ResponseEntity<>(Utils.NO_RETAINER_INVOICE_AVAILABLE, HttpStatus.BAD_REQUEST);
        }

        double availableAmount = listAvailableInvoices
                .stream()
                .mapToDouble(i -> {
                    if (i.getBalanceAmount() == null) {
                        return 0.0;
                    }
                    return i.getBalanceAmount();
                })
                .sum();
        if (availableAmount < appliedAmount) {
            return new ResponseEntity<>(Utils.INSUFFICIENT_AMOUNT_TO_REDEEM, HttpStatus.BAD_REQUEST);
        }
        AtomicReference<Double> newBalance = new AtomicReference<>(appliedAmount);
        HashMap<String, Double> appliedAmountInvoiceIdMapper = new HashMap<>();

        List<InvoicesV1> listNewInvoiceAfterRedeeming = listAvailableInvoices
                .stream()
                .map(i -> {
                    if (newBalance.get() > 0) {
                        if (i.getBalanceAmount() > appliedAmount) {
                            appliedAmountInvoiceIdMapper.put(i.getInvoiceId(), appliedAmount);
                            i.setBalanceAmount(i.getBalanceAmount() - appliedAmount);
                            newBalance.set(0.0);
                        }
                        else {
                            double redeemingAmountAfterCurrentInvoice = newBalance.get() - i.getBalanceAmount();
                            if (redeemingAmountAfterCurrentInvoice >= 0) {
                                appliedAmountInvoiceIdMapper.put(i.getInvoiceId(), i.getBalanceAmount());
                                i.setBalanceAmount(0.0);
                                newBalance.set(newBalance.get() - redeemingAmountAfterCurrentInvoice);
                            }
                            else {
                                double newBalanceAmount = i.getBalanceAmount() - newBalance.get();
                                i.setBalanceAmount(newBalanceAmount);
                                appliedAmountInvoiceIdMapper.put(i.getInvoiceId(), newBalance.get());
                                newBalance.set(newBalance.get());
                            }
                        }
                    }
                    return i;
                })
                .toList();

        invoiceRedemptionService.applyInvoiceFromRetainer(hostelId, invoicesV1.getInvoiceId(), appliedAmountInvoiceIdMapper, redeemedAt);
        tenantBankTransactionService.addRetainerTransactionForRedemption(hostelId, invoicesV1.getInvoiceId(), invoicesV1.getCustomerId(), appliedAmountInvoiceIdMapper, appliedAmount, redeemedAt);
        double invoicePaid = 0.0;
        if (invoicesV1.getPaidAmount() != null) {
            invoicePaid = invoicesV1.getPaidAmount();
        }

        double newPaidAmount = invoicePaid + appliedAmount;


        invoicesV1.setPaidAmount(newPaidAmount);
        if (invoicesV1.getTotalAmount() == newPaidAmount) {
            invoicesV1.setPaymentStatus(PaymentStatus.PAID.name());
        }
        else {
            invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
        }
        invoicesV1Repository.save(invoicesV1);

        if (!listNewInvoiceAfterRedeeming.isEmpty()) {
            invoicesV1Repository.saveAll(listNewInvoiceAfterRedeeming);
        }

        return new ResponseEntity<>(HttpStatus.OK);

    }

    public RetainerInfo getAvailableRetainersByCustomerForSettlement(String customerId) {
        List<String> invoiceTypes = new ArrayList<>();
        invoiceTypes.add(InvoiceType.EB_HOLDING.name());
        invoiceTypes.add(InvoiceType.AMOUNT_HOLDING.name());

        List<InvoicesV1> listInvoices = invoicesV1Repository.findRetainersByCustomerIdAndInvoiceTypes(customerId, invoiceTypes);
        if (listInvoices == null) {
            listInvoices = new ArrayList<>();
        }
        double totalRetinerValue = listInvoices
                .stream()
                .mapToDouble(i -> {
                    if (i.getTotalAmount() == null) {
                        return 0.0;
                    }
                    return i.getTotalAmount();
                })
                .sum();
        double availableRetainerValue = listInvoices
                .stream()
                .mapToDouble(i -> {
                    if (i.getBalanceAmount() == null) {
                        return 0.0;
                    }
                    return i.getBalanceAmount();
                })
                .sum();
        List<RetainerItems> listRetainerItems = listInvoices
                .stream()
                .map(i -> new InvoiceRetainerItemMapper().apply(i))
                .toList();

        return new RetainerInfo(listInvoices.size(),
                availableRetainerValue,
                totalRetinerValue,
                listRetainerItems);

    }

    public void updateBalanceOfRetainerInvoices(String customerId) {
        List<String> retainerInvoices = new ArrayList<>();
        retainerInvoices.add(InvoiceType.AMOUNT_HOLDING.name());
        retainerInvoices.add(InvoiceType.EB_HOLDING.name());

        List<InvoicesV1> listAvailableInvoices = invoicesV1Repository.findRetainersByCustomerIdAndInvoiceTypes(customerId, retainerInvoices);
        if (listAvailableInvoices != null && !listAvailableInvoices.isEmpty()) {
            List<InvoicesV1> listInvoicesUpdates = listAvailableInvoices
                    .stream()
                    .peek(i -> i.setBalanceAmount(0.0))
                    .toList();
            invoicesV1Repository.saveAll(listInvoicesUpdates);
        }
    }

    public com.smartstay.smartstay.dto.customer.RetainerInfo getRetaineListByCUstomerId(String hostelId, String customerId) {
        List<String> invoiceTypes = new ArrayList<>();
        invoiceTypes.add(InvoiceType.ADVANCE.name());
        invoiceTypes.add(InvoiceType.BOOKING.name());
        invoiceTypes.add(InvoiceType.EB_HOLDING.name());
        invoiceTypes.add(InvoiceType.AMOUNT_HOLDING.name());

        List<TransactionV1> listRetainerTransactions;
        List<BankingV1> listBankings;

        List<InvoicesV1> listInvoices = invoicesV1Repository.findByCustomerIdAndInvoiceTypeIn(customerId, invoiceTypes);
        if (listInvoices != null) {
            listInvoices = listInvoices
                    .stream()
                    .filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name()))
                    .toList();
        }

        if (listInvoices != null) {
            List<String> invoiceIds = listInvoices
                    .stream()
                    .map(InvoicesV1::getInvoiceId)
                    .toList();
            listRetainerTransactions = transactionService.getLatestTransactions(hostelId, invoiceIds);
            if (listRetainerTransactions != null) {
                Set<String> bankIds = listRetainerTransactions
                        .stream()
                        .map(TransactionV1::getBankId)
                        .collect(Collectors.toSet());
                if (bankIds != null) {
                    listBankings = bankingService.findAllBanksById(bankIds);
                }
                else {
                    listBankings = null;
                }
            } else {
                listBankings = null;
            }
        } else {
            listBankings = null;
            listRetainerTransactions = null;
        }

        RetainerSummary summary = new RetainerSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        com.smartstay.smartstay.dto.customer.RetainerInfo retainerInfo = new com.smartstay.smartstay.dto.customer.RetainerInfo(summary, null);

        if (listInvoices != null) {
            Double totalRetainerAmount = 0.0;
            Double totalAdvanceAmount = 0.0;
            Double totalBookingAmount = 0.0;
            Double totalRentAmount = 0.0;
            Double totalEbAmount = 0.0;
            Double totalOtherAmount = 0.0;

            totalRetainerAmount = listInvoices
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getBalanceAmount() != null) {
                            return i.getBalanceAmount();
                        }
                        return 0.0;
                    })
                    .sum();

            totalAdvanceAmount = listInvoices
                    .stream()
                    .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name()))
                    .mapToDouble(i -> {
                        if (i.getBalanceAmount() != null) {
                            return i.getBalanceAmount();
                        }
                        return 0.0;
                    })
                    .sum();
            totalBookingAmount = listInvoices
                    .stream()
                    .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()))
                    .mapToDouble(i -> {
                        if (i.getBalanceAmount() != null) {
                            return i.getBalanceAmount();
                        }
                        return 0.0;
                    })
                    .sum();

            totalRentAmount = listInvoices
                    .stream()
                    .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.AMOUNT_HOLDING.name()))
                    .mapToDouble(i -> {
                        if (i.getBalanceAmount() != null) {
                            return i.getBalanceAmount();
                        }
                        return 0.0;
                    })
                    .sum();
            totalEbAmount = listInvoices
                    .stream()
                    .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.EB_HOLDING.name()))
                    .mapToDouble(i -> {
                        if (i.getBalanceAmount() != null) {
                            return i.getBalanceAmount();
                        }
                        return 0.0;
                    })
                    .sum();

            RetainerSummary retainerSummary = new RetainerSummary(Utils.roundOffWithTwoDigit(totalRetainerAmount),
                    Utils.roundOffWithTwoDigit(totalBookingAmount),
                    Utils.roundOffWithTwoDigit(totalAdvanceAmount),
                    Utils.roundOffWithTwoDigit(totalEbAmount),
                    Utils.roundOffWithTwoDigit(totalRentAmount),
                    0.0);
            List<RetainerListItems> retainerItems = listInvoices
                    .stream()
                    .map(i -> new InvoiceRetainerItemsMapper(listRetainerTransactions, listBankings).apply(i))
                    .toList();

            return new com.smartstay.smartstay.dto.customer.RetainerInfo(retainerSummary, retainerItems);

        }

        return retainerInfo;
    }
}
