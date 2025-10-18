package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Bills.ReceiptMapper;
import com.smartstay.smartstay.Wrappers.InvoiceListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.bills.BillTemplates;
import com.smartstay.smartstay.dto.bills.PaymentSummary;
import com.smartstay.smartstay.dto.invoices.Invoices;
import com.smartstay.smartstay.dto.transaction.Receipts;
import com.smartstay.smartstay.ennum.BillConfigTypes;
import com.smartstay.smartstay.ennum.InvoiceMode;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.payloads.invoice.ManualInvoice;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.responses.invoices.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class InvoiceV1Service {
    @Autowired
    InvoicesV1Repository invoicesV1Repository;
    @Autowired
    Authentication authentication;
    @Autowired
    TemplatesService templateService;
    @Autowired
    private UsersService usersService;
    private CustomersService customersService;
    @Autowired
    PaymentSummaryService paymentSummaryService;
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
    private TransactionService transactionService;

    private BookingsService bookingsService;
    private HostelService hostelService;
    private BedsService bedService;


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

    public void addInvoice(String customerId, Double amount, String type, String hostelId, String customerMobile, String customerMailId, String joiningDate, int startDay) {
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
                existingV1 = invoicesV1Repository.findLatestInvoiceByPrefix(templates.prefix());
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

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Utils.USER_INPUT_DATE_FORMAT);
            LocalDate joiningDate1 = LocalDate.parse(joiningDate.replace("/", "-"), formatter);
            LocalDate dueDate = joiningDate1.plusDays(5);
            Date endDate = Utils.findLastDate(startDay, Utils.stringToDate(joiningDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT));

            invoicesV1.setTotalAmount(amount);
            invoicesV1.setBasePrice(baseAmount);
            invoicesV1.setInvoiceType(type);
            invoicesV1.setCustomerId(customerId);
            invoicesV1.setInvoiceNumber(invoiceNumber.toString());
            invoicesV1.setPaymentStatus(PaymentStatus.PENDING.name());
            invoicesV1.setCreatedBy(authentication.getName());
            invoicesV1.setGst(gstAmount);
            invoicesV1.setCgst(cgst);
            invoicesV1.setSgst(sgst);
            invoicesV1.setGstPercentile(gstPercentile);
            invoicesV1.setInvoiceDueDate(java.sql.Date.valueOf(dueDate));
            invoicesV1.setCustomerMobile(customerMobile);
            invoicesV1.setCustomerMailId(customerMailId);
            invoicesV1.setCreatedAt(new Date());
            invoicesV1.setInvoiceStartDate(java.sql.Date.valueOf(joiningDate1));
            invoicesV1.setInvoiceEndDate(endDate);
            invoicesV1.setInvoiceGeneratedDate(java.sql.Date.valueOf(joiningDate1));
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
     * this is used only for booking purpose. Do not use it any where
     */
    public String addBookingInvoice(String customerId, Double amount, String type, String hostelId, String customerMobile, String customerMailId, String bankId, String referenceNumber) {
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
                existingV1 = invoicesV1Repository.findLatestInvoiceByPrefix(templates.prefix());
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
            invoicesV1.setInvoiceType(type);
            invoicesV1.setCustomerId(customerId);
            invoicesV1.setInvoiceNumber(invoiceNumber.toString());
            invoicesV1.setInvoiceType(InvoiceType.BOOKING.name());
            invoicesV1.setPaymentStatus(PaymentStatus.PAID.name());
            invoicesV1.setCreatedBy(authentication.getName());
            invoicesV1.setInvoiceDueDate(Utils.addDaysToDate(new Date(), 0));
            invoicesV1.setCustomerMobile(customerMobile);
            invoicesV1.setCustomerMailId(customerMailId);
            invoicesV1.setCgst(cgst);
            invoicesV1.setSgst(sgst);
            invoicesV1.setGst(gstAmount);
            invoicesV1.setGstPercentile(gstPercentile);
            invoicesV1.setCreatedAt(new Date());
            invoicesV1.setInvoiceGeneratedDate(new Date());
            invoicesV1.setInvoiceMode(InvoiceMode.AUTOMATIC.name());
            invoicesV1.setHostelId(hostelId);


            InvoicesV1 invV1 = invoicesV1Repository.save(invoicesV1);
            String status = null;
            if (type.equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
                status = "Active";
            } else if (type.equalsIgnoreCase(InvoiceType.RENT.name())) {
                status = "Active";
            }

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
                existingV1 = invoicesV1Repository.findLatestInvoiceByPrefix(templates.prefix());
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

    public ResponseEntity<?> getTransactions(String hostelId) {
        List<Invoices> listInvoices = invoicesV1Repository.findByHostelId(hostelId);

        List<InvoicesList> invoicesResponse = listInvoices
                .stream()
                .map(item -> new InvoiceListMapper().apply(item))
                .toList();

        return new ResponseEntity<>(invoicesResponse, HttpStatus.OK);
    }

    public int recordPayment(String invoiceId, String status) {
        InvoicesV1 invoice = invoicesV1Repository.findById(invoiceId).orElse(null);
        if (invoice != null) {
            invoice.setPaymentStatus(status);
            invoice.setUpdatedAt(new Date());
            invoice.setUpdatedBy(authentication.getName());
            invoicesV1Repository.save(invoice);
            return 1;
        }
        return 0;
    }


    public ResponseEntity<?> getAllReceipts(String hostelId) {
        List<Receipts> listReceipts = invoicesV1Repository.findReceipts(hostelId);
        List<ReceiptsList> receipts = listReceipts
                .stream()
                .map(item -> new ReceiptMapper().apply(item))
                .toList();
        return new ResponseEntity<>(receipts, HttpStatus.OK);
    }

    public ResponseEntity<?> getAllReceiptsByHostelId(String hostelId) {
        List<Receipts> listReceipts = transactionService.getAllReceiptsByHostelId(hostelId);
        List<ReceiptsList> receipts = listReceipts
                .stream()
                .map(item -> new ReceiptMapper().apply(item))
                .toList();
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
        return invoiceV1.getTotalAmount();
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
        HostelV1 hostelV1 = hostelService.getHostelInfo(customers.getHostelId());
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (!Utils.checkNullOrEmpty(manualInvoice.invoiceDate())) {
            return new ResponseEntity<>(Utils.INVALID_INVOICE_DATE, HttpStatus.BAD_REQUEST);
        }
        String invoiceNumber = null;
        double ebAmount = 0.0;
        if (Utils.checkNullOrEmpty(manualInvoice.ebAmount())) {
            ebAmount = manualInvoice.ebAmount();
        }
        StringBuilder prefixSuffix = new StringBuilder();
        if (Utils.checkNullOrEmpty(manualInvoice.invoiceNumber())) {
            if (invoicesV1Repository.findByInvoiceNumberAndHostelId(manualInvoice.invoiceNumber(), customers.getHostelId()) != null) {
                return new ResponseEntity<>(Utils.INVOICE_NUMBER_ALREADY_REGISTERED, HttpStatus.BAD_REQUEST);
            }
            else {
                invoiceNumber = manualInvoice.invoiceNumber();
                prefixSuffix.append(invoiceNumber);
            }
        }
        else {
            String prefix = "INV";
            com.smartstay.smartstay.dao.BillTemplates templates = templatesService.getTemplateByHostelId(customers.getHostelId());
            if (templates != null && templates.getTemplateTypes() != null) {
                if (!templates.getTemplateTypes().isEmpty()) {
                    prefix = templates.getTemplateTypes().get(templates.getTemplateTypes().size()-1).getInvoicePrefix();
                }
                prefixSuffix.append(prefix);
            }
            InvoicesV1 inv = invoicesV1Repository.findLatestInvoiceByPrefix(prefix);
            String[] prefArr = inv.getInvoiceNumber().split("-");
            if (prefArr.length > 1) {
                int suffix = Integer.parseInt(prefArr[1]) + 1;
                prefixSuffix.append("-");
                if (suffix < 10) {
                    prefixSuffix.append("00");
                    prefixSuffix.append(suffix);
                }
                else if (suffix < 100) {
                    prefixSuffix.append("0");
                    prefixSuffix.append(suffix);
                }
                else {
                    prefixSuffix.append(suffix);
                }
            }
        }



        int day = 1;
        if (hostelV1.getElectricityConfig() != null) {
            day = hostelV1.getElectricityConfig().getBillDate();
        }

        Date dateStartDate = null;
        Date dateEndDate = null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(Utils.stringToDate(manualInvoice.invoiceDate(), Utils.USER_INPUT_DATE_FORMAT));
        cal.set(Calendar.DAY_OF_MONTH, day);

        dateStartDate = cal.getTime();
        dateEndDate = Utils.findLastDate(day, cal.getTime());

        if (Utils.compareWithTwoDates(cal.getTime(), new Date()) > 0) {
            return new ResponseEntity<>(Utils.FUTURE_DATES_NOT_ALLOWED, HttpStatus.BAD_REQUEST);
        }

        if (invoicesV1Repository.findInvoiceByCustomerIdAndDate(customerId, dateStartDate, dateEndDate) != null) {
            return new ResponseEntity<>(Utils.INVOICE_ALREADY_PRESENT, HttpStatus.BAD_REQUEST);
        }

        BookingsV1 filteredBooking = bookingsService.getBookingByCustomerIdAndDate(customerId, dateStartDate, dateEndDate);
        if (filteredBooking == null) {
            return new ResponseEntity<>(Utils.CUSTOMER_NOT_CHECKED_IN_DATE, HttpStatus.BAD_REQUEST);
        }

        Date invoiceStartDate = null;
        Date invoiceEndDate = null;
        Date invoiceDueDate = null;

        if (Utils.compareWithTwoDates(filteredBooking.getJoiningDate(), dateStartDate) <=0) {
            invoiceStartDate = dateStartDate;
            invoiceDueDate = Utils.addDaysToDate(invoiceStartDate, 5);
        }
        else {
            invoiceStartDate = filteredBooking.getJoiningDate();
            invoiceDueDate = Utils.addDaysToDate(filteredBooking.getJoiningDate(), 5);
        }

        if (filteredBooking.getLeavingDate() == null) {
            invoiceEndDate = dateEndDate;
        }
        else if (Utils.compareWithTwoDates(filteredBooking.getLeavingDate(), dateEndDate) >= 0) {
            invoiceEndDate = dateEndDate;
        }
        else if (Utils.compareWithTwoDates(filteredBooking.getLeavingDate(), dateEndDate) >= 0) {
            invoiceEndDate = filteredBooking.getLeavingDate();
        }
        else {
            invoiceEndDate = dateEndDate;
        }

        long noOfDaysOnThatMonth = Utils.findNumberOfDays(dateStartDate, dateEndDate) + 1;
        long noOfDaysStayed = Utils.findNumberOfDays(invoiceStartDate, invoiceEndDate) + 1;
        double invoiceAmount = 0.0;

        if (noOfDaysOnThatMonth == noOfDaysStayed) {
            invoiceAmount = manualInvoice.rentAmount();
        }
        else {
            double rentPerDay = manualInvoice.rentAmount()/noOfDaysOnThatMonth;
            invoiceAmount = rentPerDay*noOfDaysStayed;
        }

        InvoicesV1 invoicesV1 = new InvoicesV1();
        invoicesV1.setInvoiceNumber(prefixSuffix.toString());
        invoicesV1.setBasePrice(invoiceAmount);
        invoicesV1.setCustomerId(customerId);
        invoicesV1.setHostelId(customers.getHostelId());
        invoicesV1.setInvoiceType(InvoiceType.RENT.name());
        invoicesV1.setCustomerMailId(customers.getEmailId());
        invoicesV1.setCustomerMobile(customers.getMobile());
        invoicesV1.setEbAmount(ebAmount);
        invoicesV1.setTotalAmount(invoiceAmount + ebAmount);
        invoicesV1.setGst(0.0);
        invoicesV1.setCgst(0.0);
        invoicesV1.setSgst(0.0);
        invoicesV1.setGstPercentile(0.0);
        invoicesV1.setPaymentStatus(PaymentStatus.PAID.name());
        invoicesV1.setOthersDescription("");
        invoicesV1.setInvoiceMode(InvoiceMode.MANUAL.name());
        invoicesV1.setCreatedBy(authentication.getName());
        invoicesV1.setInvoiceGeneratedDate(new Date());
        invoicesV1.setInvoiceStartDate(invoiceStartDate);
        invoicesV1.setInvoiceDueDate(invoiceDueDate);
        invoicesV1.setInvoiceEndDate(invoiceEndDate);

        invoicesV1Repository.save(invoicesV1);


        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);


    }

    public List<InvoicesV1> listAllUnpaidInvoices(String customerId, String hostelId) {
        return invoicesV1Repository.findByHostelIdAndCustomerIdAndPaymentStatusNotIgnoreCase(hostelId, customerId, PaymentStatus.PAID.name());
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

        String hostelPhone = null;
        String hostelEmail = null;
        String invoiceType = "Rent";
        StringBuilder hostelFullAddress = new StringBuilder();
        String invoiceSignatureUrl = null;
        String hostelLogo = null;

        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            invoiceType = "Advance";
        }

        if (hostelV1.getHouseNo() != null) {
            hostelFullAddress.append(hostelV1.getHouseNo());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getStreet() != null) {
            hostelFullAddress.append(hostelV1.getStreet());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getCity() != null) {
            hostelFullAddress.append(hostelV1.getCity());
            hostelFullAddress.append(", ");
        }
        if (hostelV1.getState() != null) {
            hostelFullAddress.append(hostelV1.getState());
            hostelFullAddress.append("-");
        }
        if (hostelV1.getPincode() != 0) {
            hostelFullAddress.append(hostelV1.getPincode());
        }

        AccountDetails accountDetails = null;
        ConfigInfo signatureInfo = null;
        com.smartstay.smartstay.dao.BillTemplates hostelTemplates = templatesService.getTemplateByHostelId(hostelId);
        if (hostelTemplates != null) {
            if (!hostelTemplates.isMobileCustomized()) {
                hostelPhone = hostelTemplates.getMobile();
            }
            else {
                hostelPhone = hostelTemplates.getTemplateTypes()
                        .stream()
                        .filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                        .map(BillTemplateType::getInvoicePhoneNumber)
                        .toString();

            }

            if (!hostelTemplates.isEmailCustomized()) {
                hostelEmail = hostelTemplates.getEmailId();
            }
            else {
                hostelEmail = hostelTemplates.getTemplateTypes()
                        .stream()
                        .filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                        .map(BillTemplateType::getInvoicePhoneNumber)
                        .toString();
            }

            BillTemplateType templateType = hostelTemplates
                    .getTemplateTypes()
                    .stream()
                    .filter(item -> item.getInvoiceType().equalsIgnoreCase(BillConfigTypes.RENTAL.name()))
                    .findAny().get();

            if (!hostelTemplates.isSignatureCustomized()) {
                invoiceSignatureUrl = hostelTemplates.getDigitalSignature();
            } else {
                invoiceSignatureUrl = templateType.getInvoiceSignatureUrl();
            }
            if (!hostelTemplates.isLogoCustomized()) {
                hostelLogo = hostelTemplates.getHostelLogo();
            }
            else {
                hostelLogo = templateType.getInvoiceLogoUrl();
            }

            if (templateType.getBankAccountId() != null) {
                BankingV1 bankingV1 = bankingService.getBankDetails(templateType.getBankAccountId());
                accountDetails = new AccountDetails(bankingV1.getAccountNumber(),
                        bankingV1.getIfscCode(),
                        bankingV1.getBankName(),
                        bankingV1.getUpiId(),
                        templateType.getQrCode());
            }
            else {
                accountDetails = new AccountDetails(null,
                        null,
                        null,
                        null,
                        templateType.getQrCode());
            }


            signatureInfo = new ConfigInfo(templateType.getInvoiceTermsAndCondition(),
                    invoiceSignatureUrl,
                    hostelLogo,
                    hostelFullAddress.toString(),
                    templateType.getInvoiceTemplateColor(),
                    templateType.getInvoiceNotes(),
                    invoiceType);
        }

        if (hostelV1.getBillingRulesList() != null && !hostelV1.getBillingRulesList().isEmpty()) {

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

            customerInfo = new CustomerInfo(customers.getFirstName(),
                    customers.getLastName(),
                    fullName.toString(),
                    customers.getMobile(),
                    "91",
                    fullAddress.toString(),
                    Utils.dateToString(customers.getJoiningDate()));
        }

        StayInfo stayInfo = null;
        CustomersBedHistory bedHistory = customersBedHistoryService.getCustomerBedByStartDate(customers.getCustomerId(), invoicesV1.getInvoiceStartDate(), invoicesV1.getInvoiceEndDate());

        BedDetails bedDetails = bedService.getBedDetails(bedHistory.getBedId());
        if (bedDetails != null) {
            stayInfo = new StayInfo(bedDetails.getBedName(),
                    bedDetails.getFloorName(),
                    bedDetails.getRoomName(),
                    hostelV1.getHostelName());
        }

        Double subTotal = 0.0;
        Double paidAmount = 0.0;
        Double balanceAmount = 0.0;

        paidAmount = transactionService.findPaidAmountForInvoice(invoiceId);
        balanceAmount = invoicesV1.getTotalAmount() - paidAmount;
        subTotal = invoicesV1.getTotalAmount();
        if (invoicesV1.getEbAmount() != null) {
            subTotal = subTotal + invoicesV1.getEbAmount();
        }

        List<InvoiceItems> listInvoiceItems = new ArrayList<>();
        if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.RENT.name())) {
            InvoiceItems items1 = new InvoiceItems(invoicesV1.getInvoiceNumber(), "Rent", invoicesV1.getBasePrice());
            listInvoiceItems.add(items1);
        }
        else if (invoicesV1.getInvoiceType().equalsIgnoreCase(InvoiceType.ADVANCE.name())) {
            InvoiceItems items1 = new InvoiceItems(invoicesV1.getInvoiceNumber(), "Advance", invoicesV1.getBasePrice());
            listInvoiceItems.add(items1);
        }

        if (invoicesV1.getEbAmount() != null && invoicesV1.getEbAmount() != 0) {
            InvoiceItems items2 = new InvoiceItems(invoicesV1.getInvoiceNumber(), "Electricity", invoicesV1.getEbAmount());
            listInvoiceItems.add(items2);
        }



        InvoiceInfo invoiceInfo = new InvoiceInfo(subTotal,
                0.0,
                0.0,
                invoicesV1.getTotalAmount(),
                paidAmount,
                balanceAmount,
                listInvoiceItems);





        InvoiceDetails details = new InvoiceDetails(invoicesV1.getInvoiceNumber(),
                invoicesV1.getInvoiceId(),
                Utils.dateToString(invoicesV1.getInvoiceStartDate()),
                Utils.dateToString(invoicesV1.getInvoiceDueDate()),
                hostelEmail,
                hostelPhone,
                        "91",
                        customerInfo,
                        stayInfo,
                        invoiceInfo,
                accountDetails,
                signatureInfo);
        return new ResponseEntity<>(details, HttpStatus.OK);

    }
}
