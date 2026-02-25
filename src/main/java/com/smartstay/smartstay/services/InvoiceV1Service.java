package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Bills.ReceiptMapper;
import com.smartstay.smartstay.Wrappers.invoices.InitializeRefund;
import com.smartstay.smartstay.Wrappers.invoices.InvoiceMapper;
import com.smartstay.smartstay.Wrappers.invoices.NewInvoiceListMapper;
import com.smartstay.smartstay.Wrappers.transactions.TransactionsListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.RestTemplateLoggingInterceptor;
import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.bank.PaymentHistoryProjection;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.bills.BillTemplates;
import com.smartstay.smartstay.dto.bills.PaymentSummary;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.customer.InvoiceRefundHistory;
import com.smartstay.smartstay.dto.customer.ReassignRent;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.invoices.InvoiceAggregateDto;
import com.smartstay.smartstay.dto.invoices.InvoiceCustomer;
import com.smartstay.smartstay.dto.transaction.Receipts;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.events.RecurringEvents;
import com.smartstay.smartstay.filterOptions.invoice.CreatedBy;
import com.smartstay.smartstay.filterOptions.invoice.InvoiceFilterOptions;
import com.smartstay.smartstay.payloads.invoice.InvoiceResponse;
import com.smartstay.smartstay.payloads.invoice.ItemResponse;
import com.smartstay.smartstay.payloads.invoice.ManualInvoice;
import com.smartstay.smartstay.payloads.invoice.UpdateRecurringInvoice;
import com.smartstay.smartstay.repositories.BillingRuleRepository;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.responses.invoices.*;
import com.smartstay.smartstay.util.InvoiceUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvoiceV1Service {
    private final RestTemplate restTemplate;
    @Autowired
    InvoicesV1Repository invoicesV1Repository;
    @Autowired
    Authentication authentication;
    @Autowired
    TemplatesService templateService;
    @Autowired
    PaymentSummaryService paymentSummaryService;
    @Value("${REPORTS_URL}")
    private String reportsUrl;
    @Autowired
    private UsersService usersService;
    private CustomersService customersService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private TemplatesService templatesService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private CustomersBedHistoryService customersBedHistoryService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private InvoiceItemService invoiceItemService;
    @Autowired
    private CreditDebitNoteService creditDebitNoteService;
    @Autowired
    private RentHistoryService rentHistoryService;
    @Autowired
    private BillingRuleRepository billingRuleRepository;
    @Autowired
    private WhatsAppService whatsappService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    private TransactionService transactionService;

    private BookingsService bookingsService;
    private HostelService hostelService;
    private BedsService bedService;

    public InvoiceV1Service(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    @Autowired
    public void setCustomersService(@Lazy CustomersService customersService) {
        this.customersService = customersService;
    }

    @Autowired
    public void setBookingsService(@Lazy BookingsService bookingService) {
        this.bookingsService = bookingService;
    }

    @Autowired
    public void setHostelService(@Lazy HostelService hostelService) {
        this.hostelService = hostelService;
    }

    @Autowired
    public void setBedsService(@Lazy BedsService bedsService) {
        this.bedService = bedsService;
    }

    @Autowired
    public void setTransactionService(@Lazy TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void addInvoice(String customerId, Double amount, String type, String hostelId, String customerMobile, String customerMailId, String joiningDate, BillingDates billingDates) {
        if (authentication.isAuthenticated()) {
            StringBuilder invoiceNumber = new StringBuilder();
            BillTemplates templates = templateService.getBillTemplate(hostelId, type);
            InvoicesV1 existingV1 = null;

            double gstAmount = 0;
            double gstPercentile = 0;
            double baseAmount = 0;
            double cgst = 0;
            double sgst = 0;

            if (templates != null) {

                if (templates.gstPercentile() != null) {
                    gstPercentile = templates.gstPercentile();
                    cgst = templates.gstPercentile() / 2;
                    sgst = templates.gstPercentile() / 2;
                    baseAmount = amount / (1 + (templates.gstPercentile() / 100));
                    gstAmount = amount - baseAmount;
                    if (baseAmount == 0) {
                        baseAmount = amount;
                    }
                }

                invoiceNumber.append(templates.prefix());
                invoiceNumber.append("-");
                invoiceNumber.append(templates.suffix());
                existingV1 = invoicesV1Repository.findLatestInvoiceByPrefix(templates.prefix(), hostelId);
            }
            InvoicesV1 invoicesV1 = new InvoicesV1();
            if (existingV1 != null) {
                invoiceNumber = new StringBuilder();
                invoiceNumber.append(templates.prefix());

                String[] suffix = existingV1.getInvoiceNumber().split("-");
                if (suffix.length > 1) {
                    invoiceNumber.append("-");
                    int suff = Integer.parseInt(suffix[1]) + 1;
                    invoiceNumber.append(String.format("%03d", suff));
                } else {
                    invoiceNumber.append("-00");
                    invoiceNumber.append("1");

                }
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Utils.USER_INPUT_DATE_FORMAT);
            Date joiningDate1 = Utils.stringToDate(joiningDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            Date dueDate = Utils.addDaysToDate(joiningDate1, billingDates.dueDays());
            Date endDate = billingDates.currentBillEndDate();

            invoicesV1.setTotalAmount(amount);
            invoicesV1.setBasePrice(baseAmount);
            invoicesV1.setInvoiceType(type);
            invoicesV1.setCustomerId(customerId);
            invoicesV1.setInvoiceNumber(invoiceNumber.toString());
            invoicesV1.setPaidAmount(0.0);
            invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
            invoicesV1.setCreatedBy(authentication.getName());
            invoicesV1.setGst(gstAmount);
            invoicesV1.setCgst(cgst);
            invoicesV1.setSgst(sgst);
            invoicesV1.setGstPercentile(gstPercentile);
            invoicesV1.setInvoiceDueDate(Utils.convertToTimeStamp(dueDate));
            invoicesV1.setCustomerMobile(customerMobile);
            invoicesV1.setCustomerMailId(customerMailId);
            invoicesV1.setCreatedAt(new Date());
            invoicesV1.setInvoiceStartDate(Utils.convertToTimeStamp(joiningDate1));
            invoicesV1.setInvoiceEndDate(Utils.convertToTimeStamp(endDate));
            invoicesV1.setInvoiceGeneratedDate(Utils.convertToTimeStamp(joiningDate1));
            invoicesV1.setInvoiceMode(InvoiceMode.AUTOMATIC.name());
            invoicesV1.setCancelled(false);
            invoicesV1.setHostelId(hostelId);

            List<InvoiceItems> listInvoiceItems = new ArrayList<>();

            InvoiceItems invoiceItems = new InvoiceItems();
            invoiceItems.setInvoice(invoicesV1);
            if (type.equalsIgnoreCase(InvoiceType.RENT.name())) {
                invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
            } else if (type.equalsIgnoreCase(InvoiceType.BOOKING.name())) {
                invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.BOOKING.name());
            } else if (type.equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.ADVANCE.name());
            }

            invoiceItems.setAmount(amount);
            listInvoiceItems.add(invoiceItems);

            invoicesV1.setInvoiceItems(listInvoiceItems);

            invoicesV1Repository.save(invoicesV1);
            String status = null;
            if (type.equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                status = "Active";
            } else if (type.equalsIgnoreCase(InvoiceType.RENT.name())) {
                status = "Active";
            }

            PaymentSummary summary = new PaymentSummary(hostelId, customerId, invoiceNumber.toString(), amount, customerMailId, customerMobile, status);
            paymentSummaryService.addInvoice(summary);
        }


    }

    /**
     * this is used only for booking purpose. Do not use it any where
     */
    public String addBookingInvoice(String customerId, Double amount, String type, String hostelId, String customerMobile, String customerMailId, String bankId, String referenceNumber, Date bookingDate) {
        if (authentication.isAuthenticated()) {
            StringBuilder invoiceNumber = new StringBuilder();
            BillTemplates templates = templateService.getBillTemplate(hostelId, InvoiceType.ADVANCE.name());
            InvoicesV1 existingV1 = null;

            double gstAmount = 0;
            double gstPercentile = 0;
            double basePrice = 0;
            double cgst = 0;
            double sgst = 0;

            if (templates != null) {

                if (templates.gstPercentile() != null) {
                    gstPercentile = templates.gstPercentile();
                    cgst = templates.gstPercentile() / 2;
                    sgst = templates.gstPercentile() / 2;
                    basePrice = amount / (1 + (templates.gstPercentile() / 100));
                    gstAmount = amount - basePrice;
                }

                invoiceNumber.append(templates.prefix());
                invoiceNumber.append("-");
                invoiceNumber.append(templates.suffix());
                existingV1 = invoicesV1Repository.findLatestInvoiceByPrefix(templates.prefix(), hostelId);
            }
            InvoicesV1 invoicesV1 = new InvoicesV1();
            if (existingV1 != null) {
                invoiceNumber = new StringBuilder();
                invoiceNumber.append(templates.prefix());

                String[] suffix = existingV1.getInvoiceNumber().split("-");
                if (suffix.length > 1) {
                    invoiceNumber.append("-");
                    int suff = Integer.parseInt(suffix[1]) + 1;
                    invoiceNumber.append(String.format("%03d", suff));
                }
            }


            invoicesV1.setBasePrice(basePrice);
            invoicesV1.setTotalAmount(amount);
            invoicesV1.setPaidAmount(amount);
            invoicesV1.setInvoiceType(type);
            invoicesV1.setCustomerId(customerId);
            invoicesV1.setInvoiceNumber(invoiceNumber.toString());
            invoicesV1.setInvoiceType(InvoiceType.BOOKING.name());
            invoicesV1.setPaymentStatus(PaymentStatus.PAID.name());
            invoicesV1.setCreatedBy(authentication.getName());
            invoicesV1.setInvoiceStartDate(bookingDate);
            invoicesV1.setInvoiceEndDate(bookingDate);
            invoicesV1.setInvoiceDueDate(bookingDate);
            invoicesV1.setCustomerMobile(customerMobile);
            invoicesV1.setCustomerMailId(customerMailId);
            invoicesV1.setCgst(cgst);
            invoicesV1.setSgst(sgst);
            invoicesV1.setGst(gstAmount);
            invoicesV1.setGstPercentile(gstPercentile);
            invoicesV1.setCreatedAt(new Date());
            invoicesV1.setInvoiceGeneratedDate(new Date());
            invoicesV1.setInvoiceMode(InvoiceMode.AUTOMATIC.name());
            invoicesV1.setCancelled(false);
            invoicesV1.setHostelId(hostelId);

            InvoiceItems invoiceItems = new InvoiceItems();
            invoiceItems.setAmount(amount);
            invoiceItems.setInvoiceItem(InvoiceType.BOOKING.name());
            invoiceItems.setInvoice(invoicesV1);
            List<InvoiceItems> listInvoiceItems = new ArrayList<>();
            listInvoiceItems.add(invoiceItems);

            invoicesV1.setInvoiceItems(listInvoiceItems);


            InvoicesV1 invV1 = invoicesV1Repository.save(invoicesV1);

            return invV1.getInvoiceId();
        }
        return null;
    }

    /**
     * this should be called only for bookings
     *
     * @param customerId
     * @param amount
     * @param type
     * @param hostelId
     * @param customerMobile
     * @param customerMailId
     */
    public void addReceipt(String customerId, Double amount, String type, String hostelId, String customerMobile, String customerMailId) {

        if (authentication.isAuthenticated()) {
            StringBuilder invoiceNumber = new StringBuilder();
            BillTemplates templates = templateService.getBillTemplate(hostelId, InvoiceType.BOOKING.name());
            InvoicesV1 existingV1 = null;
            double gstAmount = 0;
            double gstPercentile = 0;
            double basePrice = 0;
            double cgst = 0;
            double sgst = 0;

            if (templates != null) {
                if (templates.gstPercentile() != null) {
                    gstPercentile = templates.gstPercentile();
                    basePrice = amount / (1 + (templates.gstPercentile() / 100));
                    gstAmount = amount - basePrice;
                    cgst = templates.gstPercentile() / 2;
                    sgst = templates.gstPercentile() / 2;
                }

                invoiceNumber.append(templates.prefix());
                invoiceNumber.append("-");
                invoiceNumber.append(templates.suffix());
                existingV1 = invoicesV1Repository.findLatestInvoiceByPrefix(templates.prefix(), hostelId);
            }
            InvoicesV1 invoicesV1 = new InvoicesV1();
            if (existingV1 != null) {
                invoiceNumber = new StringBuilder();
                invoiceNumber.append(templates.prefix());

                String[] suffix = existingV1.getInvoiceNumber().split("-");
                if (suffix.length > 1) {
                    invoiceNumber.append("-");
                    int suff = Integer.parseInt(suffix[1]) + 1;
                    invoiceNumber.append(String.format("%03d", suff));
                }
            }

            invoicesV1.setBasePrice(Math.ceil(basePrice));
            invoicesV1.setTotalAmount(Math.ceil(amount));
            invoicesV1.setInvoiceType(type);
            invoicesV1.setCustomerId(customerId);
            invoicesV1.setInvoiceNumber(invoiceNumber.toString());
            invoicesV1.setPaymentStatus(PaymentStatus.PAID.name());
            invoicesV1.setCreatedBy(authentication.getName());
            invoicesV1.setInvoiceDueDate(Utils.addDaysToDate(new Date(), 0));
            invoicesV1.setCustomerMobile(customerMobile);
            invoicesV1.setCustomerMailId(customerMailId);
            invoicesV1.setGst(gstAmount);
            invoicesV1.setCgst(cgst);
            invoicesV1.setSgst(sgst);
            invoicesV1.setGstPercentile(gstPercentile);
            invoicesV1.setCreatedAt(new Date());
            invoicesV1.setInvoiceGeneratedDate(new Date());
            invoicesV1.setCancelled(false);
            invoicesV1.setInvoiceMode(InvoiceMode.AUTOMATIC.name());
            invoicesV1.setHostelId(hostelId);


            invoicesV1Repository.save(invoicesV1);
            String status = null;
            if (type.equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                status = "Active";
            } else if (type.equalsIgnoreCase(InvoiceType.RENT.name())) {
                status = "Active";
            }

            PaymentSummary summary = new PaymentSummary(hostelId, customerId, invoiceNumber.toString(), amount, customerMailId, customerMobile, status);
            paymentSummaryService.addInvoice(summary);
        }

    }

    /**
     * Using in transaction service.
     * Changing anything here may impact in trasaction service
     * <p>
     * userd inside transaction service
     *
     * @param invoiceId
     * @return
     */
    public InvoicesV1 findInvoiceDetails(String invoiceId) {
        return invoicesV1Repository.findById(invoiceId).orElse(null);
    }

    public ResponseEntity<?> getAllInvoices(String hostelId, String startDate, String endDate, List<String> types, List<String> createdBy, List<String> mode, String searchKey, List<String> paymentStatus) {
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
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        Date dStartDate = null;
        Date dEndDate = null;
        if (startDate != null) {
            dStartDate = Utils.stringToDate(startDate.replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }
        if (endDate != null) {
            dEndDate = Utils.stringToDate(endDate.replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }
        List<String> invoiceTypes = null;
        if (types != null) {
            invoiceTypes = types;
        } else {
            invoiceTypes = new ArrayList<>();
            invoiceTypes.add(InvoiceType.RENT.name());
            invoiceTypes.add(InvoiceType.SETTLEMENT.name());
            invoiceTypes.add(InvoiceType.ADVANCE.name());
            invoiceTypes.add(InvoiceType.REASSIGN_RENT.name());
        }

        List<String> createdByUsers = null;
        if (createdBy != null && !createdBy.isEmpty()) {
            createdByUsers = createdBy;
        }

        List<String> modes = null;
        if (mode != null && !mode.isEmpty()) {
            modes = mode;
        }
        List<String> pStatus = null;
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            pStatus = paymentStatus;
        }

        List<String> userIds = null;
        if (searchKey != null && !searchKey.trim().equalsIgnoreCase("")) {
            List<Customers> listCustomers = customersService.searchCustomerByHostelName(hostelId, searchKey.trim());
            userIds = listCustomers.stream().map(Customers::getCustomerId).toList();
        }

        List<UserHostel> listAdminUsers = userHostelService.findAllByHostelId(hostelId);
        List<String> adminIds = listAdminUsers.stream().map(UserHostel::getUserId).toList();
        List<Users> adminUsers = usersService.findAllUsersFromUserId(adminIds);

        List<CreatedBy> listCreatedBy = adminUsers.stream().map(i -> {
            StringBuilder fullName = new StringBuilder();
            if (i.getFirstName() != null) {
                fullName.append(i.getFirstName());
            }
            if (i.getLastName() != null && i.getLastName().trim().equalsIgnoreCase("")) {
                fullName.append(" ");
                fullName.append(i.getLastName());
            }
            return new CreatedBy(fullName.toString(), i.getUserId());
        }).toList();

        InvoiceFilterOptions invoiceFilterOptions = new InvoiceFilterOptions();
        if (listCreatedBy != null) {
            invoiceFilterOptions.setCreatedBy(listCreatedBy);
        }

        List<InvoicesV1> listAllInvoice = invoicesV1Repository.findAllInvoicesByHostelId(hostelId, dStartDate, dEndDate, invoiceTypes, createdByUsers, modes, pStatus, userIds);

        List<String> customerIds = listAllInvoice.stream().map(InvoicesV1::getCustomerId).toList();
        List<Customers> lisAllCustomersForInvoices = customersService.getCustomerDetails(customerIds);

        List<InvoicesList> newInvoicesList = listAllInvoice.stream().map(i -> new NewInvoiceListMapper(lisAllCustomersForInvoices, adminUsers).apply(i)).toList();

        NewInvoicesList newInvoicesListResponse = new NewInvoicesList(hostelId, invoiceFilterOptions, newInvoicesList);
        return new ResponseEntity<>(newInvoicesListResponse, HttpStatus.OK);
    }

    public int recordPayment(String invoiceId, String status, double amount) {
        InvoicesV1 invoice = invoicesV1Repository.findById(invoiceId).orElse(null);
        if (invoice != null) {
            double paidAmount = 0.0;
            if (invoice.getPaidAmount() != null) {
                paidAmount = invoice.getPaidAmount();
            }
            invoice.setPaymentStatus(status);
            invoice.setUpdatedAt(new Date());
            invoice.setPaidAmount(paidAmount + amount);
            invoice.setUpdatedBy(authentication.getName());
            invoicesV1Repository.save(invoice);
            return 1;
        }
        return 0;
    }

    public ResponseEntity<?> getAllReceiptsByHostelId(String hostelId) {
        List<Receipts> listReceipts = transactionService.getAllReceiptsByHostelIdOld(hostelId);
        List<ReceiptsList> receipts = listReceipts.stream().map(item -> new ReceiptMapper().apply(item)).toList();
        return new ResponseEntity<>(receipts, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllReceiptsByHostelIdNew(String hostelId) {
        List<TransactionV1> listReceipts = transactionService.getAllReceiptsByHostelId(hostelId);
        List<String> customerIds = listReceipts.stream().map(TransactionV1::getCustomerId).toList();
        List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);
        List<String> invoiceIds = listReceipts.stream().map(TransactionV1::getInvoiceId).toList();
        List<InvoicesV1> invoices = invoicesV1Repository.findByInvoiceIdIn(invoiceIds);
        Set<String> bankIds = listReceipts.stream().map(TransactionV1::getBankId).collect(Collectors.toSet());
        List<BankingV1> listBanks = bankingService.findAllBanksById(bankIds);


        List<ReceiptsList> receipts = listReceipts.stream().map(item -> new TransactionsListMapper(listCustomers, listBanks, invoices).apply(item)).toList();
        return new ResponseEntity<>(receipts, HttpStatus.OK);
    }

    /**
     * use only for getting booking amount
     *
     * @param customerId
     * @param hostelId
     * @return
     */
    public Double getBookingAmount(String customerId, String hostelId) {
        InvoicesV1 invoiceV1 = invoicesV1Repository.findByCustomerIdAndHostelIdAndInvoiceType(customerId, hostelId, InvoiceType.BOOKING.name());
        return invoiceV1.getPaidAmount();
    }

    public InvoicesV1 getBookingInvoice(String customerId, String hostelId) {
        return invoicesV1Repository.findByCustomerIdAndHostelIdAndInvoiceType(customerId, hostelId, InvoiceType.BOOKING.name());
    }

    /**
     * this is used only for cancel the booking
     *
     * @param customerId
     * @return
     */
    public InvoicesV1 getInvoiceDetails(String customerId, String hostelId) {
        return invoicesV1Repository.findByCustomerIdAndHostelIdAndInvoiceType(customerId, hostelId, InvoiceType.BOOKING.name());
    }

    public InvoicesV1 getAdvanceInvoiceDetails(String customerId, String hostelId) {
        return invoicesV1Repository.findByCustomerIdAndHostelIdAndInvoiceType(customerId, hostelId, InvoiceType.ADVANCE.name());
    }

    public void cancelBookingInvoice(InvoicesV1 invoicesV1) {
        invoicesV1.setPaymentStatus(PaymentStatus.CANCELLED.name());
        invoicesV1.setCancelled(true);
        invoicesV1Repository.save(invoicesV1);
    }

    public ResponseEntity<?> generateManualInvoice(String customerId, ManualInvoice manualInvoice) {
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

        Customers customers = customersService.getCustomerInformation(customerId);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_CREATE_INVOICE_CHECKOUT_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_CREATE_INVOICE_SETTLEMET_CREATED_CUSTOMERS, HttpStatus.BAD_REQUEST);
        }
        HostelV1 hostelV1 = hostelService.getHostelInfo(customers.getHostelId());
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (!Utils.checkNullOrEmpty(manualInvoice.invoiceDate())) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_DATE, HttpStatus.BAD_REQUEST);
        }

        List<ItemResponse> items = manualInvoice.items() != null ? manualInvoice.items() : Collections.emptyList();
//        Optional<ItemResponse> ebItem = items.stream()
//                .filter(item -> com.smartstay.smartstay.ennum.InvoiceItems.EB.name().equalsIgnoreCase(item.invoiceItem()))
//                .findFirst();
//
//        Double ebAmount123 = ebItem.map(ItemResponse::amount).orElse(0.0);

        Optional<ItemResponse> rentItem = items.stream().filter(item -> com.smartstay.smartstay.ennum.InvoiceItems.RENT.name().equalsIgnoreCase(item.invoiceItem()) || "ROOM RENT".equalsIgnoreCase(item.invoiceItem())).findFirst();

        Double rentAmount = rentItem.map(ItemResponse::amount).orElse(0.0);

        String invoiceNumber = null;
        StringBuilder prefixSuffix = new StringBuilder();
        if (Utils.checkNullOrEmpty(manualInvoice.invoiceNumber())) {
            if (invoicesV1Repository.findByInvoiceNumberAndHostelId(manualInvoice.invoiceNumber(), customers.getHostelId()) != null) {
                return new ResponseEntity<>(Utils.INVOICE_NUMBER_ALREADY_REGISTERED, HttpStatus.BAD_REQUEST);
            } else {
                invoiceNumber = manualInvoice.invoiceNumber();
                prefixSuffix.append(invoiceNumber);
            }
        } else {
            String prefix = "INV";
            com.smartstay.smartstay.dao.BillTemplates templates = templatesService.getTemplateByHostelId(customers.getHostelId());
            if (templates != null && templates.getTemplateTypes() != null) {
                if (!templates.getTemplateTypes().isEmpty()) {
                    BillTemplateType rentTemplateType = templates.getTemplateTypes().stream().filter(i -> i.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name())).findFirst().get();
                    prefix = rentTemplateType.getInvoicePrefix();
                }
                prefixSuffix.append(prefix);
            }
            InvoicesV1 inv = invoicesV1Repository.findLatestInvoiceByPrefix(prefix, hostelV1.getHostelId());
            if (inv != null) {
                String[] prefArr = inv.getInvoiceNumber().split("-");
                if (prefArr.length > 1) {
                    int suffix = Integer.parseInt(prefArr[1]) + 1;
                    prefixSuffix.append("-");
                    if (suffix < 10) {
                        prefixSuffix.append("00");
                        prefixSuffix.append(suffix);
                    } else if (suffix < 100) {
                        prefixSuffix.append("0");
                        prefixSuffix.append(suffix);
                    } else {
                        prefixSuffix.append(suffix);
                    }
                }
            } else {
                prefixSuffix.append("-");
                //this is going to be the first invoice
                prefixSuffix.append("001");
            }

        }


