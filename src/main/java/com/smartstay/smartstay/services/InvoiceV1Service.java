package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Bills.ReceiptMapper;
import com.smartstay.smartstay.Wrappers.InvoiceListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.bills.BillTemplates;
import com.smartstay.smartstay.dto.bills.PaymentSummary;
import com.smartstay.smartstay.dto.invoices.Invoices;
import com.smartstay.smartstay.dto.transaction.Receipts;
import com.smartstay.smartstay.ennum.InvoiceMode;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.repositories.InvoicesV1Repository;
import com.smartstay.smartstay.responses.invoices.InvoicesList;
import com.smartstay.smartstay.responses.invoices.ReceiptsList;
import com.smartstay.smartstay.util.Utils;
import jdk.jshell.execution.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private BookingsService bookingsService;

    private HostelService hostelService;
    @Autowired
    public void setCustomersService(@Lazy CustomersService customersService) {
        this.customersService = customersService;
    }
    @Autowired
    public void setBookingsService(@Lazy  BookingsService bookingService) {
        this.bookingsService = bookingService;
    }

    @Autowired
    public void setHostelService(@Lazy HostelService hostelService) {
        this.hostelService = hostelService;
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
                    cgst = templates.gstPercentile() /2;
                    sgst = templates.gstPercentile() / 2;
                    baseAmount = amount/(1+(templates.gstPercentile()/100));
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
                status  = "Active";
            }
            else if (type.equalsIgnoreCase(InvoiceType.RENT.name())) {
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
                    basePrice = amount/(1+(templates.gstPercentile()/100));
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
                status  = "Active";
            }
            else if (type.equalsIgnoreCase(InvoiceType.RENT.name())) {
                status = "Active";
            }

            return invV1.getInvoiceId();
        }
        return null;
    }

    /**
     *
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
                    basePrice = amount/(1+(templates.gstPercentile()/100));
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

            invoicesV1.setBasePrice(basePrice);
            invoicesV1.setTotalAmount(amount);
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
                status  = "Active";
            }
            else if (type.equalsIgnoreCase(InvoiceType.RENT.name())) {
                status = "Active";
            }

            PaymentSummary summary = new PaymentSummary(hostelId, customerId, invoiceNumber.toString(), amount, customerMailId, customerMobile, status);
            paymentSummaryService.addInvoice(summary);
        }


    }

    /**
     * Using in transaction service.
     * Changing anything here may impact in trasaction service
     *
     * userd inside transaction service
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
     *
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

    public ResponseEntity<?> generateManualInvoice(String hostelId, String customerId, String startDate, String endDate, Double ebAmount) {
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
        Customers customers = customersService.getCustomerInformation(customerId);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        BookingsV1 bookingV1 = bookingsService.getBookingDetails(customerId);
        if (bookingV1 == null) {
            return new ResponseEntity<>(Utils.NO_RECORDS_FOUND, HttpStatus.BAD_REQUEST);
        }
        int day = 1;
        if (hostelV1.getElectricityConfig() != null) {
            day = hostelV1.getElectricityConfig().getBillDate();
        }

        Date dateStartDate = null;
        Date dateEndDate = null;
        Calendar cal = Calendar.getInstance();
        if (!Utils.checkNullOrEmpty(startDate)) {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
            cal.set(Calendar.DAY_OF_MONTH, day);

            dateStartDate = cal.getTime();
        }
        else {
            dateStartDate = Utils.stringToDate(startDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            cal.setTime(dateStartDate);
            cal.set(Calendar.DAY_OF_MONTH, day);
        }

        if (!Utils.checkNullOrEmpty(endDate)) {
            Calendar endCal = Calendar.getInstance();
            endCal.set(Calendar.MONTH, cal.get(Calendar.MONTH) -1);
            endCal.setTime(Utils.findLastDate(day, endCal.getTime()));
            dateEndDate = endCal.getTime();
        }
        else {
            Calendar endCal = Calendar.getInstance();
            Date dateEnd = Utils.stringToDate(endDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            endCal.setTime(Utils.findLastDate(day, dateEnd));

            dateEndDate = endCal.getTime();

        }

        return null;


    }

    public List<InvoicesV1> listAllUnpaidInvoices(String customerId, String hostelId) {
        return invoicesV1Repository.findByHostelIdAndCustomerIdAndPaymentStatusNotIgnoreCase(hostelId, customerId, PaymentStatus.PAID.name());
    }

    public InvoicesV1 getCurrentMonthInvoice(String customerId) {
        return invoicesV1Repository.findLatestInvoiceByCustomerId(customerId);
    }
}
