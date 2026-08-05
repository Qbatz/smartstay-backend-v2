package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.retainer.InvoicesInvoiceInfoMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.ennum.InvoiceMode;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.payloads.retainer.LoadBalance;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        else {
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

        retainerRelationService.addRelationForDeposit(customerId, hostelId, loadBalance, isRegisteredRelation, createdInvoice);
        transactionService.addRetainerTransaction(createdInvoice, loadBalance);
        tenantBankTransactionService.addRetainerTransaction(createdInvoice, loadBalance, paymentDate, isRegisteredRelation);


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
                return "RET-000" + newNum;
            }
            else if (newNum < 100) {
                return "RET-00" + newNum;
            }
            else if (newNum < 1000) {
                return "RET-0" + newNum;
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
}