//        int day = 1;
//        if (hostelV1.getElectricityConfig() != null) {
//            day = hostelV1.getElectricityConfig().getBillDate();
//        }

        Date invoiceDate = Utils.stringToDate(manualInvoice.invoiceDate(), Utils.USER_INPUT_DATE_FORMAT);

//        Date dateStartDate = null;
//        Date dateEndDate = null;
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(invoiceDate);
//        cal.set(Calendar.DAY_OF_MONTH, day);
        BillingDates currentBillingDate = hostelService.getBillingRuleOnDate(hostelV1.getHostelId(), new Date());
        BillingDates invoiceBillingDate = hostelService.getBillingRuleOnDate(hostelV1.getHostelId(), invoiceDate);

        boolean isCurrentCycle = Utils.compareWithTwoDates(invoiceDate, currentBillingDate.currentBillStartDate()) >= 0;

//        dateStartDate = cal.getTime();
//        Date dateEndDate = Utils.findLastDate(day, cal.getTime());

        if (Utils.compareWithTwoDates(invoiceDate, new Date()) > 0) {
            return new ResponseEntity<>(Utils.FUTURE_DATES_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
        }

        BookingsV1 filteredBooking = bookingsService.getBookingByCustomerIdAndDate(customerId, invoiceDate, invoiceBillingDate.currentBillEndDate());
        if (filteredBooking == null) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_DATE, HttpStatus.BAD_REQUEST);
        }

        List<InvoicesV1> previousInvoices = invoicesV1Repository.findInvoiceByCustomerIdAndDate(customerId, invoiceBillingDate.currentBillStartDate(), invoiceBillingDate.currentBillEndDate());
        if (previousInvoices != null) {
            if (!previousInvoices.isEmpty()) {
                return new ResponseEntity<>(Utils.INVOICE_ALREADY_PRESENT, HttpStatus.BAD_REQUEST);
            }
        }

        Date invoiceStartDate = null;
        Date invoiceEndDate = null;
        Date invoiceDueDate = null;

        if (Utils.compareWithTwoDates(filteredBooking.getJoiningDate(), invoiceBillingDate.currentBillStartDate()) <= 0) {
            invoiceStartDate = invoiceBillingDate.currentBillStartDate();
            invoiceDueDate = Utils.addDaysToDate(invoiceStartDate, invoiceBillingDate.dueDays());
        } else {
            invoiceStartDate = filteredBooking.getJoiningDate();
            invoiceDueDate = Utils.addDaysToDate(filteredBooking.getJoiningDate(), invoiceBillingDate.dueDays());
        }

        if (filteredBooking.getLeavingDate() == null) {
            invoiceEndDate = invoiceBillingDate.currentBillEndDate();
        } else if (Utils.compareWithTwoDates(filteredBooking.getLeavingDate(), invoiceBillingDate.currentBillEndDate()) >= 0) {
            invoiceEndDate = invoiceBillingDate.currentBillEndDate();
        } else if (Utils.compareWithTwoDates(filteredBooking.getLeavingDate(), invoiceBillingDate.currentBillEndDate()) >= 0) {
            invoiceEndDate = filteredBooking.getLeavingDate();
        } else {
            invoiceEndDate = invoiceBillingDate.currentBillEndDate();
        }

        long noOfDaysOnThatMonth = Utils.findNumberOfDays(invoiceBillingDate.currentBillStartDate(), invoiceBillingDate.currentBillEndDate());
        long noOfDaysStayed = Utils.findNumberOfDays(invoiceStartDate, invoiceEndDate);
        double invoiceAmount = 0.0;

        if (noOfDaysOnThatMonth == noOfDaysStayed) {
            invoiceAmount = rentAmount;
        } else {
            double rentPerDay = rentAmount / noOfDaysOnThatMonth;
            invoiceAmount = rentPerDay * noOfDaysStayed;
        }

        double totalAmount = 0.0;
        InvoicesV1 invoicesV1 = new InvoicesV1();
        List<InvoiceItems> listInvoicesItems = new ArrayList<>();
        for (ItemResponse item : manualInvoice.items()) {
            InvoiceItems invoiceItem = new InvoiceItems();
            String itemName = item.invoiceItem().trim().toUpperCase();
            if (itemName.equalsIgnoreCase("EB")) {
                invoiceItem.setAmount(item.amount());
                invoiceItem.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.EB.name());
            } else if (itemName.equalsIgnoreCase("RENT") || itemName.equalsIgnoreCase("ROOM RENT")) {
                invoiceItem.setAmount(invoiceAmount);
                invoiceItem.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
            } else if (itemName.equalsIgnoreCase("AMENITY")) {
                invoiceItem.setAmount(item.amount());
                invoiceItem.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name());
            } else {
                invoiceItem.setAmount(item.amount());
                invoiceItem.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
                invoiceItem.setOtherItem(item.invoiceItem());
            }
            if (item.amount() != null) {
                totalAmount = totalAmount + item.amount();
            }
            invoiceItem.setInvoice(invoicesV1);
            listInvoicesItems.add(invoiceItem);
        }

        invoicesV1.setInvoiceNumber(prefixSuffix.toString());
        invoicesV1.setBasePrice(Utils.roundOfDouble(totalAmount));
        invoicesV1.setCustomerId(customerId);
        invoicesV1.setHostelId(customers.getHostelId());
        invoicesV1.setInvoiceType(InvoiceType.RENT.name());
        invoicesV1.setCustomerMailId(customers.getEmailId());
        invoicesV1.setCustomerMobile(customers.getMobile());
//        invoicesV1.setEbAmount(ebAmount);
        invoicesV1.setTotalAmount(Utils.roundOfDouble(totalAmount));
        invoicesV1.setGst(0.0);
        invoicesV1.setCgst(0.0);
        invoicesV1.setSgst(0.0);
        invoicesV1.setGstPercentile(0.0);

        if (isCurrentCycle) {
            invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
            invoicesV1.setPaidAmount(0.0);
        } else {
            invoicesV1.setPaymentStatus(PaymentStatus.PAID.name());
            invoicesV1.setPaidAmount(Utils.roundOfDouble(totalAmount));
        }
        invoicesV1.setOthersDescription("");
        invoicesV1.setInvoiceMode(InvoiceMode.MANUAL.name());
        invoicesV1.setCreatedBy(authentication.getName());
        invoicesV1.setInvoiceGeneratedDate(new Date());
        invoicesV1.setInvoiceStartDate(Utils.convertToTimeStamp(invoiceStartDate));
        invoicesV1.setInvoiceDueDate(Utils.convertToTimeStamp(invoiceDueDate));
        invoicesV1.setInvoiceEndDate(Utils.convertToTimeStamp(invoiceEndDate));
        invoicesV1.setCancelled(false);

        invoicesV1.setInvoiceItems(listInvoicesItems);

        InvoicesV1 invoicesV11 = invoicesV1Repository.save(invoicesV1);
        usersService.addUserLog(hostelV1.getHostelId(), invoicesV11.getInvoiceId(), ActivitySource.INVOICE, ActivitySourceType.MANUAL_BILL, users);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);


    }

    public List<InvoicesV1> listAllUnpaidInvoices(String customerId, String hostelId) {
        return invoicesV1Repository.findByHostelIdAndCustomerIdAndPaymentStatusNotIgnoreCaseAndIsCancelledFalse(hostelId, customerId, PaymentStatus.PAID.name()).stream().filter(i -> !i.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())).toList();
    }

    List<InvoicesV1> listAllOldUnpaidInvoices(String customerId, String hostelId) {
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
        return invoicesV1Repository.findOldRentalPendingInvoicesExcludeCurrentMonth(customerId, hostelId, billingDates.currentBillStartDate()).stream().filter(i -> !i.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())).toList();
    }

    public InvoicesV1 getCurrentMonthInvoice(String customerId) {
        return invoicesV1Repository.findLatestInvoiceByCustomerId(customerId);
    }

    public ResponseEntity<?> getInvoiceDetailsByInvoiceId(String hostelId, String invoiceId) {
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

        InvoicesV1 invoicesV1 = invoicesV1Repository.findById(invoiceId).orElse(null);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }


        String paymentStatus = null;
        if (invoicesV1.getPaymentStatus() != null) {
            paymentStatus = InvoiceUtils.getInvoicePaymentStatusByStatus(invoicesV1.getPaymentStatus());
        }

        if (invoicesV1.isCancelled()) {
            paymentStatus = "Cancelled";
        }

        StringBuilder invoiceMonth = new StringBuilder();
        StringBuilder invoiceRentalPeriod = new StringBuilder();

        String hostelPhone = null;
        String hostelEmail = null;
        String invoiceType = "Rent";
        StringBuilder hostelFullAddress = new StringBuilder();
        String invoiceSignatureUrl = null;
        String hostelLogo = null;

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Advance";
        } else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            invoiceType = "Booking";
        }

        if (hostelV1.getHouseNo() != null && !hostelV1.getHouseNo().trim().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getHouseNo());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getStreet() != null && !hostelV1.getStreet().trim().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getStreet());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getCity() != null && !hostelV1.getCity().trim().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getCity());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getState() != null && !hostelV1.getState().trim().equalsIgnoreCase("")) {
            hostelFullAddress.append(hostelV1.getState());
            hostelFullAddress.append("-");
        }
        if (hostelV1.getPincode() != 0) {
            hostelFullAddress.append(hostelV1.getPincode());
        }

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name()) || invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.REASSIGN_RENT.name())) {

            invoiceRentalPeriod.append(Utils.dateToDateMonth(invoicesV1.getInvoiceStartDate()));
            invoiceRentalPeriod.append("-");
            invoiceRentalPeriod.append(Utils.dateToDateMonth(invoicesV1.getInvoiceEndDate()));
            BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelId, invoicesV1.getInvoiceStartDate());
            if (billingDates != null) {
                invoiceMonth.append(Utils.dateToMonth(billingDates.currentBillStartDate()));
            }
        }

        AccountDetails accountDetails = null;
        ConfigInfo signatureInfo = null;
        com.smartstay.smartstay.dao.BillTemplates hostelTemplates = templatesService.getTemplateByHostelId(hostelId);
        if (hostelTemplates != null) {
            if (!hostelTemplates.isMobileCustomized()) {
                hostelPhone = hostelTemplates.getMobile();
            } else {
                if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                    hostelPhone = hostelTemplates.getTemplateTypes().stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.ADVANCE.name())).map(BillTemplateType::getInvoicePhoneNumber).toList().getFirst();
                } else {
                    hostelPhone = hostelTemplates.getTemplateTypes().stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name())).map(BillTemplateType::getInvoicePhoneNumber).toList().getFirst();
                }

            }

            if (!hostelTemplates.isEmailCustomized()) {
                hostelEmail = hostelTemplates.getEmailId();
            } else {
                if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                    hostelEmail = hostelTemplates.getTemplateTypes().stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.ADVANCE.name())).map(BillTemplateType::getInvoiceMailId).toList().getFirst();
                } else {
                    hostelEmail = hostelTemplates.getTemplateTypes().stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name())).map(BillTemplateType::getInvoiceMailId).toList().getFirst();
                }
            }

            BillTemplateType templateType = null;

            if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                templateType = hostelTemplates.getTemplateTypes().stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.ADVANCE.name())).toList().getFirst();
            } else {
                templateType = hostelTemplates.getTemplateTypes().stream().filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name())).toList().getFirst();
            }

            if (!hostelTemplates.isSignatureCustomized()) {
                invoiceSignatureUrl = hostelTemplates.getDigitalSignature();
            } else {
                invoiceSignatureUrl = templateType.getInvoiceSignatureUrl();
            }
            if (!hostelTemplates.isLogoCustomized()) {
                hostelLogo = hostelTemplates.getHostelLogo();
            } else {
                hostelLogo = templateType.getInvoiceLogoUrl();
            }

            if (templateType.getBankAccountId() != null) {
                BankingV1 bankingV1 = bankingService.getBankDetails(templateType.getBankAccountId());
                accountDetails = new AccountDetails(bankingV1.getAccountNumber(), bankingV1.getIfscCode(), bankingV1.getBankName(), bankingV1.getUpiId(), templateType.getQrCode());
            } else {
                accountDetails = new AccountDetails(null, null, null, null, templateType.getQrCode());
            }


            signatureInfo = new ConfigInfo(templateType.getInvoiceTermsAndCondition(), invoiceSignatureUrl, hostelLogo, hostelFullAddress.toString(), templateType.getInvoiceTemplateColor(), templateType.getInvoiceNotes(), invoiceType);
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

            customerInfo = new CustomerInfo(customers.getFirstName(), customers.getLastName(), fullName.toString(), customers.getCustomerId(), customers.getMobile(), "91", fullAddress.toString(), Utils.dateToString(customers.getJoiningDate()));
        }

        StayInfo stayInfo = null;
        CustomersBedHistory bedHistory = customersBedHistoryService.getCustomerBedByStartDate(customers.getCustomerId(), invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());

        if (bedHistory != null) {
            BedDetails bedDetails = bedService.getBedDetails(bedHistory.getBedId());
            if (bedDetails != null) {
                stayInfo = new StayInfo(bedDetails.getBedName(), bedDetails.getFloorName(), bedDetails.getRoomName(), hostelV1.getHostelName());
            }
        }

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            double paidAmount = transactionService.findPaidAmountForInvoice(invoiceId);

            double balanceAmount = invoicesV1.getTotalAmount() - paidAmount;
            if (invoicesV1.getTotalAmount() < 0) {
                balanceAmount = invoicesV1.getTotalAmount() + paidAmount;
            }
            List<String> invoicesList = invoicesV1.getCancelledInvoices();
            List<com.smartstay.smartstay.responses.invoices.InvoiceItems> listInvoiceItems = new ArrayList<>();
            listInvoiceItems.add(new com.smartstay.smartstay.responses.invoices.InvoiceItems(invoicesV1.getInvoiceNumber(), InvoiceType.SETTLEMENT.name(), invoicesV1.getBasePrice()));
            List<Deductions> listDeductions = invoicesV1.getInvoiceItems().stream().map(i -> {
                Deductions d = new Deductions();
                if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name())) {
                    if (i.getOtherItem() != null) {
                        i.setInvoiceItem(i.getOtherItem());
                    }
                } else {
                    i.setInvoiceItem(i.getInvoiceItem());
                }
                d.setType(i.getInvoiceItem());
                d.setAmount(i.getAmount());

                return d;
            }).toList();

            double totalDeductionAmount = invoicesV1.getInvoiceItems().stream().mapToDouble(InvoiceItems::getAmount).sum();


            InvoiceInfo invoiceInfo = new InvoiceInfo(invoicesV1.getBasePrice(), 0.0, 0.0, invoicesV1.getTotalAmount(), paidAmount, balanceAmount, invoiceRentalPeriod.toString(), invoiceMonth.toString(), paymentStatus, invoicesV1.isCancelled(), totalDeductionAmount, listInvoiceItems, listDeductions);
            List<InvoiceSummary> invoiceSummaries = invoicesV1Repository.findInvoiceSummariesByHostelId(hostelId, invoicesList);
            List<InvoiceRefundHistory> listRefunds = transactionService.getRefundHistory(hostelId, invoiceId);
            FinalSettlementResponse finalSettlementResponse = new FinalSettlementResponse(invoicesV1.getInvoiceNumber(), invoicesV1.getInvoiceId(), Utils.dateToString(invoicesV1.getInvoiceStartDate()), Utils.dateToString(invoicesV1.getInvoiceDueDate()), hostelEmail, hostelPhone, "91", InvoiceType.SETTLEMENT.name(), customers.getHostelId(), customerInfo, stayInfo, accountDetails, signatureInfo, invoiceSummaries, invoiceInfo, listRefunds);
            return new ResponseEntity<>(finalSettlementResponse, HttpStatus.OK);

        }

        Double subTotal = 0.0;
        Double paidAmount = 0.0;
        Double balanceAmount = 0.0;

//        paidAmount = transactionService.findPaidAmountForInvoice(invoiceId);
        if (invoicesV1.getPaidAmount() != null) {
            paidAmount = invoicesV1.getPaidAmount();
        }
        balanceAmount = invoicesV1.getTotalAmount() - paidAmount;
        subTotal = invoicesV1.getTotalAmount();
        List<com.smartstay.smartstay.responses.invoices.InvoiceItems> listInvoiceItems = new ArrayList<>();

        for (InvoiceItems item : invoicesV1.getInvoiceItems()) {
            String description;
            switch (item.getInvoiceItem()) {
                case "RENT" -> description = "Rent";
                case "ADVANCE" -> description = "Advance";
                case "EB" -> description = "Electricity Bill";
                case "AMENITY" -> description = "Amenity";
                case "OTHERS" -> description = item.getOtherItem() != null ? item.getOtherItem() : "Others";
                default -> description = Utils.capitalize(item.getInvoiceItem());
            }
            com.smartstay.smartstay.responses.invoices.InvoiceItems responseItem = new com.smartstay.smartstay.responses.invoices.InvoiceItems(invoicesV1.getInvoiceNumber(), description, item.getAmount());

            listInvoiceItems.add(responseItem);
        }
        List<PaymentHistoryProjection> paymentHistoryList = transactionService.getPaymentHistoryByInvoiceId(invoiceId);

        InvoiceInfo invoiceInfo = new InvoiceInfo(subTotal, 0.0, 0.0, invoicesV1.getTotalAmount(), paidAmount, balanceAmount, invoiceRentalPeriod.toString(), invoiceMonth.toString(), paymentStatus, invoicesV1.isCancelled(), 0.0, listInvoiceItems, null);

        InvoiceDetails details = new InvoiceDetails(invoicesV1.getInvoiceNumber(), invoicesV1.getInvoiceId(), Utils.dateToString(invoicesV1.getInvoiceStartDate()), Utils.dateToString(invoicesV1.getInvoiceDueDate()), hostelEmail, hostelPhone, "91", customers.getHostelId(), customerInfo, stayInfo, invoiceInfo, accountDetails, paymentHistoryList, signatureInfo);
        return new ResponseEntity<>(details, HttpStatus.OK);

    }

    public List<InvoiceResponse> getInvoiceResponseList(String customerId) {
        List<InvoicesV1> invoices = invoicesV1Repository.findByCustomerId(customerId);

        return invoices.stream().map(invoice -> InvoiceMapper.toResponse(invoice, invoicesV1Repository)).toList();

    }

    public void cancelActiveInvoice(List<InvoicesV1> unpaidUpdated) {
        invoicesV1Repository.saveAll(unpaidUpdated);
    }

    public InvoicesV1 createSettlementInvoice(Customers customers, String hostelId, double totalAmountToBePaid, List<InvoicesV1> unpaidInvoices, List<Deductions> listDeductions, Double totalAmountWithoutDeduction, Date leavingDate, Users users) {
        List<InvoicesV1> invoicesV1 = invoicesV1Repository.findByCustomerIdAndInvoiceType(customers.getCustomerId(), InvoiceType.SETTLEMENT.name());
        if (!invoicesV1.isEmpty()) {
            InvoicesV1 settlementInvoice = invoicesV1.get(0);

            List<String> listUnpaidInvoicesId = new ArrayList<>(unpaidInvoices.stream().map(InvoicesV1::getInvoiceId).toList());

            settlementInvoice.setCancelledInvoices(listUnpaidInvoicesId);
            settlementInvoice.setBasePrice(Utils.roundOfDouble(totalAmountWithoutDeduction));
            settlementInvoice.setTotalAmount(totalAmountToBePaid);
            settlementInvoice.setInvoiceStartDate(leavingDate);
            settlementInvoice.setInvoiceDueDate(leavingDate);
            settlementInvoice.setInvoiceEndDate(leavingDate);
            settlementInvoice.setUpdatedAt(new Date());

            List<InvoiceItems> listInvoiceItems = listDeductions.stream().map(i -> {
                InvoiceItems invoiceItems = new InvoiceItems();
                invoiceItems.setAmount(i.getAmount());
                if (i.getType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.MAINTENANCE.name())) {
                    invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.MAINTENANCE.name());
                } else if (i.getType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name())) {
                    invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.EB.name());
                } else if (i.getType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name())) {
                    invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name());
                } else if (i.getType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name())) {
                    invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
                } else {
                    invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
                    invoiceItems.setOtherItem(i.getType());

                }
                invoiceItems.setInvoice(settlementInvoice);
                return invoiceItems;
            }).toList();
            if (!listInvoiceItems.isEmpty()) {
                settlementInvoice.setInvoiceItems(listInvoiceItems);
            }

            invoicesV1Repository.save(settlementInvoice);

            usersService.finalSettlementGenetated(hostelId, settlementInvoice.getInvoiceId(), ActivitySource.SETTLEMENT, ActivitySourceType.UPDATE, customers.getCustomerId(), users);
            return settlementInvoice;
        } else {
            List<String> listUnpaidInvoicesId = unpaidInvoices.stream().map(InvoicesV1::getInvoiceId).toList();
            InvoicesV1 settlementInvoice = new InvoicesV1();
            settlementInvoice.setCancelledInvoices(listUnpaidInvoicesId);
            settlementInvoice.setCustomerId(customers.getCustomerId());
            settlementInvoice.setHostelId(hostelId);
            settlementInvoice.setInvoiceNumber(generateInvoiceNumber(hostelId, "RENT"));
            settlementInvoice.setCustomerMobile(customers.getMobile());
            settlementInvoice.setInvoiceType(InvoiceType.SETTLEMENT.name());
            settlementInvoice.setBasePrice(Utils.roundOfDouble(totalAmountWithoutDeduction));
            settlementInvoice.setTotalAmount(totalAmountToBePaid);
            settlementInvoice.setGst(0.0);
            settlementInvoice.setCgst(0.0);
            settlementInvoice.setSgst(0.0);
            settlementInvoice.setGst(0.0);
            if (totalAmountToBePaid > 0) {
                settlementInvoice.setPaymentStatus(PaymentStatus.PENDING.name());
            } else if (totalAmountToBePaid == 0) {
                settlementInvoice.setPaymentStatus(PaymentStatus.REFUNDED.name());
            } else {
                settlementInvoice.setPaymentStatus(PaymentStatus.PENDING_REFUND.name());
            }

            settlementInvoice.setOthersDescription(null);
            settlementInvoice.setInvoiceMode(InvoiceMode.MANUAL.name());
            settlementInvoice.setCancelled(false);
            settlementInvoice.setCreatedBy(authentication.getName());
            settlementInvoice.setUpdatedBy(authentication.getName());
            settlementInvoice.setInvoiceGeneratedDate(new Date());
            settlementInvoice.setInvoiceStartDate(leavingDate);
            settlementInvoice.setInvoiceDueDate(leavingDate);
            settlementInvoice.setInvoiceEndDate(leavingDate);
            settlementInvoice.setCreatedAt(new Date());
            settlementInvoice.setUpdatedAt(new Date());

            List<InvoiceItems> listInvoiceItems = listDeductions.stream().map(i -> {
                InvoiceItems invoiceItems = new InvoiceItems();
                invoiceItems.setAmount(i.getAmount());
                if (i.getType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.MAINTENANCE.name())) {
                    invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.MAINTENANCE.name());
                } else if (i.getType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name())) {
                    invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.EB.name());
                } else if (i.getType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name())) {
                    invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name());
                } else if (i.getType().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name())) {
                    invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
                } else {
                    invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name());
                    invoiceItems.setOtherItem(i.getType());
                }
                invoiceItems.setInvoice(settlementInvoice);
                return invoiceItems;
            }).toList();

            settlementInvoice.setInvoiceItems(listInvoiceItems);

            InvoicesV1 invoicesV11 = invoicesV1Repository.save(settlementInvoice);
            usersService.finalSettlementGenetated(hostelId, invoicesV11.getInvoiceId(), ActivitySource.SETTLEMENT, ActivitySourceType.CREATE, customers.getCustomerId(), users);
            return invoicesV11;
        }
    }

    public String generateInvoiceNumber(String hostelId, String type) {
        StringBuilder invoiceNumber = new StringBuilder();
        BillTemplates templates = templateService.getBillTemplate(hostelId, type);
        InvoicesV1 existingV1 = null;

        if (templates != null) {
            existingV1 = invoicesV1Repository.findLatestInvoiceByPrefix(templates.prefix(), hostelId);
            invoiceNumber = new StringBuilder();
            invoiceNumber.append(templates.prefix());

            if (existingV1 == null) {
                invoiceNumber.append("-001");
            } else {
                String[] suffix = existingV1.getInvoiceNumber().split("-");
                if (suffix.length > 1) {
                    invoiceNumber.append("-");
                    int suff = Integer.parseInt(suffix[suffix.length - 1]) + 1;
                    invoiceNumber.append(String.format("%03d", suff));
                }
            }

        } else {
            invoiceNumber.append("INV");
            invoiceNumber.append("-");
            invoiceNumber.append("001");
        }

        return invoiceNumber.toString();
    }

    public ReassignRent calculateAndCreateInvoiceForReassign(Customers customers, String joiningDate, Double newRent) {

        Date dateJoiningDate = Utils.stringToDate(joiningDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        BillingDates billingDates = hostelService.getBillingRuleOnDate(customers.getHostelId(), dateJoiningDate);
        if (billingDates != null) {
//            List<InvoicesV1> listInvoices = invoicesV1Repository.findInvoiceByCustomerIdAndDate(customers.getCustomerId(), billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
            InvoicesV1 latestInvoice = invoicesV1Repository.findCurrentRunningInvoice(customers.getCustomerId(), billingDates.currentBillStartDate());
            if (latestInvoice != null) {
                CustomersBedHistory latestHistory = customersBedHistoryService.getLatestCustomerBed(customers.getCustomerId());

                Date billStartDate = latestInvoice.getInvoiceStartDate();
                if (latestHistory != null) {
                    if (Utils.compareWithTwoDates(latestHistory.getStartDate(), billStartDate) > 0) {
                        billStartDate = latestHistory.getStartDate();
                    }
                }
                long noOfDaysInOldInvoice = Utils.findNumberOfDays(billStartDate, latestInvoice.getInvoiceEndDate());
                double rent = latestInvoice
                        .getInvoiceItems().stream().filter(item -> com.smartstay.smartstay.ennum.InvoiceItems.RENT
                                .name().equalsIgnoreCase(item.getInvoiceItem()))
                        .mapToDouble(InvoiceItems::getAmount).sum();

                double rentPerDay = rent / noOfDaysInOldInvoice;
                if (Utils.compareWithTwoDates(dateJoiningDate, latestInvoice.getInvoiceStartDate()) == 0) {
                    long noOfDaysInCurrentMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(),
                            billingDates.currentBillEndDate());
                    double rentPerDayNew = newRent / noOfDaysInCurrentMonth;
                    long noOfDaysStaying = Utils.findNumberOfDays(dateJoiningDate, billingDates.currentBillEndDate());
                    double rentForNewInvoice = Math.round(noOfDaysStaying * rentPerDayNew);

                    double currentPaid = latestInvoice.getPaidAmount() != null ? latestInvoice.getPaidAmount() : 0.0;
                    double newBalanceAmount = 0.0;

                    latestInvoice.getInvoiceItems().stream()
                            .filter(item -> com.smartstay.smartstay.ennum.InvoiceItems.RENT.name()
                                    .equalsIgnoreCase(item.getInvoiceItem()))
                            .findFirst()
                            .ifPresent(item -> {
                                item.setAmount(rentForNewInvoice);
                                invoiceItemService.updateInvoiceItems(item);
                            });

                    double totalAmountUpdate = (latestInvoice.getTotalAmount() - rent) + rentForNewInvoice;
                    latestInvoice.setTotalAmount(totalAmountUpdate);
                    latestInvoice.setBasePrice(totalAmountUpdate);
                    latestInvoice.setInvoiceType(InvoiceType.REASSIGN_RENT.name());

                    if (currentPaid >= totalAmountUpdate) {
                        latestInvoice.setPaymentStatus(PaymentStatus.PAID.name());
                        latestInvoice.setPaidAmount(totalAmountUpdate);
                        newBalanceAmount = currentPaid - totalAmountUpdate;
                    } else if (currentPaid > 0) {
                        latestInvoice.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
                        latestInvoice.setPaidAmount(currentPaid);
                    } else {
                        latestInvoice.setPaymentStatus(PaymentStatus.PENDING.name());
                        latestInvoice.setPaidAmount(0.0);
                    }

                    invoicesV1Repository.save(latestInvoice);
                    return new ReassignRent(latestInvoice.getInvoiceId(), latestInvoice.getInvoiceId(),
                            newBalanceAmount, latestInvoice.getInvoiceStartDate());

                } else {
                    Calendar lastDayCal = Calendar.getInstance();
                    lastDayCal.setTime(dateJoiningDate);
                    lastDayCal.set(Calendar.DAY_OF_MONTH, lastDayCal.get(Calendar.DAY_OF_MONTH) - 1);
                    latestInvoice.setInvoiceEndDate(lastDayCal.getTime());

                    long noOfDaysStayed = Utils.findNumberOfDays(latestInvoice.getInvoiceStartDate(),
                            lastDayCal.getTime());

                    double balanceAmount = 0.0;
                    double rentForOldInvoice = Math.round(noOfDaysStayed * rentPerDay);
                    double totalAmountForOldInvoice = (latestInvoice.getTotalAmount() - rent) + rentForOldInvoice;

                    if (latestInvoice.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                        balanceAmount = latestInvoice.getTotalAmount() - totalAmountForOldInvoice;
                        latestInvoice.setPaidAmount(totalAmountForOldInvoice);
                    } else if (latestInvoice.getPaymentStatus()
                            .equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
                        balanceAmount = latestInvoice.getPaidAmount() - totalAmountForOldInvoice;

                        if (latestInvoice.getPaidAmount() >= rentForOldInvoice) {
                            latestInvoice.setPaymentStatus(PaymentStatus.PAID.name());
                            latestInvoice.setPaidAmount(totalAmountForOldInvoice);
                        } else {
                            if (latestInvoice.getPaidAmount() > 0) {
                                double paidAmountForOldRent = latestInvoice.getPaidAmount() - rentForOldInvoice;
                                latestInvoice.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
                                latestInvoice.setPaidAmount(paidAmountForOldRent);
                            }
                        }

                    } else {
                        balanceAmount = 0;
                    }

                    latestInvoice.setTotalAmount(totalAmountForOldInvoice);

                    latestInvoice.getInvoiceItems().stream()
                            .filter(item -> com.smartstay.smartstay.ennum.InvoiceItems.RENT.name()
                                    .equalsIgnoreCase(item.getInvoiceItem()))
                            .findFirst().map(item -> {
                                item.setAmount(rentForOldInvoice);
                                return item;
                            }).ifPresent(modifiedRentItems -> invoiceItemService.updateInvoiceItems(modifiedRentItems));

                    invoicesV1Repository.save(latestInvoice);

                    return createNewInvoice(latestInvoice, joiningDate, newRent, billingDates, balanceAmount);
                }

            }
        }

        return null;
    }

    public ReassignRent createNewInvoice(InvoicesV1 oldInvoice, String joiningDate, Double rent, BillingDates billingDates, double balanceAmount) {
        double newBalanceAmount = 0.0;
        Date dateJoiningDate = Utils.stringToDate(joiningDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        long noOfDaysInCurrentMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());

        double rentPerDay = rent / noOfDaysInCurrentMonth;
        long noOfDaysStaying = Utils.findNumberOfDays(dateJoiningDate, billingDates.currentBillEndDate());

        double rentForNewInvoice = Math.round(noOfDaysStaying * rentPerDay);

        String invoiceNumber = null;
        BillTemplates templates = templateService.getBillTemplate(oldInvoice.getHostelId(), InvoiceType.RENT.name());
        if (templates != null) {
            InvoicesV1 inv = invoicesV1Repository.findLatestInvoiceByPrefix(templates.prefix(), oldInvoice.getHostelId());
            if (inv == null) {
                String invoice = templates.prefix() + "-" + templates.suffix();
                invoiceNumber = invoice;
            } else {
                invoiceNumber = Utils.formPrefixSuffix(inv.getInvoiceNumber());
            }
        }

        InvoicesV1 invoicesV1 = new InvoicesV1();
        invoicesV1.setBasePrice(rentForNewInvoice);
        invoicesV1.setTotalAmount(rentForNewInvoice);
        invoicesV1.setInvoiceStartDate(Utils.convertToTimeStamp(dateJoiningDate));
        invoicesV1.setInvoiceEndDate(Utils.convertToTimeStamp(billingDates.currentBillEndDate()));
        invoicesV1.setCreatedBy(authentication.getName());
        invoicesV1.setCreatedAt(new Date());
        invoicesV1.setInvoiceType(InvoiceType.REASSIGN_RENT.name());
        invoicesV1.setCustomerId(oldInvoice.getCustomerId());
        invoicesV1.setCustomerMobile(oldInvoice.getCustomerMobile());
        invoicesV1.setCustomerMailId(oldInvoice.getCustomerMailId());
        invoicesV1.setInvoiceNumber(invoiceNumber);

        if (balanceAmount == 0) {
            invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
            invoicesV1.setPaidAmount(0.0);
        } else {
            if (balanceAmount >= rentForNewInvoice) {
                invoicesV1.setPaymentStatus(PaymentStatus.PAID.name());
                invoicesV1.setPaidAmount(rentForNewInvoice);
                newBalanceAmount = balanceAmount - rentForNewInvoice;
            } else {
                invoicesV1.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
                invoicesV1.setPaidAmount(balanceAmount);
            }

        }

        invoicesV1.setCreatedBy(authentication.getName());
        invoicesV1.setInvoiceDueDate(Utils.convertToTimeStamp(Utils.addDaysToDate(dateJoiningDate, billingDates.dueDays())));
        invoicesV1.setCustomerMobile(invoicesV1.getCustomerMobile());
        invoicesV1.setCustomerMailId(invoicesV1.getCustomerMailId());
        invoicesV1.setGst(0.0);
        invoicesV1.setCgst(0.0);
        invoicesV1.setSgst(0.0);
        invoicesV1.setGstPercentile(0.0);
        invoicesV1.setCreatedAt(new Date());
        invoicesV1.setInvoiceGeneratedDate(new Date());
        invoicesV1.setCancelled(false);
        invoicesV1.setInvoiceMode(InvoiceMode.AUTOMATIC.name());
        invoicesV1.setHostelId(oldInvoice.getHostelId());

        InvoiceItems invoiceItems = new InvoiceItems();
        invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
        invoiceItems.setAmount(rentForNewInvoice);
        invoiceItems.setInvoice(invoicesV1);

        List<InvoiceItems> listInvoiceItems = new ArrayList<>();
        listInvoiceItems.add(invoiceItems);
        invoicesV1.setInvoiceItems(listInvoiceItems);

        InvoicesV1 invoicesV11 = invoicesV1Repository.save(invoicesV1);

        return new ReassignRent(oldInvoice.getInvoiceId(), invoicesV11.getInvoiceId(), newBalanceAmount, invoicesV11.getInvoiceStartDate());
    }

    public InvoicesV1 getFinalSettlementStatus(String customerId) {
        List<InvoicesV1> listFinalSettlement = invoicesV1Repository.findByCustomerIdAndInvoiceType(customerId, InvoiceType.SETTLEMENT.name());
        if (listFinalSettlement == null || listFinalSettlement.isEmpty()) {
            return null;
        }

        return listFinalSettlement.stream().findAny().get();
    }

    public boolean isFinalSettlementPaid(InvoicesV1 invoicesV1) {
        Double finalSettlementPaidAmount = transactionService.getFinalSettlementPaidAmount(invoicesV1.getInvoiceId());
        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name()) || invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name()) || invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.REFUNDED.name()) || invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_REFUND.name())) {

            if (invoicesV1.getTotalAmount() < 0) {
                double finalSettlementAmount = -1 * invoicesV1.getTotalAmount();
                return finalSettlementPaidAmount >= finalSettlementAmount;

            }
            return finalSettlementPaidAmount >= invoicesV1.getTotalAmount();
        }

        return false;
    }

    public Double getPayableAmount(InvoicesV1 invoicesV1) {
        Double finalSettlementPaidAmount = transactionService.getFinalSettlementPaidAmount(invoicesV1.getInvoiceId());

        return finalSettlementPaidAmount - invoicesV1.getTotalAmount();
    }

    public ResponseEntity<?> initializeRefund(String hostelId, String invoiceId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.BAD_REQUEST);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BOOKING, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        InvoicesV1 invoicesV1 = invoicesV1Repository.findById(invoiceId).orElse(null);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }
        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.REFUNDED.name())) {
            return new ResponseEntity<>(Utils.REFUND_COMPLETED, HttpStatus.BAD_REQUEST);
        }
        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_REFUND_CANCELLED_INVOICE, HttpStatus.BAD_REQUEST);
        }

        double refundableAmount = 0.0;
        double refundedAmount = 0.0;
        double pendingRefund = 0.0;
        String invoiceDate = null;
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.SETTLEMENT.name())) {
            if (invoicesV1.getTotalAmount() != null && invoicesV1.getTotalAmount() < 0) {
                refundableAmount = -(invoicesV1.getTotalAmount());

                if (invoicesV1.getPaidAmount() != null) {
                    pendingRefund = refundableAmount - invoicesV1.getPaidAmount();
                    refundedAmount = invoicesV1.getPaidAmount();
                } else {
                    pendingRefund = invoicesV1.getTotalAmount() - 0;
                    refundedAmount = 0;
                }
            } else {
                return new ResponseEntity<>(Utils.CANNOT_INITIATE_REFUND, HttpStatus.BAD_REQUEST);
            }
        } else {
            BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelId, new Date());
            if (billingDates != null && billingDates.currentBillStartDate() != null) {
                if (Utils.compareWithTwoDates(invoicesV1.getInvoiceStartDate(), billingDates.currentBillStartDate()) < 0) {
                    return new ResponseEntity<>(Utils.CANNOT_REFUND_FOR_OLD_INVOICES, HttpStatus.BAD_REQUEST);
                }
            }

            if (invoicesV1.getPaidAmount() != null && invoicesV1.getPaidAmount() > 0) {
                refundableAmount = invoicesV1.getPaidAmount();
            } else {
                return new ResponseEntity<>(Utils.CANNOT_REFUND_FOR_UNPAID_INVOICES, HttpStatus.BAD_REQUEST);
            }

        }

        CustomersBedHistory bedHistory = customersBedHistoryService.getLatestCustomerBed(invoicesV1.getCustomerId());
        String bedName = null;
        String floorName = null;
        String roomName = null;
        invoiceDate = Utils.dateToString(invoicesV1.getInvoiceStartDate());
        if (bedHistory != null) {
            com.smartstay.smartstay.dto.beds.BedDetails bedDetails = bedService.getBedDetails(bedHistory.getBedId());
            if (bedDetails != null) {
                bedName = bedDetails.getBedName();
                floorName = bedDetails.getFloorName();
                roomName = bedDetails.getRoomName();
            }
        }
        List<RefundableBanks> listBanks = bankingService.initializeRefund(invoicesV1.getHostelId());
        InitializeRefund refundInitializations = new InitializeRefund(roomName, floorName, bedName, refundableAmount, refundedAmount, pendingRefund, invoiceDate, listBanks);
        return new ResponseEntity<>(refundInitializations, HttpStatus.OK);
    }

    public void saveInvoice(InvoicesV1 invoicesV1) {
        invoicesV1Repository.save(invoicesV1);
    }

    public InvoicesV1 getCurrentMonthRentInvoice(String customerId) {
        return invoicesV1Repository.findLatestRentInvoiceByCustomerId(customerId);
    }

    public List<InvoicesV1> getAllCurrentMonthRentInvoices(String customerId) {
        return invoicesV1Repository.findAllRentInvoicesByCustomerId(customerId);
    }

    public void cancelAdvanceInvoice(InvoicesV1 invAdvanceInvoice) {
        if (!invAdvanceInvoice.isCancelled()) {
            invAdvanceInvoice.setCancelled(true);
        }
        invoicesV1Repository.save(invAdvanceInvoice);
    }

    public ResponseEntity<?> generateRecurringManually(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        applicationEventPublisher.publishEvent(new RecurringEvents(this, hostelId));

//        List<com.smartstay.smartstay.dao.BillingRules> listBillingRules = hostelService.findAllHostelsHavingBillingToday();

//        List<HostelV1> listHostels = listBillingRules
//                .stream()
//                .map(BillingRules::getHostel)
//                .toList();
//
//        if (listHostels != null && !listHostels.isEmpty()) {
//           listHostels.forEach(item -> {
//
//           });

//            return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
//        }
        return new ResponseEntity<>("Not today", HttpStatus.BAD_REQUEST);
    }

    public List<InvoiceCustomer> findDueCustomers(List<String> lisCustomerIds) {
        if (authentication.isAuthenticated()) {
            return invoicesV1Repository.findByCustomerIdAndBedIdsForDue(lisCustomerIds, new Date());
        }
        return null;
    }

    public Integer getPendingInvoiceCounts(String hostelId) {
        return invoicesV1Repository.findPendingInvoices(hostelId).size();
    }

    public void updateJoiningDateOnAdvanceInvoice(String customerId, Date joiningDate) {
        List<InvoicesV1> invoicesV1 = invoicesV1Repository.findByCustomerIdAndInvoiceType(customerId, InvoiceType.ADVANCE.name());
        if (invoicesV1 != null && !invoicesV1.isEmpty()) {
            BillingDates billingDates = hostelService.getBillingRuleOnDate(invoicesV1.get(0).getHostelId(), joiningDate);
            InvoicesV1 inv1 = invoicesV1.get(0);
            inv1.setInvoiceStartDate(joiningDate);
            Date dueDate = Utils.addDaysToDate(joiningDate, billingDates.dueDays());
            inv1.setInvoiceDueDate(dueDate);

            invoicesV1Repository.save(inv1);
        }
    }

    public void findOldInvoiceAndUpdate(String customerId, Date oldJoiningDate, Date newJoiningDate, String hostelId, Double rent) {
        BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelId, oldJoiningDate);

        List<InvoicesV1> oldInvoices = invoicesV1Repository.findInvoiceByCustomerIdAndDate(customerId, billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
        if (oldInvoices != null && !oldInvoices.isEmpty()) {
            InvoicesV1 invoicesV1 = oldInvoices.get(0);
            List<TransactionV1> transactions = transactionService.getTransactionsByInvoiceId(invoicesV1.getInvoiceId());
            if (Utils.compareWithTwoDates(newJoiningDate, billingDates.currentBillStartDate()) < 0) {
                double rentAmount = invoicesV1.getInvoiceItems().stream().filter(i -> i.getInvoiceItem().equalsIgnoreCase(InvoiceType.RENT.name())).mapToDouble(InvoiceItems::getAmount).sum();
                long noOfDaysStayed = Utils.findNumberOfDays(invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());
                if (noOfDaysStayed < 0) {
                    noOfDaysStayed = -1 * noOfDaysStayed;
                }
                double rentPerDay = rentAmount / noOfDaysStayed;

                long noDaysStayingBasedOnNewJoiningDate = Utils.findNumberOfDays(billingDates.currentBillStartDate(), invoicesV1.getInvoiceEndDate());
                if (noDaysStayingBasedOnNewJoiningDate < 0) {
                    noDaysStayingBasedOnNewJoiningDate = -1 * noDaysStayingBasedOnNewJoiningDate;
                }
                double newRent = Math.round(noDaysStayingBasedOnNewJoiningDate * rentPerDay);
                double oldBasePrice = invoicesV1.getBasePrice() - rentAmount;

                double paidAmount = 0.0;
                if (!transactions.isEmpty()) {
                    paidAmount = transactions.stream().mapToDouble(TransactionV1::getPaidAmount).sum();
                }

                invoicesV1.setInvoiceStartDate(newJoiningDate);
                invoicesV1.setBasePrice(oldBasePrice + newRent);
                invoicesV1.setTotalAmount(oldBasePrice + newRent);
                if (paidAmount > 0) {
                    if ((oldBasePrice + newRent) > paidAmount) {
                        invoicesV1.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
                    }
                }

//                invoicesV1.setInvoiceItems(invoiceItems);

                invoicesV1Repository.save(invoicesV1);

                List<InvoiceItems> invoiceItems = invoicesV1.getInvoiceItems().stream().map(i -> {
                    if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name())) {
                        i.setAmount(newRent);
                    }
                    return i;
                }).toList();

                invoiceItemService.updateInvoiceItems(invoiceItems);

//                String invoiceNumber = null;
//                BillTemplates templates = templateService.getBillTemplate(hostelId, InvoiceType.RENT.name());
//                if (templates != null) {
//                    InvoicesV1 inv = invoicesV1Repository.findLatestInvoiceByPrefix(templates.prefix(), hostelId);
//                    if (inv == null) {
//                        String invoice = templates.prefix() +
//                                "-" +
//                                templates.suffix();
//                        invoiceNumber = invoice;
//                    }
//                    else {
//                        invoiceNumber = Utils.formPrefixSuffix(inv.getInvoiceNumber());
//                    }
//                }


//                BillingDates newJoiningDateBillingDates = hostelService.getBillingRuleOnDate(hostelId, newJoiningDate);
//                long noOfDaysAsPerNewJoiningDate = Utils.findNumberOfDays(newJoiningDate, newJoiningDateBillingDates.currentBillEndDate());
//                double rentPerDayAsPerNewDate = rent / noOfDaysAsPerNewJoiningDate;
//                long noOfDaysStayedAsPerNewDate = Utils.findNumberOfDays(newJoiningDate, newJoiningDateBillingDates.currentBillEndDate());
//                double newRentAsPerNewDate = Math.round(noOfDaysStayedAsPerNewDate * rentPerDayAsPerNewDate);
//
//                Date invoiceStateDate = newJoiningDateBillingDates.currentBillStartDate();
//                if (Utils.compareWithTwoDates(newJoiningDateBillingDates.currentBillStartDate(), newJoiningDate) > 0) {
//                    invoiceStateDate = newJoiningDate;
//                }
//                Date dueDate = Utils.addDaysToDate(invoiceStateDate, newJoiningDateBillingDates.dueDays());
//
//                InvoicesV1 newJoiningDateInvoice = new InvoicesV1();
//                newJoiningDateInvoice.setCustomerId(customerId);
//                newJoiningDateInvoice.setHostelId(hostelId);
//                newJoiningDateInvoice.setInvoiceNumber(invoiceNumber);
//                newJoiningDateInvoice.setCustomerMailId(invoicesV1.getCustomerMailId());
//                newJoiningDateInvoice.setCustomerMobile(invoicesV1.getCustomerMobile());
//                newJoiningDateInvoice.setInvoiceType(InvoiceType.RENT.name());
//                newJoiningDateInvoice.setBasePrice(newRentAsPerNewDate);
//                newJoiningDateInvoice.setTotalAmount(newRentAsPerNewDate);
//                newJoiningDateInvoice.setPaidAmount(0.0);
//                newJoiningDateInvoice.setCgst(0.0);
//                newJoiningDateInvoice.setSgst(0.0);
//                newJoiningDateInvoice.setGstPercentile(0.0);
//                newJoiningDateInvoice.setPaymentStatus(PaymentStatus.PAID.name());
//                newJoiningDateInvoice.setInvoiceMode(InvoiceMode.MANUAL.name());
//                newJoiningDateInvoice.setCancelled(false);
//                newJoiningDateInvoice.setCreatedBy(authentication.getName());
//                newJoiningDateInvoice.setInvoiceGeneratedDate(new Date());
//                newJoiningDateInvoice.setInvoiceStartDate(invoiceStateDate);
//                newJoiningDateInvoice.setInvoiceDueDate(dueDate);
//                newJoiningDateInvoice.setInvoiceEndDate(newJoiningDateBillingDates.currentBillEndDate());
//                newJoiningDateInvoice.setCreatedAt(invoiceStateDate);
//
//                List<InvoiceItems> listInvoicesItems = new ArrayList<>();
//                InvoiceItems items = new InvoiceItems();
//                items.setAmount(newRentAsPerNewDate);
//                items.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
//                items.setInvoice(newJoiningDateInvoice);
//
//                newJoiningDateInvoice.setInvoiceItems(listInvoicesItems);
//
//                invoicesV1Repository.save(newJoiningDateInvoice);

            } else {

                double rentAmount = rentHistoryService.findRent(customerId, invoicesV1.getInvoiceStartDate());
                long noOfDaysInTheMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());

                double rentPerDay = rentAmount / noOfDaysInTheMonth;

                long noDaysStayingBasedOnNewJoiningDate = Utils.findNumberOfDays(newJoiningDate, invoicesV1.getInvoiceEndDate());
                if (noDaysStayingBasedOnNewJoiningDate < 0) {
                    noDaysStayingBasedOnNewJoiningDate = -1 * noDaysStayingBasedOnNewJoiningDate;
                }

                double newRent = Math.round(noDaysStayingBasedOnNewJoiningDate * rentPerDay);
                Date dueDate = Utils.addDaysToDate(newJoiningDate, billingDates.dueDays());

                Double totalAmount = invoicesV1.getTotalAmount();
                double oldRent = invoicesV1.getInvoiceItems().stream().filter(i -> i.getInvoiceItem().equalsIgnoreCase(InvoiceType.RENT.name())).mapToDouble(InvoiceItems::getAmount).sum();
                double totalWithoutOldRent = totalAmount - oldRent;
                double totalWithNewRent = totalWithoutOldRent + newRent;

                invoicesV1.setInvoiceStartDate(newJoiningDate);
                invoicesV1.setInvoiceDueDate(dueDate);
                invoicesV1.setBasePrice(newRent);
                invoicesV1.setTotalAmount(totalWithNewRent);


//                invoicesV1.setInvoiceItems(invoiceItems);

                invoicesV1Repository.save(invoicesV1);

                List<InvoiceItems> invoiceItems = invoicesV1.getInvoiceItems().stream().filter(i -> i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name())).map(i -> {
                    i.setAmount(newRent);
                    return i;
                }).toList();
                if (!invoiceItems.isEmpty()) {
                    invoiceItemService.updateInvoiceItems(invoiceItems.get(0));
                }
            }
        }
    }

    public boolean updateJoiningDate(Customers customers, Date joinigDate, String hostelId, Date oldJoiningDate, Double rent) {
        BillingDates billingDatesForOldJoiningDates = hostelService.getBillingRuleOnDate(hostelId, oldJoiningDate);
        BillingDates currentMonthBillingDates = hostelService.getBillingRuleOnDate(hostelId, new Date());

        //joined in current month
        if (Utils.compareWithTwoDates(currentMonthBillingDates.currentBillStartDate(), oldJoiningDate) <= 0) {
            List<InvoicesV1> listALlCurrentMonthInvoices = invoicesV1Repository.findAllCurrentMonthInvoices(customers.getCustomerId(), hostelId, currentMonthBillingDates.currentBillStartDate());
            List<InvoicesV1> findPaidInvoices = listALlCurrentMonthInvoices.stream().filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())).toList();

            if (findPaidInvoices != null && !findPaidInvoices.isEmpty()) {
                return false;
            }

            if (listALlCurrentMonthInvoices.size() > 1) {
                return false;
            }
        } else if (Utils.compareWithTwoDates(oldJoiningDate, currentMonthBillingDates.currentBillStartDate()) < 0) {
            List<InvoicesV1> listInvoices = invoicesV1Repository.findAllInvoicesFromDate(customers.getCustomerId(), hostelId, oldJoiningDate);

            if (!listInvoices.isEmpty()) {
                return false;
            }
        }

        updateJoiningDateOnAdvanceInvoice(customers.getCustomerId(), joinigDate);
        if (Utils.compareWithTwoDates(currentMonthBillingDates.currentBillStartDate(), oldJoiningDate) <= 0) {
            if (Utils.compareWithTwoDates(joinigDate, currentMonthBillingDates.currentBillStartDate()) < 0) {
                //delete current month invoice
                findAndDeleteCurrentMothInvoice(currentMonthBillingDates.currentBillStartDate(), customers.getCustomerId());
            } else {
                findOldInvoiceAndUpdate(customers.getCustomerId(), oldJoiningDate, joinigDate, hostelId, rent);
            }
        } else {
            if (Utils.compareWithTwoDates(oldJoiningDate, currentMonthBillingDates.currentBillStartDate()) < 0) {
                if (Utils.compareWithTwoDates(currentMonthBillingDates.currentBillStartDate(), joinigDate) <= 0) {
//                    findOldInvoiceAndUpdate(customerId, oldJoiningDate, joinigDate, hostelId, rent);
                    createNewInvoiceAfterForCurrentBillingCycle(customers, joinigDate, hostelId, rent, currentMonthBillingDates);
                }
            }
        }

        return true;

    }

    private void createNewInvoiceAfterForCurrentBillingCycle(Customers customer, Date joinigDate, String hostelId, Double rent, BillingDates billingDates) {
        long findNoOfDaysInCurrentMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
        long finNoOfDysGoingToStay = Utils.findNumberOfDays(joinigDate, billingDates.currentBillEndDate());
        double rentPerDay = rent / findNoOfDaysInCurrentMonth;

        double totalRentForCurrentMonth = Math.round(rentPerDay * finNoOfDysGoingToStay);

        InvoicesV1 invoicesV1 = new InvoicesV1();
        invoicesV1.setBasePrice(totalRentForCurrentMonth);
        invoicesV1.setTotalAmount(totalRentForCurrentMonth);
        invoicesV1.setInvoiceStartDate(joinigDate);
        invoicesV1.setInvoiceEndDate(billingDates.currentBillEndDate());
        invoicesV1.setCreatedBy(authentication.getName());
        invoicesV1.setCreatedAt(new Date());
        invoicesV1.setInvoiceType(InvoiceType.RENT.name());
        invoicesV1.setCustomerId(customer.getCustomerId());
        invoicesV1.setCustomerMobile(customer.getMobile());
        invoicesV1.setCustomerMailId(customer.getEmailId());
        invoicesV1.setInvoiceNumber(generateInvoiceNumber(hostelId, InvoiceType.RENT.name()));
        invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
        invoicesV1.setPaidAmount(0.0);

        invoicesV1.setCreatedBy(authentication.getName());
        invoicesV1.setInvoiceDueDate(Utils.addDaysToDate(joinigDate, billingDates.dueDays()));
        invoicesV1.setGst(0.0);
        invoicesV1.setCgst(0.0);
        invoicesV1.setSgst(0.0);
        invoicesV1.setGstPercentile(0.0);
        invoicesV1.setCreatedAt(Utils.convertToTimeStamp(new Date()));
        invoicesV1.setInvoiceGeneratedDate(Utils.convertToTimeStamp(joinigDate));
        invoicesV1.setCancelled(false);
        invoicesV1.setInvoiceMode(InvoiceMode.AUTOMATIC.name());
        invoicesV1.setHostelId(hostelId);

        InvoiceItems invoiceItems = new InvoiceItems();
        invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
        invoiceItems.setAmount(totalRentForCurrentMonth);
        invoiceItems.setInvoice(invoicesV1);

        List<InvoiceItems> listInvoiceItems = new ArrayList<>();
        listInvoiceItems.add(invoiceItems);
        invoicesV1.setInvoiceItems(listInvoiceItems);

        invoicesV1Repository.save(invoicesV1);

    }

    private void findAndDeleteCurrentMothInvoice(Date date, String customerId) {
        InvoicesV1 invoicesV1 = invoicesV1Repository.findLatestRentInvoiceByCustomerId(customerId);
        invoicesV1Repository.delete(invoicesV1);
    }

    public List<InvoicesV1> findRentInvoiceByCustomerId(String customerId) {
        List<InvoicesV1> listNotPaidInvoices = invoicesV1Repository.findByCustomerIdAndInvoiceType(customerId, InvoiceType.RENT.name()).stream().filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())).toList();
        return invoicesV1Repository.findByCustomerIdAndInvoiceType(customerId, InvoiceType.RENT.name());
    }

    /**
     * this is to change the old rental amounts
     *
     * @param customers
     * @param newRentalAmount
     * @param joiningDate
     * @param billingDates
     * @return
     */
    public boolean canChangeRentalAmount(Customers customers, Double newRentalAmount, Date joiningDate, BillingDates billingDates) {
        List<InvoicesV1> listInvoices = invoicesV1Repository.findAllRentInvoicesByCustomerId(customers.getCustomerId());
        List<InvoicesV1> recurringInvoices = listInvoices.stream().filter(i -> i.getInvoiceMode().equalsIgnoreCase(InvoiceMode.RECURRING.name())).toList();

        if (!recurringInvoices.isEmpty()) {
            return false;
        }

        List<InvoicesV1> paidInvoice = listInvoices.stream().filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())).toList();
        if (!paidInvoice.isEmpty()) {
            return false;
        }

        List<InvoicesV1> manualInvoices = listInvoices.stream().filter(i -> i.getInvoiceMode().equalsIgnoreCase(InvoiceMode.MANUAL.name())).toList();
        if (!manualInvoices.isEmpty()) {
            return false;
        }

        List<CustomersBedHistory> listCustomersBedHistory = customersBedHistoryService.getCheckedInReassignedHistory(customers.getCustomerId());
        if (listCustomersBedHistory.size() > 1) {
            return false;
        }

        long findNoOfDaysInJoiningMonth = Utils.findNumberOfDays(billingDates.currentBillStartDate(), billingDates.currentBillEndDate());
        double rentPerDay = newRentalAmount / findNoOfDaysInJoiningMonth;
        long findNoOfDaysStayedInTheMonth = Utils.findNumberOfDays(joiningDate, billingDates.currentBillEndDate());

        double totalRentForCurrentMonth = Math.round(findNoOfDaysStayedInTheMonth * rentPerDay);

        InvoicesV1 invoicesV1 = invoicesV1Repository.findLatestRentInvoiceByCustomerId(customers.getCustomerId());
        if (invoicesV1 != null) {
            invoicesV1.setBasePrice(totalRentForCurrentMonth);
            invoicesV1.setTotalAmount(totalRentForCurrentMonth);
            invoicesV1.setInvoiceStartDate(joiningDate);
            invoicesV1.setInvoiceEndDate(billingDates.currentBillEndDate());
            invoicesV1.setCreatedBy(authentication.getName());
            invoicesV1.setCreatedAt(new Date());
            invoicesV1.setInvoiceType(InvoiceType.RENT.name());
            invoicesV1.setCustomerId(customers.getCustomerId());
            invoicesV1.setCustomerMobile(customers.getMobile());
            invoicesV1.setCustomerMailId(customers.getEmailId());
            invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
            invoicesV1.setPaidAmount(0.0);

            invoicesV1.setCreatedBy(authentication.getName());
            invoicesV1.setInvoiceDueDate(Utils.addDaysToDate(joiningDate, billingDates.dueDays()));
            invoicesV1.setGst(0.0);
            invoicesV1.setCgst(0.0);
            invoicesV1.setSgst(0.0);
            invoicesV1.setGstPercentile(0.0);
            invoicesV1.setCreatedAt(Utils.convertToTimeStamp(new Date()));
            invoicesV1.setInvoiceGeneratedDate(Utils.convertToTimeStamp(joiningDate));
            invoicesV1.setCancelled(false);
            invoicesV1.setInvoiceMode(InvoiceMode.AUTOMATIC.name());
            invoicesV1.setHostelId(customers.getHostelId());

            invoicesV1.getInvoiceItems().stream().filter(item -> com.smartstay.smartstay.ennum.InvoiceItems.RENT.name().equalsIgnoreCase(item.getInvoiceItem())).findFirst().map(item -> {
                item.setAmount(totalRentForCurrentMonth);
                return item;
            }).ifPresent(modifiedRentItems -> invoiceItemService.updateInvoiceItems(modifiedRentItems));


            invoicesV1Repository.save(invoicesV1);
        }


//        InvoiceItems invoiceItems = new InvoiceItems();
//        invoiceItems.setInvoiceItem(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name());
//        invoiceItems.setAmount(totalRentForCurrentMonth);
//        invoiceItems.setInvoice(invoicesV1);
//
//        invoiceItemService.updateInvoiceItems(invoiceItems);


        return true;
    }

    public InvoicesV1 findRunningInvoice(String customerId, BillingDates billDate) {
        return invoicesV1Repository.findCurrentRunningInvoice(customerId, billDate.currentBillStartDate());
    }

    public List<InvoicesV1> findAllCurrentMonthRentalInvoice(String customerId, String hostelId, Date startDate) {

        return invoicesV1Repository.findAllCurrentMonthInvoices(customerId, hostelId, startDate);
    }

    public void updateAdvaceAmount(InvoicesV1 advanceInvoice, Double newAdvance) {
        advanceInvoice.setBasePrice(newAdvance);

        advanceInvoice.getInvoiceItems().stream().filter(i -> i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.ADVANCE.name())).findFirst().map(i -> {
            i.setAmount(newAdvance);
            return i;
        }).ifPresent(i -> invoiceItemService.updateInvoiceItems(i));

        advanceInvoice.setTotalAmount(newAdvance);

        invoicesV1Repository.save(advanceInvoice);

    }

    public List<InvoicesV1> findInvoices(List<String> invoicesIds) {
        return invoicesV1Repository.findByInvoiceIdIn(invoicesIds);
    }

    public int findAllRentalInvoicesAmountExceptCurrentMonth(String customerId, String hostelId) {
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);

        return invoicesV1Repository.findAllRentalInvoicesExceptCurrentMonth(customerId, hostelId, billingDates.currentBillStartDate()).size();
    }

    public InvoicesV1 findLatestInvoice(String customerId) {
        return invoicesV1Repository.findLatestInvoiceByCustomerId(customerId);
    }

    public List<InvoicesV1> findLatestInvoicesByCustomerIds(List<String> customerIds) {
        return invoicesV1Repository.findLatestInvoicesByCustomerIds(customerIds);
    }

    public InvoicesV1 deleteReceipt(InvoicesV1 invoicesV1, TransactionV1 transactionV1) {
        double receiptAmount = transactionV1.getPaidAmount();
        double newPaidAmount = invoicesV1.getPaidAmount() - receiptAmount;

        invoicesV1.setPaidAmount(newPaidAmount);
        if (newPaidAmount > 0) {
            invoicesV1.setPaymentStatus(PaymentStatus.PARTIAL_PAYMENT.name());
        } else {
            invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
        }

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name())) {
            bookingsService.deleteBookingReceipt(invoicesV1.getCustomerId(), receiptAmount);
        }
        return invoicesV1Repository.save(invoicesV1);
    }

    public ResponseEntity<?> updateRecurringInvoice(String hostelId, String invoiceId, List<UpdateRecurringInvoice> recurringInvoiceItems) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_INVOICE, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        InvoicesV1 invoicesV1 = invoicesV1Repository.findById(invoiceId).orElse(null);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }
        if (!invoicesV1.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!invoicesV1.getInvoiceMode().equalsIgnoreCase(InvoiceMode.RECURRING.name())) {
            return new ResponseEntity<>(Utils.EDIT_ALLOWED_ONLY_RECURRING_INVOICE, HttpStatus.BAD_REQUEST);
        }
        if (recurringInvoiceItems == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
            return new ResponseEntity<>(Utils.CANNOT_EDIT_PAID_INVOICES, HttpStatus.BAD_REQUEST);
        }

        Double newInvoiceAmount = invoiceItemService.updateRecurringInvoiceItems(recurringInvoiceItems, invoicesV1);

        invoicesV1.setTotalAmount(newInvoiceAmount);

        invoicesV1Repository.save(invoicesV1);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    public ResponseEntity<?> getInvoiceDetailsForEdit(String hostelId, String invoiceId) {
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
        InvoicesV1 invoicesV1 = invoicesV1Repository.findById(invoiceId).orElse(null);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }
        if (!invoicesV1.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        String invoiceMode = null;
        String paymentStatus = null;
        boolean canEdit = false;

        if (invoicesV1.getInvoiceMode().equalsIgnoreCase(InvoiceMode.RECURRING.name())) {
            canEdit = true;
            invoiceMode = "Recurring";
        } else if (invoicesV1.getInvoiceMode().equalsIgnoreCase(InvoiceMode.MANUAL.name())) {
            invoiceMode = "Manual";
        } else if (invoicesV1.getInvoiceMode().equalsIgnoreCase(InvoiceMode.AUTOMATIC.name())) {
            invoiceMode = "Automatic";
        }

        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
            paymentStatus = "Paid";
        } else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING.name())) {
            paymentStatus = "Pending";
            canEdit = true;
        } else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name())) {
            paymentStatus = "Partial Payment";
        } else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.REFUNDED.name())) {
            paymentStatus = "Refunded";
        } else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PENDING_REFUND.name())) {
            paymentStatus = "Pending Refund";
        } else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_REFUND.name())) {
            paymentStatus = "Partially Refunded";
        } else if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name())) {
            paymentStatus = "Cancelled";
        }

        if (invoicesV1.isCancelled()) {
            paymentStatus = "Cancelled";
            canEdit = false;
        }

        List<com.smartstay.smartstay.responses.invoices.InvoiceItems> invoiceItems = invoicesV1.getInvoiceItems().stream().map(i -> {
            String invoiceItem = null;
            if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.MAINTENANCE.name())) {
                invoiceItem = "Maintenance";
            } else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.RENT.name())) {
                invoiceItem = "Rent";
            } else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.EB.name())) {
                invoiceItem = "EB";
            } else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.OTHERS.name())) {
                invoiceItem = i.getOtherItem();
            } else if (i.getInvoiceItem().equalsIgnoreCase(com.smartstay.smartstay.ennum.InvoiceItems.AMENITY.name())) {
                invoiceItem = "Amenity";
            }
            com.smartstay.smartstay.responses.invoices.InvoiceItems item = new com.smartstay.smartstay.responses.invoices.InvoiceItems(invoicesV1.getInvoiceNumber(), invoiceItem, i.getAmount());
            return item;
        }).toList();


        Details details = new Details(invoicesV1.getInvoiceNumber(), Utils.dateToString(invoicesV1.getInvoiceStartDate()), Utils.dateToString(invoicesV1.getInvoiceEndDate()), Utils.dateToString(invoicesV1.getInvoiceDueDate()), invoiceMode, paymentStatus, invoicesV1.getTotalAmount(), canEdit, invoiceItems);

        return new ResponseEntity<>(details, HttpStatus.OK);
    }

    public InvoicesV1 findSettlementInvoiceByCustomerId(String customerId, String hostelId) {
        return invoicesV1Repository.findByCustomerIdAndHostelIdAndInvoiceType(customerId, hostelId, InvoiceType.SETTLEMENT.name());
    }

    /**
     * this is for reports page.
     * it fetches only rent, reassign rent
     *
     * @param hostelId
     * @param startDate
     * @param endDate
     * @return
     */
    public List<InvoicesV1> findInvoiceByHostelIdAndStartAndEndDate(String hostelId, Date startDate, Date endDate) {
        List<String> invoiceTypes = new ArrayList<>();
        invoiceTypes.add(InvoiceType.RENT.name());
        invoiceTypes.add(InvoiceType.REASSIGN_RENT.name());
        invoiceTypes.add(InvoiceType.ADVANCE.name());
        invoiceTypes.add(InvoiceType.RENT.name());
        List<InvoicesV1> listInvoices = invoicesV1Repository.findInvoiceByHostelIdAndStartDateAndEndDate(hostelId, startDate, endDate, invoiceTypes);
        if (listInvoices != null) {
            return listInvoices;
        }
        return new ArrayList<>();
    }

    public List<InvoicesV1> findByInvoiceIdIn(List<String> invoiceIds) {
        return invoicesV1Repository.findByInvoiceIdIn(invoiceIds);
    }


    public int countByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return invoicesV1Repository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
    }

    public Double sumTotalAmountByHostelIdAndDateRangeExcludingSettlement(String hostelId, String invoiceType, Date startDate, Date endDate) {
        return invoicesV1Repository.sumTotalAmountByHostelIdAndDateRangeExcludingSettlement(hostelId, invoiceType, startDate, endDate);
    }


    public Double sumPaidAmountByHostelIdAndDateRangeExcludingSettlement(String hostelId, String invoiceType, Date startDate, Date endDate) {
        return invoicesV1Repository.sumPaidAmountByHostelIdAndDateRangeExcludingSettlement(hostelId, invoiceType, startDate, endDate);
    }

    public List<InvoicesV1> getCurrentMonthFinalSettlement(String hostelId, Date startDate, Date endDate) {
        return invoicesV1Repository.findSettlementByHostelIdAndStartDateAndEndDate(hostelId, startDate, endDate);
    }

    public InvoiceAggregateDto getInvoiceAggregatesForReport(String hostelId, Date startDate, Date endDate, String search, List<String> paymentStatus, List<String> invoiceModes, List<String> invoiceTypes, List<String> createdBy, Double minPaidAmount, Double maxPaidAmount, Double minOutstandingAmount, Double maxOutstandingAmount) {
        List<String> customerIds = null;
        if (search != null && !search.trim().isEmpty()) {
            List<Customers> customers = customersService.searchCustomerByHostelName(hostelId, search);
            if (customers == null || customers.isEmpty()) {
                return new InvoiceAggregateDto(0L, 0.0, 0.0, 0.0, 0.0, 0.0);
            }
            customerIds = customers.stream().map(Customers::getCustomerId).collect(Collectors.toList());
        }

//        if (customerIds != null && !customerIds.isEmpty()) {
//            return invoicesV1Repository.findInvoiceAggregatesByFiltersWithCustomers(hostelId, startDate, endDate, customerIds, paymentStatus, invoiceModes, invoiceTypes, createdBy, minPaidAmount, maxPaidAmount, minOutstandingAmount, maxOutstandingAmount);
//        } else {
//            return invoicesV1Repository.findInvoiceAggregatesByFilters(hostelId, startDate, endDate, paymentStatus, invoiceModes, invoiceTypes, createdBy, minPaidAmount, maxPaidAmount, minOutstandingAmount, maxOutstandingAmount);
//        }

        return null;
    }

    public List<Object[]> getDistinctCreators(String hostelId) {
        List<String> creatorIds = invoicesV1Repository.findDistinctCreatedBy(hostelId);
        if (creatorIds == null || creatorIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Users> users = usersService.findAllUsersFromUserId(creatorIds);
        return users.stream().map(u -> new Object[]{u.getUserId(), u.getFirstName(), u.getLastName()}).collect(Collectors.toList());
    }

    public List<InvoicesV1> getInvoicesForReport(String hostelId, Date startDate, Date endDate, String search, List<String> paymentStatus, List<String> invoiceModes, List<String> invoiceTypes, List<String> createdBy, Double minPaidAmount, Double maxPaidAmount, Double minOutstandingAmount, Double maxOutstandingAmount, List<Boolean> isCancelled, Pageable pageable) {
        List<String> customerIds = null;
        if (search != null && !search.trim().isEmpty()) {
            List<Customers> customers = customersService.searchCustomerByHostelName(hostelId, search);
            if (customers == null || customers.isEmpty()) {
                return new ArrayList<>();
            }
            customerIds = customers.stream().map(Customers::getCustomerId).collect(Collectors.toList());
        }

        if (customerIds != null && !customerIds.isEmpty()) {
            return invoicesV1Repository.findInvoicesByFiltersWithCustomers(hostelId, startDate, endDate, customerIds, paymentStatus, invoiceModes, invoiceTypes, createdBy, minPaidAmount, maxPaidAmount, minOutstandingAmount, maxOutstandingAmount, isCancelled, pageable);
        } else {
            return invoicesV1Repository.findInvoicesByFilters(hostelId, startDate, endDate, paymentStatus, invoiceModes, invoiceTypes, createdBy, minPaidAmount, maxPaidAmount, minOutstandingAmount, maxOutstandingAmount, isCancelled, pageable);
        }
    }

    public List<String> findInvoiceIdsByHostelIdAndTypeIn(String hostelId, List<String> invoiceTypes) {
        return invoicesV1Repository.findInvoiceIdsByHostelIdAndTypeIn(hostelId, invoiceTypes);
    }

    public ResponseEntity<?> downloadInvoice(String hostelId, String invoiceId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        InvoicesV1 invoicesV1 = invoicesV1Repository.findById(invoiceId).orElse(null);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }
        if (!invoicesV1.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
            if (invoicesV1.getInvoiceUrl() != null) {
                return new ResponseEntity<>(invoicesV1.getInvoiceUrl(), HttpStatus.OK);
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String endpoint = reportsUrl + "/v2/reports/invoices/" + hostelId + "/" + invoiceId;
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                invoicesV1.setInvoiceUrl(response.getBody());

                invoicesV1Repository.save(invoicesV1);
            }
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }

    }

    public ResponseEntity<?> shareInvoiceWhatsApp(String hostelId, String invoiceId) {
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
        InvoicesV1 invoicesV1 = invoicesV1Repository.findById(invoiceId).orElse(null);
        if (invoicesV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_ID, HttpStatus.BAD_REQUEST);
        }
        if (!invoicesV1.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

        String invoiceUrl = null;
        if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) && invoicesV1.getInvoiceUrl() != null) {
            invoiceUrl = invoicesV1.getInvoiceUrl();
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String endpoint = reportsUrl + "/v2/reports/invoices/" + hostelId + "/" + invoiceId;
            HttpEntity<Void> request = new HttpEntity<>(headers);
            try {
                ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    invoiceUrl = response.getBody();
                    if (invoicesV1.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                        invoicesV1.setInvoiceUrl(invoiceUrl);
                        invoicesV1Repository.save(invoicesV1);
                    }
                }
            } catch (Exception e) {
                return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
            }
        }

        if (invoiceUrl == null) {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }

        Customers customer = customersService.getCustomerInformation(invoicesV1.getCustomerId());
        String customerName = "Guest";
        if (customer != null) {
            customerName = customer.getFirstName();
            if (customer.getLastName() != null && !customer.getLastName().isEmpty()) {
                customerName += " " + customer.getLastName();
            }
        }

        String mobileNumber = invoicesV1.getCustomerMobile();
        if (mobileNumber == null && customer != null) {
            mobileNumber = customer.getMobile();
        }

        if (mobileNumber == null) {
            return new ResponseEntity<>("Customer mobile number not found", HttpStatus.BAD_REQUEST);
        }
        whatsappService.sendInvoiceNotification(mobileNumber, customerName, invoiceUrl.replace("https://smartstaydevs.s3.ap-south-1.amazonaws.com/", ""));

        return new ResponseEntity<>(invoiceUrl, HttpStatus.OK);

    }

}
