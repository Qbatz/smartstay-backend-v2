package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.InvoiceItems;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.beds.RoomBedCount;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.reports.ComplaintsReportFilterRequest;
import com.smartstay.smartstay.dto.reports.ComplaintsReportResponse;
import com.smartstay.smartstay.dto.reports.ElectricityForReports;
import com.smartstay.smartstay.dto.reports.ReportsDto;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.payloads.reports.invoice.InvConsolidatedViewPayload;
import com.smartstay.smartstay.responses.Reports.FooterInfo;
import com.smartstay.smartstay.responses.Reports.ReportDetailsResponse;
import com.smartstay.smartstay.responses.Reports.ReportResponse;
import com.smartstay.smartstay.responses.Reports.TenantRegisterResponse;
import com.smartstay.smartstay.responses.Reports.invoice.*;
import com.smartstay.smartstay.responses.filterOptions.ColumnFiltersRes;
import com.smartstay.smartstay.responses.filters.EnumFilterRes;
import com.smartstay.smartstay.responses.filters.FloorsFilterRes;
import com.smartstay.smartstay.responses.filters.UserFilterRes;
import com.smartstay.smartstay.responses.hostel.HostelInfoRes;
import com.smartstay.smartstay.responses.pagination.PaginationDataRes;
import com.smartstay.smartstay.responses.transaction.TransactionReportResponse;
import com.smartstay.smartstay.util.AddressUtils;
import com.smartstay.smartstay.util.CustomerUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import com.smartstay.smartstay.util.columnOptions.InvRegisterConsolidatedColumnUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Value("${REPORTS_URL}")
    private String reportsUrl;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private InvoiceV1Service invoiceV1Service;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private VendorService vendorService;
    @Autowired
    private ComplaintsService complaintService;
    @Autowired
    private ComplaintTypeService complaintTypeService;
    @Autowired
    private AmenityRequestService amenityRequestService;

    @Autowired
    private HostelService hostelService;
    @Autowired
    private ElectricityService electricityService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private FloorsService floorsService;
    @Autowired
    private DownloadService downloadService;
    @Autowired
    private HostelConfigService hostelConfigService;
    @Autowired
    private TableColumnService tableColumnService;

    public ResponseEntity<?> getReports(String hostelId, String customStartDate, String customEndDate) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Date startDate = null;
        Date endDate = null;

        if (customStartDate != null && customEndDate != null) {
            startDate = Utils.stringToDate(customStartDate, Utils.USER_INPUT_DATE_FORMAT);
            endDate = Utils.stringToDate(customEndDate, Utils.USER_INPUT_DATE_FORMAT);
        } else {
            BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
            if (billingDates != null) {
                startDate = billingDates.currentBillStartDate();
                endDate = billingDates.currentBillEndDate();
            }
        }

        ReportsDto reportsDto = invoiceV1Service.getCurrentMonthInvoiceDetailsForReports(hostelId, startDate, endDate);
        // Invoices for hostel
        int invoiceCount = 0;
        Double invoiceTotal = 0.0;
        Double paidTotal = 0.0;
        Double outstandingAmount = 0.0;
        if (reportsDto != null) {
            invoiceCount = reportsDto.invoiceCount();
            invoiceTotal = reportsDto.totalAmount();
            paidTotal = reportsDto.paidAmount();
            outstandingAmount = reportsDto.outstandingAmount();
        }
        ReportResponse.InvoiceReport invoiceReport = ReportResponse.InvoiceReport.builder()
                .noOfInvoices(invoiceCount)
                .totalAmount(Utils.roundOffWithTwoDigit(invoiceTotal)).build();

        // Receipts
        int receiptCount = transactionService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        Double receiptTotal = transactionService.sumPaidAmountByHostelIdAndDateRange(hostelId, startDate,
                endDate);
        ReportResponse.ReceiptReport receiptReport = ReportResponse.ReceiptReport.builder()
                .totalReceipts(receiptCount)
                .totalAmount(Utils.roundOffWithTwoDigit(receiptTotal)).build();

        // Banking
        int bankTransCount = bankTransactionService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        Double bankBalance = bankingService.sumBalanceByHostelId(hostelId);
        ReportResponse.BankingReport bankingReport = ReportResponse.BankingReport.builder()
                .totalTransactions(bankTransCount).totalAmount(Utils.roundOffWithTwoDigit(bankBalance)).build();

        // Tenants
        int filledBeds = bedsService.countOccupiedByHostelId(hostelId);
        int totalBeds = bedsService.countAllByHostelId(hostelId);

        List<BookingsV1> tenantBookings = bookingsService.findAllBookingsWithFilters(hostelId, startDate,
                endDate, null,
                null, null, null);

        Set<String> uniqueTenants = new HashSet<>();
        int activeCount = 0;
        int noticeCount = 0;
        int checkoutCount = 0;

        for (BookingsV1 b : tenantBookings) {
            uniqueTenants.add(b.getCustomerId());
            String status = b.getCurrentStatus();

            if (BookingStatus.CHECKIN.name().equalsIgnoreCase(status)) {
                activeCount++;
            } else if (BookingStatus.NOTICE.name().equalsIgnoreCase(status)) {
                noticeCount++;
            } else if (BookingStatus.VACATED.name().equalsIgnoreCase(status)
                    || BookingStatus.TERMINATED.name().equalsIgnoreCase(status)) {
                checkoutCount++;
            }
        }

        double occupancyRate = 0.0;
        if (totalBeds > 0) {
            occupancyRate = ((double) filledBeds / totalBeds) * 100;
            occupancyRate = Utils.roundOffWithTwoDigit(occupancyRate);
        }

        ReportResponse.TenantReport tenantReport = ReportResponse.TenantReport.builder()
                .totalTenants(uniqueTenants.size())
                .occupancyRate(occupancyRate)
                .noticeTenantCount(noticeCount)
                .activeTenantCount(activeCount)
                .checkoutTenantsCount(checkoutCount).build();

        // Expenses
        int expenseCount = expenseService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        Double expenseTotal = expenseService.sumAmountByHostelIdAndDateRange(hostelId, startDate, endDate);
        ReportResponse.ExpenseReport expenseReport = ReportResponse.ExpenseReport.builder()
                .totalExpenses(expenseCount)
                .totalExpenseAmount(expenseTotal).build();

        Double totalRevenue = 0.0;
        if (invoiceTotal != null && expenseTotal != null) {
            totalRevenue = invoiceTotal - expenseTotal;
        }

        // Vendor
        int vendorCount = vendorService.countByHostelId(hostelId);
        ReportResponse.VendorReport vendorReport = ReportResponse.VendorReport.builder()
                .totalVendors(vendorCount)
                .build();

        // Complaints
        int complaintCount = complaintService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        int complaintActiveCount = complaintService.countActiveByHostelIdAndDateRange(hostelId,
                Arrays.asList(ComplaintStatus.PENDING.name(), ComplaintStatus.OPENED.name()), startDate,
                endDate);
        ReportResponse.ComplaintReport complaintReport = ReportResponse.ComplaintReport.builder()
                .totalComplaints(complaintCount).activeComplaints(complaintActiveCount).build();

        // Requests
        int requestCount = amenityRequestService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        int requestActiveCount = amenityRequestService.countActiveByHostelIdAndDateRange(hostelId,
                Arrays.asList(AmenityRequestStatus.PENDING.name(), AmenityRequestStatus.OPEN.name(),
                        AmenityRequestStatus.INPROGRESS.name()),
                startDate, endDate);
        ReportResponse.RequestReport requestReport = ReportResponse.RequestReport.builder()
                .totalRequests(requestCount)
                .activeRequests(requestActiveCount).build();

        List<InvoicesV1> finalSettlements = invoiceV1Service.getCurrentMonthFinalSettlement(hostelId, startDate,
                endDate);
        int totalSettlement = finalSettlements.size();
        double totalAmount = finalSettlements.stream().mapToDouble(i -> {
            if (i.getTotalAmount() < 0) {
                return i.getTotalAmount() * -1;
            }
            return i.getTotalAmount();
        }).sum();
        double totalReturnedAmount = finalSettlements.stream().filter(i -> i.getTotalAmount() < 0)
                .mapToDouble(i -> {
                    return i.getTotalAmount() * -1;
                }).sum();
        double totalPayableAmount = finalSettlements.stream().filter(i -> i.getTotalAmount() >= 0)
                .mapToDouble(InvoicesV1::getTotalAmount).sum();

        ReportResponse.FinalSettlementReport settlementReport = ReportResponse.FinalSettlementReport.builder()
                .totalReturnedAmount(totalReturnedAmount).totalPaidAmount(totalPayableAmount)
                .totalAmount(totalAmount)
                .totalSettlements(totalSettlement).build();

        Calendar calPreviousMonth = Calendar.getInstance();
        calPreviousMonth.setTime(startDate);
        calPreviousMonth.add(Calendar.MONTH, -1);

        BillingDates previousBillingDate = hostelService.getBillingRuleOnDate(hostelId,
                calPreviousMonth.getTime());
        ElectricityForReports ebReports = electricityService.getPreviousMonthEbAmount(hostelId,
                previousBillingDate.currentBillStartDate(), previousBillingDate.currentBillEndDate());

        ReportResponse.ElectricityReport ebReportResponse = ReportResponse.ElectricityReport.builder()
                .totalAmount(ebReports.totalAmount()).totalUnits(ebReports.totalUnits())
                .totalEntries(ebReports.noOfEntries()).build();

        ReportResponse response = ReportResponse.builder().hostelId(hostelId)
                .startDate(Utils.dateToString(startDate))
                .endDate(Utils.dateToString(endDate))
                .outStandingAmount(Utils.roundOffWithTwoDigit(outstandingAmount))
                .totalRevenue(Utils.roundOffWithTwoDigit(totalRevenue))
                .invoices(invoiceReport)
                .receipts(receiptReport).banking(bankingReport)
                .tenantInfo(tenantReport)
                .expense(expenseReport).vendor(vendorReport).complaints(complaintReport)
                .electricity(ebReportResponse)
                .settlement(settlementReport).requests(requestReport).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceReportDetails(String hostelId, String search, List<String> paymentStatus,
                                                     List<String> invoiceModes, List<String> invoiceTypes, List<String> createdBy, String period,
                                                     Double minPaidAmount, Double maxPaidAmount, Double minOutstandingAmount,
                                                     Double maxOutstandingAmount,
                                                     String customStartDate, String customEndDate, int pageParams, int size) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        AtomicBoolean requestedAll = new AtomicBoolean(false);
        List<String> pStatus = null;
        if (paymentStatus != null) {
            paymentStatus.forEach(i -> {
                if (i.equalsIgnoreCase("ALL")) {
                    requestedAll.set(true);
                }
            });


            if (requestedAll.get()) {
                pStatus = new ArrayList<>();
                pStatus.add(PaymentStatus.PAID.name());
                pStatus.add(PaymentStatus.PENDING.name());
                pStatus.add(PaymentStatus.PARTIAL_PAYMENT.name());
                pStatus.add(PaymentStatus.CANCELLED.name());
            }
            else {
                pStatus = paymentStatus
                        .stream()
                        .toList();

                if (pStatus.isEmpty()) {
                    pStatus = null;
                }
            }
        }

        BillingDates dates = calculateDateRange(period, hostelId);
        Date startDate = dates.currentBillStartDate();
        Date endDate = dates.currentBillEndDate();

        if (customStartDate != null && !customStartDate.isEmpty()) {
            startDate = Utils.stringToDate(customStartDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }
        if (customEndDate != null && !customEndDate.isEmpty()) {
            endDate = Utils.stringToDate(customEndDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }
        if (minOutstandingAmount != null && minOutstandingAmount == 0) {
            minOutstandingAmount = null;
        }
        if (maxOutstandingAmount != null && maxOutstandingAmount == 0) {
            maxOutstandingAmount = null;
        }

        if (invoiceTypes == null) {
            invoiceTypes = new ArrayList<>();
            invoiceTypes.add(InvoiceType.RENT.name());
            invoiceTypes.add(InvoiceType.ADVANCE.name());
            invoiceTypes.add(InvoiceType.REASSIGN_RENT.name());
            invoiceTypes.add(InvoiceType.BOOKING.name());
            invoiceTypes.add(InvoiceType.ADDITIONAL_ADVANCE.name());
            invoiceTypes.add(InvoiceType.EB.name());
            invoiceTypes.add(InvoiceType.OTHER.name());
        }

        ReportDetailsResponse.FilterOptions options = buildFilterOptions(hostelId);

        List<InvoicesV1> invoices = invoiceV1Service.getInvoicesForReport(hostelId, startDate, endDate, search,
                pStatus, invoiceModes, invoiceTypes, createdBy, minPaidAmount, maxPaidAmount,
                minOutstandingAmount, maxOutstandingAmount);
        if (authentication.getSource().equalsIgnoreCase("web")) {
            Pageable pageableRequest = PageRequest.of(pageParams - 1, size);

            Page<InvoicesV1> pagedInvoices = invoiceV1Service.getInvoicesForReport(hostelId, startDate, endDate, search,
                    pStatus, invoiceModes, invoiceTypes, createdBy, minPaidAmount, maxPaidAmount,
                    minOutstandingAmount, maxOutstandingAmount, pageableRequest);
            return getInvoiceWebReport(invoices, pagedInvoices, options, startDate, endDate);
        }
        else {

            List<ReportDetailsResponse.InvoiceDetail> invoiceDetails = mapToInvoiceDetails(invoices);

            return buildReportResponse(hostelId, startDate, endDate, search, pStatus, invoiceModes,
                    invoiceTypes,
                    createdBy, minPaidAmount, maxPaidAmount, minOutstandingAmount, maxOutstandingAmount,
                    invoiceDetails, options, pageParams-1, size);
        }


    }

    private ResponseEntity<?> getInvoiceWebReport(List<InvoicesV1> invoices, Page<InvoicesV1> pagedInvoices,
                                                  ReportDetailsResponse.FilterOptions filterOptions,
                                                  Date startDate, Date endDate) {
        if (pagedInvoices != null) {
            List<ReportDetailsResponse.InvoiceDetail> invoiceDetails = mapToInvoiceDetails(pagedInvoices.getContent());
            double totalAmount = invoices
                    .stream()
                    .mapToDouble(i -> {
                        if (i.getTotalAmount() == null) {
                            return 0.0;
                        }
                        if (i.getTotalAmount() < 0) {
                            return i.getTotalAmount() * -1;
                        }
                        return i.getTotalAmount();
                    })
                    .sum();

            int paidInvoiceCount = (int) invoices
                    .stream()
                    .filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name()))
                    .count();

            double paidAmount = invoices
                    .stream()
                    .filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name()))
                    .mapToDouble(i -> {
                        if (i.getPaidAmount() == null) {
                            return 0.0;
                        }
                        return i.getPaidAmount();
                    })
                    .sum();

            int cancelledInvoiceCount = (int) invoices
                    .stream()
                    .filter(InvoicesV1::isCancelled)
                    .count();
            int returnedInvoiceCount = (int) invoices
                    .stream()
                    .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()) && i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name()))
                    .count();

            double returnInvoiceAmount = invoices
                    .stream()
                    .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()) && i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name()))
                    .mapToDouble(i -> {
                        if (i.getPaidAmount() != null) {
                            return i.getPaidAmount();
                        }
                        return i.getTotalAmount();
                    })
                    .sum();

            double cancelledAmount = invoices
                    .stream()
                    .filter(InvoicesV1::isCancelled)
                    .mapToDouble(i -> {
                        if (i.getPaidAmount() == null) {
                            return i.getTotalAmount();
                        }
                        return i.getTotalAmount() - i.getPaidAmount();
                    })
                    .sum();

            double outstandingAmount = invoices
                    .stream()
                    .filter(i -> !i.isCancelled())
                    .mapToDouble(i -> {
                        if (i.getPaidAmount() == null) {
                            return i.getTotalAmount();
                        }
                        return i.getTotalAmount() - i.getPaidAmount();
                    })
                    .sum();


//                if (aggregates != null) {
//                        if (aggregates.getCount() != null)
//                                totalInvoices = aggregates.getCount().intValue();
//                        if (aggregates.getTotalAmount() != null)
//                                totalAmount = aggregates.getTotalAmount();
//                        if (aggregates.getPaidAmount() != null)
//                                paidAmount = aggregates.getPaidAmount();
//                }
//                Double refundAmount = 0.0;
//                if (aggregates != null && aggregates.getRefundAmount() != null) {
//                        refundAmount = aggregates.getRefundAmount();
//                }
//                outstandingAmount = totalAmount - paidAmount;
            int totalPages = pagedInvoices.getTotalPages();
            int currentPage = pagedInvoices.getPageable().getPageNumber() + 1;
            int totalInvoice = (int) pagedInvoices.getTotalElements();
            int itemsPerPage = pagedInvoices.getSize();
            double outstandingInvoiceCount = totalInvoice - paidInvoiceCount;

            long totalBookingInvoices = invoices.stream()
                    .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()))
                    .count();

            double totalBookingAmount = invoices.stream()
                    .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()))
                    .mapToDouble(i -> {
                        if (i.getTotalAmount() == null) return 0.0;
                        if (i.getTotalAmount() < 0) return i.getTotalAmount() * -1;
                        return i.getTotalAmount();
                    })
                    .sum();

            ReportDetailsResponse response = new ReportDetailsResponse()
                    .builder()
                    .totalInvoices(totalInvoice)
                    .startDate(Utils.dateToString(startDate))
                    .endDate(Utils.dateToString(endDate))
                    .currentPage(currentPage)
                    .totalPages(totalPages)
                    .itemsPerPage(itemsPerPage)
                    .totalAmount(Utils.roundOffWithTwoDigit(totalAmount))
                    .refundAmount(Utils.roundOffWithTwoDigit(returnInvoiceAmount))
                    .outStandingAmount(Utils.roundOffWithTwoDigit(outstandingAmount))
                    .paidAmount(Utils.roundOffWithTwoDigit(paidAmount))
                    .cancelledAmount(Utils.roundOffWithTwoDigit(cancelledAmount))
                    .refundedAmount(Utils.roundOffWithTwoDigit(returnInvoiceAmount))
                    .paidInvoiceCount(paidInvoiceCount)
                    .cancelledInvoiceCount(cancelledInvoiceCount)
                    .outStandingAmount(Utils.roundOffWithTwoDigit(outstandingAmount))
                    .returnedInvoiceCount(returnedInvoiceCount)
                    .totalBookingInvoices((int) totalBookingInvoices)
                    .totalBookingAmount(Utils.roundOffWithTwoDigit(totalBookingAmount))
                    .filterOptions(filterOptions)
                    .invoiceList(invoiceDetails)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);


        }
        return new ResponseEntity<>(Utils.INVOICE_NOT_FOUND, HttpStatus.BAD_REQUEST);
    }

    private BillingDates calculateDateRange(String period, String hostelId) {
        if (period == null || period.isEmpty()) {
            return hostelService.getCurrentBillStartAndEndDates(hostelId);
        }

        Calendar cal = Calendar.getInstance();
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
        if (period.equalsIgnoreCase("this month") || period.equalsIgnoreCase("this_month")) {
            return billingDates;
        } else if (period.equalsIgnoreCase("last month") || period.equalsIgnoreCase("last_month")) {

            cal.setTime(billingDates.currentBillStartDate());
            cal.add(Calendar.MONTH, -1);

            return hostelService.getBillingRuleOnDate(hostelId, cal.getTime());
        } else if (period.equalsIgnoreCase("last 3 months") || period.equalsIgnoreCase("last_3_months")) {
            cal.setTime(billingDates.currentBillStartDate());
            cal.add(Calendar.MONTH, -3);
            BillingDates bDates = hostelService.getBillingRuleOnDate(hostelId, cal.getTime());
            return new BillingDates(bDates.currentBillStartDate(),
                    billingDates.currentBillEndDate(),
                    bDates.dueDate(),
                    bDates.dueDays(),
                    bDates.hasGracePeriod(),
                    bDates.gracePeriodDays(),
                    bDates.typeOfBilling(),
                    billingDates.billingModel());
        } else if (period.equalsIgnoreCase("last 6 months") || period.equalsIgnoreCase("last_6_months")) {
            cal.setTime(billingDates.currentBillStartDate());
            cal.add(Calendar.MONTH, -6);
            BillingDates bDates = hostelService.getBillingRuleOnDate(hostelId, cal.getTime());
            return new BillingDates(bDates.currentBillStartDate(),
                    billingDates.currentBillEndDate(),
                    bDates.dueDate(),
                    bDates.dueDays(),
                    bDates.hasGracePeriod(),
                    bDates.gracePeriodDays(),
                    bDates.typeOfBilling(),
                    bDates.billingModel());
        }
        return billingDates;
    }

    private List<ReportDetailsResponse.InvoiceDetail> mapToInvoiceDetails(List<InvoicesV1> invoices) {
        List<ReportDetailsResponse.InvoiceDetail> details = invoices.stream()
                .map(this::convertToInvoiceDetail)
                .collect(Collectors.toList());

        populateCustomerDetails(details, invoices);
        return details;
    }

    private ReportDetailsResponse.InvoiceDetail convertToInvoiceDetail(InvoicesV1 inv) {
        String paymentStatus = null;
        if (inv.isCancelled()) {
            paymentStatus = "Cancelled";
        } else {
            paymentStatus = inv.getPaymentStatus();
        }
        return ReportDetailsResponse.InvoiceDetail.builder().invoiceId(inv.getInvoiceId())
                .invoiceNumber(inv.getInvoiceNumber()).customerId(inv.getCustomerId())
                .invoiceAmount(inv.getTotalAmount()).baseAmount(inv.getBasePrice())
                .paidAmount(inv.getPaidAmount())
                .dueAmount(inv.getTotalAmount()
                        - (inv.getPaidAmount() != null ? inv.getPaidAmount() : 0.0))
                .cgst(inv.getCgst()).sgst(inv.getSgst()).gst(inv.getGst())
                .createdAt(Utils.dateToString(inv.getCreatedAt())).createdBy(inv.getCreatedBy())
                .hostelId(inv.getHostelId()).invoiceDate(Utils.dateToString(inv.getInvoiceStartDate()))
                .dueDate(Utils.dateToString(inv.getInvoiceDueDate()))
                .invoiceType(Utils.capitalize(inv.getInvoiceType()))
                .invoiceMode(Utils.capitalize(inv.getInvoiceMode()))
                .paymentStatus(Utils.capitalize(paymentStatus))
                .updatedAt(Utils.dateToString(inv.getUpdatedAt())).isCancelled(inv.isCancelled())
                .build();
    }

    private void populateCustomerDetails(List<ReportDetailsResponse.InvoiceDetail> details,
                                         List<InvoicesV1> invoices) {
        List<String> customerIds = invoices.stream().map(InvoicesV1::getCustomerId).distinct()
                .collect(Collectors.toList());

        if (!customerIds.isEmpty()) {
            List<Customers> customerList = customersService.getCustomerDetails(customerIds);
            Map<String, Customers> customerMap = customerList.stream()
                    .collect(Collectors.toMap(Customers::getCustomerId, c -> c));

            details.forEach(detail -> {
                Customers c = customerMap.get(detail.getCustomerId());
                if (c != null) {
                    detail.setFirstName(Utils.capitalize(c.getFirstName()));
                    detail.setLastName(Utils.capitalize(c.getLastName()));
                    String full = (c.getFirstName() != null ? c.getFirstName() : "") + " "
                            + (c.getLastName() != null ? c.getLastName() : "");
                    detail.setFullName(Utils.capitalize(full.trim()));
                    detail.setInitials(getInitials(c.getFirstName(), c.getLastName()));
                    detail.setProfilePic(CustomerUtils.getProfilePic(c));
                }
            });
        }
    }

    private String getInitials(String firstName, String lastName) {
        StringBuilder initials = new StringBuilder();
        if (firstName != null) {
            initials.append(firstName.toUpperCase().charAt(0));
        }
        if (lastName != null && !lastName.trim().equalsIgnoreCase("")) {

            initials.append(lastName.toUpperCase().charAt(0));
        } else if (firstName != null) {
            if (firstName.length() > 1) {
                initials.append(firstName.toUpperCase().charAt(1));
            }
        }
        return initials.toString();
    }

    private ReportDetailsResponse.FilterOptions buildFilterOptions(String hostelId) {
        List<Object[]> creators = invoiceV1Service.getDistinctCreators(hostelId);
        List<ReportDetailsResponse.UserFilterItem> createdByOptions = new ArrayList<>();
        if (creators != null) {
            for (Object[] row : creators) {
                String uId = (String) row[0];
                String fName = (String) row[1];
                String lName = (String) row[2];
                String fullName = (fName != null ? fName : "") + " " + (lName != null ? lName : "");
                createdByOptions.add(ReportDetailsResponse.UserFilterItem.builder().userId(uId)
                        .name(Utils.capitalize(fullName.trim())).build());
            }
        }

        List<String> periods = Arrays.asList("This Month", "Last Month", "Last 3 Months", "Last 6 Months");

        return ReportDetailsResponse.FilterOptions.builder()
                .paymentStatus(toFilterItems(PaymentStatus.values()))
                .invoiceModes(toFilterItems(InvoiceMode.values()))
                .invoiceTypes(toInvoiceTypeFilterItems(InvoiceType.values()))
                .createdBy(createdByOptions).periods(periods).build();
    }

    private <E extends Enum<E>> List<ReportDetailsResponse.FilterItem> toFilterItems(E[] values) {
        return Arrays.stream(values)
                .filter(i -> !i.equals(PaymentStatus.ADVANCE_IN_HAND) && !i.equals(PaymentStatus.PARTIAL_REFUND)
                        && !i.equals(PaymentStatus.REFUNDED))
                .map(e -> new ReportDetailsResponse.FilterItem(Utils.capitalize(e.name()), e.name()))
                .collect(Collectors.toList());
    }

    private <E extends Enum<E>> List<ReportDetailsResponse.FilterItem> toInvoiceTypeFilterItems(E[] values) {
        return Arrays.stream(values)
                .filter(i -> !i.equals(InvoiceType.SETTLEMENT)
                        && !i.equals(InvoiceType.AMOUNT_HOLDING)
                        && !i.equals(InvoiceType.EB_HOLDING))
                .map(e -> new ReportDetailsResponse.FilterItem(Utils.capitalize(e.name()), e.name()))
                .collect(Collectors.toList());
    }

    private ResponseEntity<?> buildReportResponse(String hostelId, Date startDate, Date endDate, String search,
                                                  List<String> paymentStatus, List<String> invoiceModes, List<String> invoiceTypes,
                                                  List<String> createdBy,
                                                  Double minPaidAmount, Double maxPaidAmount, Double minOutstandingAmount,
                                                  Double maxOutstandingAmount, List<ReportDetailsResponse.InvoiceDetail> invoiceDetails,
                                                  ReportDetailsResponse.FilterOptions options, int page, int size) {

        List<InvoicesV1> invoices = invoiceV1Service.getInvoicesForReport(hostelId, startDate, endDate, search,
                paymentStatus, invoiceModes, invoiceTypes, createdBy, minPaidAmount, maxPaidAmount,
                minOutstandingAmount, maxOutstandingAmount);


        int totalInvoices = 0;
        Double totalAmount = 0.0;
        Double paidAmount = 0.0;
        Double outstandingAmount = 0.0;
        Double cancelledAmount = 0.0;
        Double returnInvoiceAmount = 0.0;

        int cancelledInvoiceCount = 0;
        int paidInvoiceCount = 0;
        int outstandingInvoiceCount = 0;
        int returnedInvoiceCount = 0;
        double profit = 0.0;

        totalInvoices = invoices.size();

        totalAmount = invoices
                .stream()
                .mapToDouble(i -> {
                    if (i.getTotalAmount() == null) {
                        return 0.0;
                    }
                    if (i.getTotalAmount() < 0) {
                        return i.getTotalAmount() * -1;
                    }
                    return i.getTotalAmount();
                })
                .sum();

        paidInvoiceCount = (int) invoices
                .stream()
                .filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name()))
                .count();

        paidAmount = invoices
                .stream()
                .filter(i -> i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name()) || i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PARTIAL_PAYMENT.name()))
                .mapToDouble(i -> {
                    if (i.getPaidAmount() == null) {
                        return i.getTotalAmount();
                    }
                    return i.getPaidAmount();
                })
                .sum();

        cancelledInvoiceCount = (int) invoices
                .stream()
                .filter(InvoicesV1::isCancelled)
                .count();
        returnedInvoiceCount = (int) invoices
                .stream()
                .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()) && i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name()))
                .count();

        returnInvoiceAmount = invoices
                .stream()
                .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()) && i.getPaymentStatus().equalsIgnoreCase(PaymentStatus.CANCELLED.name()))
                .mapToDouble(i -> {
                    if (i.getPaidAmount() != null) {
                        return i.getPaidAmount();
                    }
                    return i.getTotalAmount();
                })
                .sum();

        cancelledAmount = invoices
                .stream()
                .filter(InvoicesV1::isCancelled)
                .mapToDouble(i -> {
                    if (i.getPaidAmount() == null) {
                        return i.getTotalAmount();
                    }
                    return i.getTotalAmount() - i.getPaidAmount();
                })
                .sum();

        outstandingAmount = invoices
                .stream()
                .filter(i -> !i.isCancelled())
                .mapToDouble(i -> {
                    if (i.getPaidAmount() == null) {
                        return i.getTotalAmount();
                    }
                    return i.getTotalAmount() - i.getPaidAmount();
                })
                .sum();


//                if (aggregates != null) {
//                        if (aggregates.getCount() != null)
//                                totalInvoices = aggregates.getCount().intValue();
//                        if (aggregates.getTotalAmount() != null)
//                                totalAmount = aggregates.getTotalAmount();
//                        if (aggregates.getPaidAmount() != null)
//                                paidAmount = aggregates.getPaidAmount();
//                }
//                Double refundAmount = 0.0;
//                if (aggregates != null && aggregates.getRefundAmount() != null) {
//                        refundAmount = aggregates.getRefundAmount();
//                }
//                outstandingAmount = totalAmount - paidAmount;
        outstandingInvoiceCount = totalInvoices - paidInvoiceCount;

        long totalBookingInvoicesLong = invoices.stream()
                .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()))
                .count();

        double totalBookingAmount = invoices.stream()
                .filter(i -> i.getInvoiceType().equalsIgnoreCase(InvoiceType.BOOKING.name()))
                .mapToDouble(i -> {
                    if (i.getTotalAmount() == null) return 0.0;
                    if (i.getTotalAmount() < 0) return i.getTotalAmount() * -1;
                    return i.getTotalAmount();
                })
                .sum();

        int totalPages = (int) Math.ceil((double) totalInvoices / size);

        ReportDetailsResponse response = ReportDetailsResponse.builder().totalInvoices(totalInvoices)
                .itemsPerPage(0)
                .currentPage(page)
                .totalPages(totalPages)
                .totalAmount(Utils.roundOffWithTwoDigit(totalAmount))
                .refundAmount(Utils.roundOffWithTwoDigit(returnInvoiceAmount))
                .profit(profit)
                .outStandingAmount(Utils.roundOffWithTwoDigit(outstandingAmount))
                .cancelledAmount(Utils.roundOffWithTwoDigit(cancelledAmount))
                .paidAmount(Utils.roundOffWithTwoDigit(paidAmount))
                .paidInvoiceCount(paidInvoiceCount)
                .cancelledInvoiceCount(cancelledInvoiceCount)
                .outstandingInvoiceCount(outstandingInvoiceCount)
                .returnedInvoiceCount(returnedInvoiceCount)
                .totalBookingInvoices((int) totalBookingInvoicesLong)
                .totalBookingAmount(Utils.roundOffWithTwoDigit(totalBookingAmount))
                .startDate(Utils.dateToString(startDate))
                .endDate(Utils.dateToString(endDate))
                .filterOptions(options).invoiceList(invoiceDetails).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getReceiptDetails(String hostelId, String period, String customStartDate,
                                               String customEndDate, List<String> invoiceType, List<String> paymentMode,
                                               List<String> collectedBy,
                                               int page, int size) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());

        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

        TransactionReportResponse response = transactionService.getTransactionReport(hostelId, period,
                customStartDate,
                customEndDate, invoiceType, paymentMode, collectedBy, page, size);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getExpenseDetails(String hostelId, String period, String customStartDate,
                                               String customEndDate,
                                               List<Long> categoryIds,
                                               List<Long> subCategoryIds,
                                               List<String> paymentModes,
                                               List<String> paymentStatus,
                                               List<Integer> paidTo, List<String> createdBy, int pageParams, int size) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(expenseService.getExpenseReportDetails(hostelId, period, customStartDate,
                customEndDate, categoryIds, subCategoryIds,
                paymentModes, paymentStatus, paidTo, createdBy, pageParams,
                size),
                HttpStatus.OK);
    }

    public ResponseEntity<?> getTenantRegister(String hostelId, String search, List<String> status,
                                               List<Integer> rooms,
                                               List<Integer> floors, String period, String customStartDate, String customEndDate, int page,
                                               int size, List<String> sharingType) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (sharingType != null) {
            List<Integer> shareTypes = sharingType
                    .stream()
                    .map(Integer::parseInt)
                    .toList();
            if (rooms == null) {
                rooms = roomsService.findByHostelIdAndShareType(hostelId, shareTypes)
                        .stream()
                        .map(Rooms::getRoomId)
                        .toList();
            }
        }

        Date startDate;
        Date endDate;

        if (period != null && !period.isEmpty()) {
            BillingDates dates = calculateDateRange(period, hostelId);
            startDate = dates.currentBillStartDate();
            endDate = dates.currentBillEndDate();
        } else if (customStartDate != null || customEndDate != null) {
            if (customStartDate != null && !customStartDate.isEmpty()) {
                startDate = Utils.stringToDate(customStartDate, Utils.USER_INPUT_DATE_FORMAT);
            } else {
                startDate = Utils.stringToDate("01/01/2000", Utils.USER_INPUT_DATE_FORMAT);
            }
            if (customEndDate != null && !customEndDate.isEmpty()) {
                endDate = Utils.stringToDate(customEndDate, Utils.USER_INPUT_DATE_FORMAT);
            } else {
                endDate = new Date();
            }
        } else {
            BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
            startDate = billingDates.currentBillStartDate();
            endDate = billingDates.currentBillEndDate();
        }

        List<String> customerIds = null;
        if (search != null && !search.isEmpty()) {
            customerIds = customersService.findCustomerIdsByName(hostelId, search);
            if (customerIds.isEmpty()) {
                return buildEmptyTenantResponse(startDate, endDate, page-1, size, hostelId,
                        user.getParentId());
            }
        }

        List<BookingsV1> allBookings = bookingsService.findAllBookingsWithFilters(hostelId, startDate, endDate,
                customerIds, status, rooms, floors);

        Set<String> uniqueTenants = new HashSet<>();
        int activeCount = 0;
        double activeAmount = 0;
        int noticeCount = 0;
        double noticeAmount = 0;
        int checkoutCount = 0;
        double checkoutAmount = 0;
        int inactiveCount = 0;
        double inactiveAmount = 0;
        int bookedCount = 0;

        for (BookingsV1 b : allBookings) {
            uniqueTenants.add(b.getCustomerId());
            String bookingStatus = b.getCurrentStatus();
            double amount = (b.getRentAmount() != null ? b.getRentAmount() : 0);

            if (BookingStatus.CHECKIN.name().equalsIgnoreCase(bookingStatus)) {
                activeCount++;
                activeAmount += amount;
            } else if (BookingStatus.NOTICE.name().equalsIgnoreCase(bookingStatus)) {
                noticeCount++;
                noticeAmount += amount;
            } else if (BookingStatus.VACATED.name().equalsIgnoreCase(bookingStatus)
                    || BookingStatus.TERMINATED.name().equalsIgnoreCase(bookingStatus)) {
                checkoutCount++;
                checkoutAmount += amount;
            } else if (BookingStatus.CANCELLED.name().equalsIgnoreCase(bookingStatus)) {
                inactiveCount++;
                inactiveAmount += amount;
            } else if (BookingStatus.BOOKED.name().equalsIgnoreCase(bookingStatus)) {
                bookedCount++;

            }

        }

        TenantRegisterResponse.Summary summary = TenantRegisterResponse.Summary.builder()
                .totalTenants(uniqueTenants.size())
                .activeTenants(new TenantRegisterResponse.SegmentSummary(activeCount, 0))
                .noticePeriod(new TenantRegisterResponse.SegmentSummary(noticeCount, 0))
                .checkoutMTD(new TenantRegisterResponse.SegmentSummary(checkoutCount,
                        0))
                .inactive(new TenantRegisterResponse.SegmentSummary(inactiveCount, 0))
                .booked(new TenantRegisterResponse.SegmentSummary(bookedCount, 0))
                .build();

        List<BookingsV1> bookingsMain = bookingsService.findBookingsWithFilters(hostelId, startDate, endDate, customerIds, status, rooms,
                floors);
        List<BookingsV1> bookingsSecondary = bookingsMain;

        int totalPages = 0;
        int totalItems = bookingsMain.size();
        int itemsPerPage = 0;
        int currentPage = 0;

        if (authentication.getSource().equalsIgnoreCase("web")) {
            Page<BookingsV1> bookingsPage = bookingsService
                    .findBookingsWithFilters(hostelId, startDate, endDate, customerIds, status, rooms,
                            floors, page-1, size);
            bookingsSecondary = bookingsPage.getContent();
            totalPages = bookingsPage.getTotalPages();
            itemsPerPage = bookingsPage.getPageable().getPageSize();
            currentPage = bookingsPage.getPageable().getPageNumber() + 1;
        }


        long totalRecords = bookingsSecondary.size();

        List<String> pageCustomerIds = bookingsSecondary.stream().map(BookingsV1::getCustomerId)
                .collect(Collectors.toList());
        Map<String, Customers> customerMap = customersService.getCustomerDetails(pageCustomerIds).stream()
                .collect(Collectors.toMap(Customers::getCustomerId, c -> c, (a, b1) -> b1));

        Map<Integer, Long> sharingMap = bedsService.countBedsByRoomForHostel(hostelId).stream()
                .collect(Collectors.toMap(RoomBedCount::getRoomId,
                        RoomBedCount::getBedCount, (a, b1) -> b1));

        List<TenantRegisterResponse.TenantDetail> details = new ArrayList<>();
        for (BookingsV1 b : bookingsSecondary) {
            Customers c = customerMap.get(b.getCustomerId());
            String name = (c != null)
                    ? (c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : ""))
                    : "N/A";
            String mobile = (c != null) ? c.getMobile() : "N/A";

            String sharing = "N/A";
            Long bedCount = sharingMap.get(b.getRoomId());
            if (bedCount != null) {
                if (bedCount == 1)
                    sharing = "Single Sharing";
                else if (bedCount == 2)
                    sharing = "Two Sharing";
                else if (bedCount == 3)
                    sharing = "Three Sharing";
                else
                    sharing = bedCount + " Sharing";
            }


            long days = 0;
            if (b.getJoiningDate() != null) {
                if (b.getCheckoutDate() != null) {
                    days = Utils.findNumberOfDays(b.getJoiningDate(), b.getCheckoutDate());
                } else {
                    days = Utils.findNumberOfDays(b.getJoiningDate(), new Date());
                }
            }

            String stayDuration = days + (days == 1 ? " day" : " days");

            details.add(TenantRegisterResponse.TenantDetail.builder().tenantId(b.getCustomerId())
                    .name(Utils.capitalize(name.trim())).mobileNo(mobile).sharing(sharing)
                    .checkInDate(Utils.dateToString(b.getJoiningDate()))
                    .checkOutDate(b.getCheckoutDate() != null
                            ? Utils.dateToString(b.getCheckoutDate())
                            : null)
                    .checkInAmount(b.getRentAmount() != null ? b.getRentAmount() : 0)
                    .checkOutAmount(b.getRentAmount() != null ? b.getRentAmount() : 0)
                    .stayDuration(stayDuration)
                    .initials(getInitials(c != null ? c.getFirstName() : null, c != null ? c.getLastName() : null))
                    .profilePic(c != null ? CustomerUtils.getProfilePic(c) : null)
                    .build());
        }

        TenantRegisterResponse.Filters filtersData = buildTenantFilters(hostelId, user.getParentId());

        TenantRegisterResponse response = TenantRegisterResponse.builder().status(true)
                .message("Tenant register fetched successfully")
                .currentPage(currentPage)
                .pageSize(itemsPerPage)
                .totalPages(totalPages)
                .totalItems(totalItems)
                .dateRange(TenantRegisterResponse.DateRange
                        .builder().from(Utils.dateToString(startDate))
                        .to(Utils.dateToString(endDate)).build())
                .summary(summary).tenants(details)
                .filters(filtersData).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<?> buildEmptyTenantResponse(Date startDate, Date endDate, int page, int size,
                                                       String hostelId, String parentId) {
        TenantRegisterResponse.Summary summary = TenantRegisterResponse.Summary.builder().totalTenants(0)
                .activeTenants(new TenantRegisterResponse.SegmentSummary(0, 0))
                .noticePeriod(new TenantRegisterResponse.SegmentSummary(0, 0))
                .checkoutMTD(new TenantRegisterResponse.SegmentSummary(0, 0))
                .inactive(new TenantRegisterResponse.SegmentSummary(0, 0))
                .booked(new TenantRegisterResponse.SegmentSummary(0, 0)).build();
        TenantRegisterResponse.Filters filtersData = buildTenantFilters(hostelId, parentId);
        TenantRegisterResponse response = TenantRegisterResponse.builder().status(true)
                .message("Tenant register fetched successfully")
                .currentPage(0)
                .pageSize(0)
                .totalItems(0)
                .dateRange(TenantRegisterResponse.DateRange.builder()
                        .from(Utils.dateToString(startDate))
                        .to(Utils.dateToString(endDate)).build())
                .summary(summary).tenants(new ArrayList<>())
                .filters(filtersData).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private TenantRegisterResponse.Filters buildTenantFilters(String hostelId, String parentId) {
        List<TenantRegisterResponse.FilterItem> statusList = Arrays.stream(BookingStatus.values())
                .map(s -> {
                    if (s.name().equalsIgnoreCase(BookingStatus.CANCELLED.name())) {
                        return new TenantRegisterResponse.FilterItem(s.name(), Utils.capitalize("Inactive"));
                    }
                    return new TenantRegisterResponse.FilterItem(s.name(), Utils.capitalize(s.name()));
                })
                .collect(Collectors.toList());
        List<Rooms> rooms = roomsService.getAllRoomsByHostelId(hostelId);
        List<TenantRegisterResponse.SharingTypeFilter> sharingType = new ArrayList<>();
        HashMap<Integer, List<Integer>> floors = new HashMap<>();

        rooms.forEach(item -> {
            if (floors.containsKey(item.getSharingType())) {
                List<Integer> existingFloorIds = floors.get(item.getSharingType());
                existingFloorIds.add(item.getFloorId());
                floors.put(item.getSharingType(), existingFloorIds);
            } else {
                List<Integer> existingFloorIds = new ArrayList<>();
                existingFloorIds.add(item.getFloorId());
                floors.put(item.getSharingType(), existingFloorIds);
            }
        });

        if (rooms != null) {
            sharingType = rooms
                    .stream()
                    .map(Rooms::getSharingType)
                    .filter(Objects::nonNull)
                    .filter(i -> i > 0)
                    .sorted()
                    .distinct()
                    .map(i -> {
                        List<TenantRegisterResponse.FilterItem> filterItems = floors.get(i)
                                .stream()
                                .distinct()
                                .sorted(Comparator.reverseOrder())
                                .map(i2 -> new TenantRegisterResponse.FilterItem(i2, i2 + ""))
                                .toList();
                        if (i == 1) {
                            return new TenantRegisterResponse.SharingTypeFilter(1, Utils.SHARING_TYPE_SINGLE, filterItems);
                        } else if (i == 2) {
                            return new TenantRegisterResponse.SharingTypeFilter(2, Utils.SHARING_TYPE_TWO, filterItems);
                        } else if (i == 3) {
                            return new TenantRegisterResponse.SharingTypeFilter(3, Utils.SHARING_TYPE_THREE, filterItems);
                        } else {
                            return new TenantRegisterResponse.SharingTypeFilter(i, i + " Sharing", filterItems);
                        }
                    })
                    .toList();


        }

        List<TenantRegisterResponse.FilterItem> periodList = new ArrayList<>();
        periodList.add(new TenantRegisterResponse.FilterItem("this_month", "This Month"));
        periodList.add(new TenantRegisterResponse.FilterItem("last_month", "Last Month"));
        periodList.add(new TenantRegisterResponse.FilterItem("last_3_month", "Last 3 Months"));
        periodList.add(new TenantRegisterResponse.FilterItem("last_6_months", "Last 6 Months"));

        List<TenantRegisterResponse.FilterItem> floorList = floorsService.getFloorByHostelID(hostelId, parentId)
                .stream()
                .map(f -> new TenantRegisterResponse.FilterItem(f.getFloorId(),
                        Utils.capitalize(f.getFloorName())))
                .collect(Collectors.toList());

        List<TenantRegisterResponse.RoomFilter> roomList = roomsService.getAllRoomsByHostelId(hostelId).stream()
                .map(r -> new TenantRegisterResponse.RoomFilter(r.getRoomId(),
                        Utils.capitalize(r.getRoomName()), r.getFloorId(), r.getSharingType()))
                .collect(Collectors.toList());

        return TenantRegisterResponse.Filters.builder().tenantStatus(statusList).period(periodList)
                .floor(floorList)
                .sharingType(sharingType)
                .room(roomList).build();
    }

    public ResponseEntity<?> getComplaintsReport(String hostelId,
                                                 ComplaintsReportFilterRequest request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        Date startDate;
        Date endDate;
        if (request.getPeriod() != null && !request.getPeriod().isEmpty()) {
            BillingDates dates = calculateDateRange(request.getPeriod(), hostelId);
            startDate = dates.currentBillStartDate();
            endDate = dates.currentBillEndDate();
        } else if (request.getStartDate() != null || request.getEndDate() != null) {
            startDate = request.getStartDate() != null && !request.getStartDate().isEmpty()
                    ? Utils.stringToDate(request.getStartDate(), Utils.USER_INPUT_DATE_FORMAT)
                    : Utils.stringToDate("01/01/2000", Utils.USER_INPUT_DATE_FORMAT);
            endDate = request.getEndDate() != null && !request.getEndDate().isEmpty()
                    ? Utils.stringToDate(request.getEndDate(), Utils.USER_INPUT_DATE_FORMAT)
                    : new Date();
        } else {
            BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
            startDate = billingDates.currentBillStartDate();
            endDate = billingDates.currentBillEndDate();
        }

        List<String> statusStrings = request.getStatus() != null
                ? request.getStatus().stream().map(Enum::name).collect(Collectors.toList())
                : null;

        Page<ComplaintsV1> complaintPage = complaintService.getFilteredComplaints(
                hostelId, startDate, endDate, statusStrings, request.getRaisedBy(),
                request.getComplaintTypeIds(),
                request.getPage(), request.getSize());

        List<ComplaintsV1> complaints = complaintPage.getContent();

        // Fetch Maps
        Set<String> customerIds = complaints.stream().map(ComplaintsV1::getCustomerId)
                .collect(Collectors.toSet());
        Set<Integer> complaintTypeIds = complaints.stream()
                .map(ComplaintsV1::getComplaintTypeId).collect(Collectors.toSet());
        Set<Integer> roomIds = complaints.stream().filter(c -> c.getRoomId() != null && c.getRoomId() != 0)
                .map(ComplaintsV1::getRoomId).collect(Collectors.toSet());
        Set<Integer> bedIds = complaints.stream().filter(c -> c.getBedId() != null && c.getBedId() != 0)
                .map(ComplaintsV1::getBedId).collect(Collectors.toSet());
        Set<Integer> floorIds = complaints.stream().filter(c -> c.getFloorId() != null && c.getFloorId() != 0)
                .map(ComplaintsV1::getFloorId).collect(Collectors.toSet());
        Set<String> assigneeIds = complaints.stream().filter(c -> c.getAssigneeId() != null)
                .map(ComplaintsV1::getAssigneeId).collect(Collectors.toSet());

        Map<String, Customers> customerMap = !customerIds.isEmpty()
                ? customersService.getCustomerDetails(new ArrayList<>(customerIds)).stream().collect(
                Collectors.toMap(Customers::getCustomerId, c -> c))
                : new HashMap<>();
        Map<Integer, ComplaintTypeV1> typeMap = !complaintTypeIds.isEmpty()
                ? complaintTypeService.getComplaintTypesById(new ArrayList<>(complaintTypeIds)).stream()
                .collect(
                        Collectors.toMap(ComplaintTypeV1::getComplaintTypeId,
                                t -> t))
                : new HashMap<>();
        Map<Integer, Rooms> roomMap = !roomIds.isEmpty()
                ? roomsService.getAllRoomsByHostelId(hostelId).stream()
                .filter(r -> roomIds.contains(r.getRoomId()))
                .collect(Collectors.toMap(Rooms::getRoomId, r -> r))
                : new HashMap<>();
        Map<Integer, BedDetails> bedMap = !bedIds.isEmpty()
                ? bedsService.getBedDetails(new ArrayList<>(bedIds)).stream()
                .collect(Collectors.toMap(BedDetails::getBedId, b -> b))
                : new HashMap<>();
        Map<Integer, Floors> floorMap = !floorIds.isEmpty()
                ? floorsService.getFloorByHostelID(hostelId, user.getParentId()).stream()
                .filter(f -> floorIds.contains(f.getFloorId()))
                .collect(Collectors.toMap(Floors::getFloorId, f -> f))
                : new HashMap<>();
        Map<String, Users> userMap = !assigneeIds.isEmpty()
                ? usersService.findByListOfUserIds(new ArrayList<>(assigneeIds)).stream().collect(
                Collectors.toMap(Users::getUserId, u -> u))
                : new HashMap<>();

        List<ComplaintsReportResponse.ComplaintDetail> details = complaints.stream()
                .map(c -> {
                    Customers cust = customerMap.get(c.getCustomerId());
                    ComplaintTypeV1 type = typeMap.get(c.getComplaintTypeId());
                    Rooms room = roomMap.get(c.getRoomId());
                    BedDetails bed = bedMap.get(c.getBedId());
                    Floors floor = floorMap.get(c.getFloorId());
                    Users assignee = c.getAssigneeId() != null ? userMap.get(c.getAssigneeId())
                            : null;

                    return ComplaintsReportResponse.ComplaintDetail.builder()
                            .complaintId(c.getComplaintId())
                            .complaintType(type != null ? type.getComplaintTypeName()
                                    : "Unknown")
                            .raisedBy(cust != null
                                    ? Utils.capitalize(getResultName(
                                    cust.getFirstName(),
                                    cust.getLastName()))
                                    : "Unknown")
                            .status(Utils.capitalize(c.getStatus()))
                            .assignedTo(assignee != null
                                    ? Utils.capitalize(getResultName(
                                    assignee.getFirstName(),
                                    assignee.getLastName()))
                                    : "Unassigned")
                            .complaintDate(Utils.dateToString(c.getComplaintDate()))
                            .roomName(room != null ? room.getRoomName() : null)
                            .bedName(bed != null ? bed.getBedName() : null)
                            .floorName(floor != null ? floor.getFloorName() : null)
                            .build();
                }).collect(Collectors.toList());

        Map<String, Long> summaryCounts = complaintService.getComplaintSummary(hostelId, startDate, endDate,
                statusStrings, request.getRaisedBy(), request.getComplaintTypeIds());

        ComplaintsReportResponse.Summary summary = ComplaintsReportResponse.Summary
                .builder()
                .total(summaryCounts.getOrDefault("total", 0L).intValue())
                .resolved(summaryCounts.getOrDefault("resolved", 0L).intValue())
                .inprogress(summaryCounts.getOrDefault("inprogress", 0L).intValue())
                .completed(summaryCounts.getOrDefault("completed", 0L).intValue())
                .build();

        // Filters
        List<ComplaintsReportResponse.LabelValue> periods = Arrays.asList(
                new ComplaintsReportResponse.LabelValue("This Month", "this_month"),
                new ComplaintsReportResponse.LabelValue("Last Month", "last_month"),
                new ComplaintsReportResponse.LabelValue("Last 6 Months",
                        "last_6_months"));

        List<ComplaintsReportResponse.LabelValue> statuses = Arrays
                .stream(ComplaintStatus.values())
                .map(s -> new ComplaintsReportResponse.LabelValue(
                        Utils.capitalize(s.name()), s.name()))
                .collect(Collectors.toList());

        List<ComplaintsReportResponse.LabelValue> categories = complaintTypeService
                .getComplaintTypesByHostelId(hostelId).stream()
                .map(ct -> new ComplaintsReportResponse.LabelValue(
                        ct.getComplaintTypeName(), ct.getComplaintTypeId()))
                .collect(Collectors.toList());

        List<String> distinctCustomerIds = complaintService.getDistinctCustomerIdsByHostelId(hostelId);
        List<ComplaintsReportResponse.UserFilter> raisedBy = new ArrayList<>();
        if (!distinctCustomerIds.isEmpty()) {
            raisedBy = customersService.getCustomerDetails(distinctCustomerIds).stream()
                    .map(c -> new ComplaintsReportResponse.UserFilter(
                            c.getCustomerId(),
                            Utils.capitalize(getResultName(c.getFirstName(),
                                    c.getLastName()))))
                    .collect(Collectors.toList());
        }

        ComplaintsReportResponse response = ComplaintsReportResponse
                .builder()
                .status(true)
                .message("Complaints report successfully")
                .dateRange(new ComplaintsReportResponse.DateRange(
                        Utils.dateToString(startDate), Utils.dateToString(endDate)))
                .summary(summary)
                .filters(ComplaintsReportResponse.FilterValues.builder()
                        .complaintCategories(categories)
                        .period(periods)
                        .status(statuses)
                        .raisedBy(raisedBy)
                        .build())
                .complaints(details)
                .pagination(ComplaintsReportResponse.Pagination.builder()
                        .currentPage(complaintPage.getNumber())
                        .pageSize(complaintPage.getSize())
                        .totalRecords(complaintPage.getTotalElements())
                        .totalPages(complaintPage.getTotalPages())
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }

    private String getResultName(String firstName, String lastName) {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public ResponseEntity<?> downloadCustomersReport(String hostelId,
                                                     String search,
                                                     List<String> status,
                                                     List<Integer> room,
                                                     List<Integer> floor,
                                                     String period,
                                                     List<String> sharingType,
                                                     String startDate, String endDate) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        String sDate = null;
        String eDate = null;
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);

        if (period != null && !period.isEmpty()) {
            BillingDates dates = calculateDateRange(period, hostelId);
            sDate = Utils.dateToString(dates.currentBillStartDate());
            eDate = Utils.dateToString(dates.currentBillEndDate());
        } else if (startDate != null || endDate != null) {
            if (startDate != null && !startDate.isEmpty()) {
                sDate = Utils.dateToString(Utils.stringToDate(startDate, Utils.USER_INPUT_DATE_FORMAT));
            } else {
                sDate = Utils.dateToString(Utils.stringToDate("01/01/2000", Utils.USER_INPUT_DATE_FORMAT));
            }
            if (endDate != null && !endDate.isEmpty()) {
                eDate = Utils.dateToString(Utils.stringToDate(endDate, Utils.USER_INPUT_DATE_FORMAT));
            } else {
                eDate = Utils.dateToString(new Date());
            }
        } else {
            sDate = Utils.dateToString(billingDates.currentBillStartDate());
            eDate = Utils.dateToString(billingDates.currentBillEndDate());
        }

        String url =  reportsUrl + "/v2/tenants/"+hostelId;
        //bearer token
        //authentication.getToken();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("search", search)
                .queryParam("status", status)
                .queryParam("room", room)
                .queryParam("floor", floor)
                .queryParam("sharingType", sharingType)
                .queryParam("startDate", sDate)
                .queryParam("endDate", eDate);


        String pdfUrl = downloadService.downloadFromUrl(builder.toUriString());

        if (pdfUrl == null) {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(pdfUrl, HttpStatus.OK);

    }

    public ResponseEntity<?> downloadReceiptsReport(String hostelId, List<String> invoiceTypes, List<String> paymentModes, List<String> collectedBy, String period, String startDate, String endDate) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        String sDate = null;
        String eDate = null;
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);

        if (startDate == null) {
            sDate = Utils.dateToString(billingDates.currentBillStartDate()).replace("/", "-");
        }
        else {
            Date d = Utils.stringToDate(startDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (d == null) {
                sDate = Utils.dateToString(billingDates.currentBillStartDate()).replace("/", "-");
            }
            else {
                sDate = Utils.dateToString(d).replace("/", "-");
            }
        }
        if (endDate == null) {
            eDate = Utils.dateToString(billingDates.currentBillEndDate()).replace("/", "-");
        }
        else {
            Date d = Utils.stringToDate(endDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (d == null) {
                eDate = Utils.dateToString(billingDates.currentBillEndDate()).replace("/", "-");
            }
            else {
                eDate = Utils.dateToString(d).replace("/", "-");
            }
        }

        String url =  reportsUrl + "/v2/reports/receipts/report/"+hostelId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("startDate", sDate)
                .queryParam("endDate", eDate)
                .queryParam("collectedBy", collectedBy)
                .queryParam("paymentMode", paymentModes)
                .queryParam("invoiceType", invoiceTypes);

        String pdfUrl = downloadService.downloadFromUrl(builder.toUriString());

        if (pdfUrl == null) {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(pdfUrl, HttpStatus.OK);

    }

    public ResponseEntity<?> downloadExpenseReport(String hostelId,
                                                   String startDate,
                                                   String endDate,
                                                   List<Long> categoryId,
                                                   List<Long> subCatId,
                                                   List<String> paymentMode,
                                                   List<String> paymentStatus,
                                                   List<String> paidTo,
                                                   List<String> createdBy) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        String sDate = null;
        String eDate = null;
        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);

        if (startDate == null) {
            sDate = Utils.dateToString(billingDates.currentBillStartDate()).replace("/", "-");
        }
        else {
            Date d = Utils.stringToDate(startDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (d == null) {
                sDate = Utils.dateToString(billingDates.currentBillStartDate()).replace("/", "-");
            }
            else {
                sDate = Utils.dateToString(d).replace("/", "-");
            }
        }
        if (endDate == null) {
            eDate = Utils.dateToString(billingDates.currentBillEndDate()).replace("/", "-");
        }
        else {
            Date d = Utils.stringToDate(endDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (d == null) {
                eDate = Utils.dateToString(billingDates.currentBillEndDate()).replace("/", "-");
            }
            else {
                eDate = Utils.dateToString(d).replace("/", "-");
            }
        }

        String url =  reportsUrl + "/v2/expense/"+hostelId;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("startDate", sDate)
                .queryParam("endDate", eDate)
                .queryParam("categoryId", categoryId)
                .queryParam("subCategoryId", subCatId)
                .queryParam("paymentMode", paymentMode)
                .queryParam("paymentStatus", paymentStatus)
                .queryParam("paidTo", paidTo)
                .queryParam("createdBy", createdBy);

        String pdfUrl = downloadService.downloadFromUrl(builder.toUriString());

        if (pdfUrl == null) {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(pdfUrl, HttpStatus.OK);
    }

    public ResponseEntity<?> downloadInvoiceReport(String hostelId, String search, List<String> paymentStatus,
                                                   List<String> invoiceModes, List<String> invoiceTypes, List<String> createdBy,
                                                   String period, Double minPaidAmount, Double maxPaidAmount,
                                                   Double minOutstandingAmount, Double maxOutstandingAmount,
                                                   String startDate, String endDate) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users user = usersService.findUserByUserId(authentication.getName());
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(user.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BillingDates dates = calculateDateRange(period, hostelId);
        Date sDateObj = dates.currentBillStartDate();
        Date eDateObj = dates.currentBillEndDate();

        if (startDate != null && !startDate.isEmpty()) {
            Date d = Utils.stringToDate(startDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (d != null) {
                sDateObj = d;
            }
        }
        if (endDate != null && !endDate.isEmpty()) {
            Date d = Utils.stringToDate(endDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            if (d != null) {
                eDateObj = d;
            }
        }

        String sDate = Utils.dateToString(sDateObj).replace("/", "-");
        String eDate = Utils.dateToString(eDateObj).replace("/", "-");

        if (invoiceTypes == null) {
            invoiceTypes = new ArrayList<>();
            invoiceTypes.add(InvoiceType.RENT.name());
            invoiceTypes.add(InvoiceType.ADVANCE.name());
            invoiceTypes.add(InvoiceType.REASSIGN_RENT.name());
            invoiceTypes.add(InvoiceType.BOOKING.name());
            invoiceTypes.add(InvoiceType.EB.name());
            invoiceTypes.add(InvoiceType.OTHER.name());
            invoiceTypes.add(InvoiceType.ADDITIONAL_ADVANCE.name());
        }

        String url =  reportsUrl + "/v2/reports/invoices/pdf/"+hostelId;
        System.out.println("Invoice Report URL: " + url);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("startDate", sDate)
                .queryParam("endDate", eDate);

        if (search != null && !search.isEmpty()) {
            builder.queryParam("search", search);
        }
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            builder.queryParam("paymentStatus", paymentStatus);
        }
        if (invoiceModes != null && !invoiceModes.isEmpty()) {
            builder.queryParam("invoiceModes", invoiceModes);
        }
        if (invoiceTypes != null && !invoiceTypes.isEmpty()) {
            builder.queryParam("invoiceTypes", invoiceTypes);
        }
        if (createdBy != null && !createdBy.isEmpty()) {
            builder.queryParam("createdBy", createdBy);
        }
        if (period != null && !period.isEmpty()) {
            builder.queryParam("period", period);
        }
        if (minPaidAmount != null) {
            builder.queryParam("minPaidAmount", minPaidAmount);
        }
        if (maxPaidAmount != null) {
            builder.queryParam("maxPaidAmount", maxPaidAmount);
        }
        if (minOutstandingAmount != null) {
            builder.queryParam("minOutstandingAmount", minOutstandingAmount);
        }
        if (maxOutstandingAmount != null) {
            builder.queryParam("maxOutstandingAmount", maxOutstandingAmount);
        }

        String pdfUrl = downloadService.downloadFromUrl(builder.toUriString());

        if (pdfUrl == null) {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(pdfUrl, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceReportConsolidatedView(String hostelId, InvConsolidatedViewPayload payload) {

        String loggedInUserId = authentication.getName();

        Users loggedInUser = usersService.findUserByUserId(loggedInUserId);
        if (loggedInUser == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!userHostelService.checkHostelAccess(loggedInUserId, hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(loggedInUser.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostel = hostelService.getHostelInfo(hostelId);
        if (hostel == null){
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        BillingRules billingRules = hostelConfigService.getCurrentBillingRule(hostelId);
        if (billingRules == null){
            return new ResponseEntity<>(Utils.BILLING_RULE_NOT_AVAILABLE, HttpStatus.BAD_REQUEST);
        }

        List<ColumnFilters> tableColumns = tableColumnService
                .getInvRegConsolidatedColumns(hostelId, loggedInUserId,
                        FilterOptionsModule.MODULE_INVOICE_REGISTER_CONSOLIDATED.name());

        List<ColumnFilters> activeColumns = tableColumns.stream()
                .filter(ColumnFilters::isSelected)
                .sorted(Comparator.comparingInt(ColumnFilters::getOrder))
                .toList();

        List<String> columnNames = activeColumns.stream()
                .map(ColumnFilters::getFieldName)
                .toList();

        List<ColumnFiltersRes> activeColumnsRes = activeColumns.stream()
                .map(i -> {

                    boolean isLocked = InvRegisterConsolidatedColumnUtils.FLOOR.equals(i.getFieldName()) ||
                            InvRegisterConsolidatedColumnUtils.ROOM.equals(i.getFieldName()) ||
                            InvRegisterConsolidatedColumnUtils.BED.equals(i.getFieldName()) ||
                            InvRegisterConsolidatedColumnUtils.TOTAL_PENDING_AMOUNT.equals(i.getFieldName());

                    return new ColumnFiltersRes(i.getOrder(), i.getFieldName(),
                            i.isSelected(), isLocked);
                }).toList();

        Date today = new Date();

        Boolean isPaginated = payload.isPaginated();
        int page = payload.page();
        int size = payload.size();
        String search = payload.search();
        List<String> paymentStatus = payload.paymentStatus();
        List<String> invoiceModes = payload.invoiceModes();
        List<String> invoiceTypes = payload.invoiceTypes();
        List<String> createdBy = payload.createdBy();
        String period = payload.period();
        Double minPaidAmount = payload.minPaidAmount();
        Double maxPaidAmount = payload.maxPaidAmount();
        Double minOutstandingAmount = payload.minOutstandingAmount();
        Double maxOutstandingAmount = payload.maxOutstandingAmount();
        String startDate = payload.startDate();
        String endDate = payload.endDate();
        List<Integer> floorIds = payload.floorIds();
        Boolean includePaidInvoice = payload.includePaidInvoice();
        Boolean includeEmptyBeds = payload.includeEmptyBeds();
        String groupBy = payload.groupBy();
        Boolean includeSubTotals = payload.includeSubTotals();
        Boolean includeGrandTotal = payload.includeGrandTotal();
        Boolean includeHostelName = payload.includeHostelName();

        if (isPaginated == null){
            isPaginated = false;
        }

        if (isPaginated) {
            page = Math.max(page - 1, 0);
            size = Math.max(size, 1);
        }

        search = (search == null || search.isBlank()) ? null : search.trim();

        Set<String> filteredCustomerIds = null;
        if (search != null){
            List<Customers> customers = customersService
                    .searchCustomerByFullName(hostelId, search);

            filteredCustomerIds = customers.stream()
                    .map(Customers::getCustomerId)
                    .collect(Collectors.toSet());
        }

        if (paymentStatus == null || paymentStatus.isEmpty()){
            paymentStatus = null;
        }

        if (invoiceModes == null || invoiceModes.isEmpty()){
            invoiceModes = null;
        }

        if (invoiceTypes == null || invoiceTypes.isEmpty()){
            invoiceTypes = null;
        }

        if (createdBy == null || createdBy.isEmpty()){
            createdBy = null;
        }

        Date filterStartDate = null;
        Date filterEndDate = null;
        if (period != null && !period.isBlank()){
            BillingDates billingDates = hostelConfigService
                    .getBillingDatesByPeriod(period, billingRules);

            filterStartDate = billingDates.currentBillStartDate();
            filterEndDate = billingDates.currentBillEndDate();
        }

        if (startDate != null && endDate != null){
            filterStartDate = Utils.stringToDate(startDate
                    .replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            filterEndDate = Utils.stringToDate(endDate
                    .replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }

        List<Floors> allFloors = floorsService.getAllByHostelId(hostelId);
        Map<Integer, Floors> allFloorsMap = allFloors.stream()
                .collect(Collectors.toMap(Floors::getFloorId,
                        Function.identity()));

        List<Floors> floors = new ArrayList<>();
        List<Rooms> rooms = new ArrayList<>();
        List<Beds> beds = new ArrayList<>();
        if (floorIds == null || floorIds.isEmpty()){
            floors = allFloors;
        } else {
            floors = floorsService.getByFloorIds(new HashSet<>(floorIds));
        }

        if (floors != null && !floors.isEmpty()){
            Set<Integer> newFloorIds = floors.stream()
                    .map(Floors::getFloorId)
                    .collect(Collectors.toSet());

            rooms = roomsService.getAllByFloorIds(newFloorIds);
        }

        if (rooms != null && !rooms.isEmpty()){
            Set<Integer> roomIds = rooms.stream()
                    .map(Rooms::getRoomId)
                    .collect(Collectors.toSet());

            beds = bedsService.getAllByRoomIds(roomIds);
        }

        if (includeEmptyBeds == null){
            includeEmptyBeds = false;
        }

        List<BookingsV1> latestBookings = new ArrayList<>();
        if (beds != null && !beds.isEmpty()){

            Set<Integer> bedIds = beds.stream()
                    .map(Beds::getBedId)
                    .collect(Collectors.toSet());

            latestBookings = bookingsService
                    .getLatestBookingsByBedIds(bedIds);

            Set<String> bedCustomerIds = latestBookings.stream()
                    .map(BookingsV1::getCustomerId)
                    .collect(Collectors.toSet());

            if (!bedCustomerIds.isEmpty()){
                if (filteredCustomerIds != null){
                    filteredCustomerIds.retainAll(bedCustomerIds);
                } else {
                    filteredCustomerIds = bedCustomerIds;
                }
            }
        }

        if (includePaidInvoice == null){
            includePaidInvoice = false;
        }

        if (includePaidInvoice){
            if (paymentStatus != null){
                paymentStatus.add(PaymentStatus.PAID.name());
            }
        } else {
            if (paymentStatus != null){
                paymentStatus.remove(PaymentStatus.PAID.name());
            }
        }

        GroupByEnum groupByEnum = GroupByEnum.FLOOR_ROOM_BED;
        if (groupBy != null){
            try {
                groupByEnum = GroupByEnum.valueOf(groupBy);
            } catch (Exception e){
                return new ResponseEntity<>("Invalid Group by filter",
                        HttpStatus.BAD_REQUEST);
            }
        }

        if (includeSubTotals == null){
            includeSubTotals = true;
        }

        if (includeGrandTotal == null){
            includeGrandTotal = true;
        }

        if (includeHostelName == null){
            includeHostelName = true;
        }

        List<InvoicesV1> invoices = invoiceV1Service
                .findConsolidatedInvoicesByFilters(hostelId, filteredCustomerIds, paymentStatus,
                        invoiceModes, invoiceTypes, createdBy, filterStartDate, filterEndDate, minPaidAmount,
                        maxPaidAmount, minOutstandingAmount, maxOutstandingAmount);

        if (paymentStatus == null && !includePaidInvoice){
            invoices = invoices.stream()
                    .filter(i -> !PaymentStatus.PAID.name().equalsIgnoreCase(i.getPaymentStatus()))
                    .toList();
        }

        Map<Integer, List<Rooms>> roomsByFloor = new HashMap<>();
        if (rooms != null){
            roomsByFloor = rooms.stream()
                    .collect(Collectors.groupingBy(Rooms::getFloorId));
        }

        Map<Integer, List<Beds>> bedsByRoom = new HashMap<>();
        if (beds != null){
            bedsByRoom = beds.stream()
                    .collect(Collectors.groupingBy(Beds::getRoomId));
        }

        Map<Integer, String> customerByBed = latestBookings.stream()
                .collect(Collectors.toMap(
                        BookingsV1::getBedId,
                        BookingsV1::getCustomerId,
                        (existing, replacement) -> existing
                ));

        Map<String, List<InvoicesV1>> invoicesByCustomer = invoices.stream()
                .collect(Collectors.groupingBy(InvoicesV1::getCustomerId));

        Set<String> customerIds = invoices.stream()
                .map(InvoicesV1::getCustomerId)
                .collect(Collectors.toSet());

        List<Customers> customers = customersService
                .findByCustomerIds(new ArrayList<>(customerIds));

        Map<String, Customers> customersMap = customers.stream()
                .collect(Collectors.toMap(Customers::getCustomerId,
                        customer -> customer));

        Set<String> createdByUserIds = invoices.stream()
                .filter(i -> !InvoiceType.SETTLEMENT.name().equalsIgnoreCase(i.getInvoiceType()))
                .map(InvoicesV1::getCreatedBy)
                .collect(Collectors.toSet());

        List<Users> createdByUsers = usersService
                .findByUserIds(new ArrayList<>(createdByUserIds));

        String totalInvoice = String.valueOf(invoices.size());
        double totalInvoiceAmount = 0;
        double paidAmount = 0;
        double returnInvoiceAmount = 0;
        double cancelledAmount = 0;
        double outstandingAmount = 0;
        double totalBookingAmount = 0;

        for (InvoicesV1 invoice : invoices){

            double totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0.0;
            double invPaidAmount = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : 0.0;
            double pendingAmount = totalAmount - invPaidAmount;

            if (InvoiceType.BOOKING.name().equalsIgnoreCase(invoice.getInvoiceType())){
                totalBookingAmount += totalAmount;
            }
            if (!invoice.isCancelled()){
                outstandingAmount += pendingAmount;
            } else {
                cancelledAmount += pendingAmount;
            }
            if (InvoiceType.BOOKING.name().equalsIgnoreCase(invoice.getInvoiceType()) &&
                    PaymentStatus.CANCELLED.name().equalsIgnoreCase(invoice.getPaymentStatus())){
                if (invoice.getPaidAmount() != null){
                    returnInvoiceAmount += invoice.getPaidAmount();
                } else {
                    returnInvoiceAmount += totalAmount;
                }
            }
            if (PaymentStatus.PAID.name().equalsIgnoreCase(invoice.getPaymentStatus()) ||
                    PaymentStatus.PARTIAL_PAYMENT.name().equalsIgnoreCase(invoice.getPaymentStatus())){
                paidAmount += invPaidAmount;
            }
            totalInvoiceAmount += totalAmount;
        }

        HostelInfoRes hostelInfo = new HostelInfoRes(hostel.getHostelName(), AddressUtils.getHostelAddress(hostel),
                hostel.getMobile(), hostel.getCity(), hostel.getState(), hostel.getPincode(), hostel.getHouseNo(),
                hostel.getStreet());

        InvoiceHeader invHeader = new InvoiceHeader(filterStartDate != null ? Utils.dateToString(filterStartDate) : null,
                filterEndDate != null ? Utils.dateToString(filterEndDate) : null, totalInvoice, String.valueOf(totalInvoiceAmount),
                String.valueOf(paidAmount), String.valueOf(Utils.roundOffWithTwoDigit(totalBookingAmount)),
                String.valueOf(outstandingAmount), String.valueOf(returnInvoiceAmount), String.valueOf(cancelledAmount));

        FooterInfo footerInfo = new FooterInfo(Utils.dateToString(today), Utils.dateToTime(today));

        List<UserFilterRes> createdByFilters = createdByUsers.stream()
                .map(i -> new UserFilterRes(NameUtils
                        .getFullName(i.getFirstName(), i.getLastName()),
                        i.getUserId()))
                .toList();

        List<EnumFilterRes> periodFilters = Arrays.stream(PeriodEnum.values())
                .map(i -> new EnumFilterRes(i.name(), i.getLabel()))
                .toList();

        List<EnumFilterRes> paymentStatusFilters = Arrays.stream(PaymentStatus.values())
                .map(i -> new EnumFilterRes(i.name(), i.getDisplayName()))
                .toList();

        List<EnumFilterRes> invoiceModeFilters = Arrays.stream(InvoiceMode.values())
                .map(i -> new EnumFilterRes(i.name(),
                        NameUtils.capitalizeWordsWithUnderscore(i.name())))
                .toList();

        List<EnumFilterRes> invoiceTypeFilters = Arrays.stream(InvoiceType.values())
                .map(i -> new EnumFilterRes(i.name(),
                        NameUtils.capitalizeWordsWithUnderscore(i.name())))
                .toList();

        List<FloorsFilterRes> floorsFilters = allFloors.stream()
                .map(i -> new FloorsFilterRes(i.getFloorId(), i.getFloorName()))
                .toList();

        List<EnumFilterRes> groupByFilters = Arrays.stream(GroupByEnum.values())
                .map(i -> new EnumFilterRes(i.name(), i.getLabel()))
                .toList();

        InvConsolidatedFiltersRes filtersRes = new InvConsolidatedFiltersRes(createdByFilters, periodFilters,
                paymentStatusFilters, invoiceModeFilters, invoiceTypeFilters, floorsFilters, groupByFilters);

        PaginationDataRes paginationData = isPaginated ?
                new PaginationDataRes(page + 1, size, 0, 1)
                : null;

        List<Object> columnData = new ArrayList<>();

        InvConsolidatedPdfRes response = new InvConsolidatedPdfRes(includePaidInvoice, includeEmptyBeds,
                includeSubTotals, includeGrandTotal, includeHostelName, hostelInfo, invHeader, footerInfo,
                List.of(), List.of(), filtersRes, paginationData, activeColumnsRes, columnNames, columnData);

        if (GroupByEnum.FLOOR_ROOM_BED.equals(groupByEnum)) {

            List<InvFloorsRes> floorResponses = new ArrayList<>();

            if (floors == null){
                floors = new ArrayList<>();
            }

            for (Floors floor : floors) {

                List<InvRoomRes> roomResponses = new ArrayList<>();

                for (Rooms room : roomsByFloor.getOrDefault(floor.getFloorId(), List.of())) {

                    List<InvBedRes> bedResponses = buildBedResponses(bedsByRoom, customerByBed,
                            customersMap, invoicesByCustomer, room, includeEmptyBeds);

                    if (!includeEmptyBeds && bedResponses.isEmpty()) {
                        continue;
                    }

                    roomResponses.add(new InvRoomRes(room.getRoomId(), room.getRoomName(), floor.getFloorId(),
                            floor.getFloorName(), bedResponses));
                }

                if (!includeEmptyBeds && roomResponses.isEmpty()) {
                    continue;
                }

                floorResponses.add(new InvFloorsRes(floor.getFloorId(), floor.getFloorName(), roomResponses));
            }

            long totalElements = floorResponses.size();

            List<InvFloorsRes> paginatedFloors = paginateList(
                    floorResponses, isPaginated, page, size
            );

            paginationData = isPaginated ?
                    new PaginationDataRes(page + 1, size, totalElements,
                            (int) Math.ceil((double) totalElements / size))
                    : null;

            for (InvFloorsRes floor : paginatedFloors) {
                for (InvRoomRes room : floor.rooms()) {
                    for (InvBedRes bed : room.beds()) {
                        columnData.add(buildColumnData(bed, floor.floorName(),
                                room.roomName(), columnNames));
                    }
                }
            }

            response = new InvConsolidatedPdfRes(includePaidInvoice, includeEmptyBeds, includeSubTotals,
                    includeGrandTotal, includeHostelName, hostelInfo, invHeader, footerInfo, paginatedFloors,
                    List.of(), filtersRes, paginationData, activeColumnsRes, columnNames, columnData);
        }
        else if (GroupByEnum.ROOM_BED.equals(groupByEnum)) {

            List<InvRoomRes> roomResponses = new ArrayList<>();

            if (rooms == null){
                rooms = new ArrayList<>();
            }

            for (Rooms room : rooms) {

                List<InvBedRes> bedResponses = buildBedResponses(bedsByRoom, customerByBed,
                        customersMap, invoicesByCustomer, room, includeEmptyBeds);

                if (!includeEmptyBeds && bedResponses.isEmpty()) {
                    continue;
                }

                Floors floor = allFloorsMap.getOrDefault(room.getFloorId(), null);
                String floorName = null;
                if (floor != null) {
                    floorName = floor.getFloorName();
                }

                roomResponses.add(new InvRoomRes(room.getRoomId(), room.getRoomName(), room.getFloorId(),
                        floorName, bedResponses));
            }

            long totalElements = roomResponses.size();

            List<InvRoomRes> paginatedRooms = paginateList(
                    roomResponses, isPaginated, page, size
            );

            paginationData = isPaginated ?
                    new PaginationDataRes(page + 1, size, totalElements,
                            (int) Math.ceil((double) totalElements / size))
                    : null;

            for (InvRoomRes room : paginatedRooms) {
                for (InvBedRes bed : room.beds()) {
                    columnData.add(buildColumnData(bed, room.floorName(),
                            room.roomName(), columnNames));
                }
            }

            response = new InvConsolidatedPdfRes(includePaidInvoice, includeEmptyBeds, includeSubTotals,
                    includeGrandTotal, includeHostelName, hostelInfo, invHeader, footerInfo, List.of(),
                    paginatedRooms, filtersRes, paginationData, activeColumnsRes, columnNames, columnData);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private InvConsolidatedRes toInvConsolidatedRes(InvoicesV1 invoice, Customers customer) {

        if (invoice == null){
            return null;
        }

        String name = "NA";
        String invoiceType = "NA";
        String dueAmount = "NA";
        String paymentStatus = "NA";

        if (customer != null){
            name = NameUtils.getFullName(customer.getFirstName(), customer.getLastName());
        }

        if (InvoiceType.RENT.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Rent";
        } else if (InvoiceType.REASSIGN_RENT.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Re-Assign Rent";
        } else if (InvoiceType.BOOKING.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Booking";
        } else if (InvoiceType.ADVANCE.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Advance";
        } else if (InvoiceType.ADDITIONAL_ADVANCE.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Additional Advance";
        } else if (InvoiceType.SETTLEMENT.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Settlement";
        } else if (InvoiceType.EB.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "EB";
        } else if (InvoiceType.OTHER.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Other";
        } else if (InvoiceType.AMOUNT_HOLDING.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                InvoiceType.EB_HOLDING.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            invoiceType = "Retainer";
        } else {
            invoiceType = Utils.capitalize(invoice.getInvoiceType());
        }

        if (PaymentStatus.PAID.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Paid";
        } else if (PaymentStatus.PARTIAL_PAYMENT.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Partially Paid";
        } else if (PaymentStatus.REFUNDED.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Refunded";
        } else if (PaymentStatus.PARTIAL_REFUND.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Partially Refunded";
        } else if (PaymentStatus.PENDING.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Pending";
        } else if (PaymentStatus.PENDING_REFUND.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Pending Refunded";
        } else if (PaymentStatus.CANCELLED.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Cancelled";
        } else if (PaymentStatus.ADVANCE_IN_HAND.name().equalsIgnoreCase(invoice.getPaymentStatus())) {
            paymentStatus = "Advance In Hand";
        } else {
            paymentStatus = Utils.capitalize(invoice.getPaymentStatus());
        }

        if (invoice.isCancelled()) {
            paymentStatus = "Cancelled";
        }

        double totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0.0;
        double paidAmount = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : 0.0;
        double pendingAmount = totalAmount - paidAmount;

        if (!InvoiceType.SETTLEMENT.name().equalsIgnoreCase(invoice.getInvoiceType())) {
            dueAmount = String.valueOf(pendingAmount);
        }
        else {
            if (totalAmount < 0){
                if (invoice.getPaidAmount() != null){
                    double amount = totalAmount + paidAmount;
                    dueAmount = String.valueOf(amount);
                } else {
                    dueAmount = String.valueOf(totalAmount);
                }
            } else {
                dueAmount = String.valueOf(pendingAmount);
            }
        }

        return new InvConsolidatedRes(invoice.getInvoiceNumber(), name, invoiceType,
                Utils.dateToString(invoice.getInvoiceStartDate()), Utils.dateToString(invoice.getInvoiceDueDate()),
                String.valueOf(invoice.getTotalAmount()), String.valueOf(paidAmount),
                dueAmount, paymentStatus, invoice.getInvoiceUrl()
        );
    }

    private List<InvBedRes> buildBedResponses(Map<Integer, List<Beds>> bedsByRoom,
                                              Map<Integer, String> customerByBed,
                                              Map<String, Customers> customersMap,
                                              Map<String, List<InvoicesV1>> invoicesByCustomer,
                                              Rooms room, boolean includeEmptyBeds) {

        List<InvBedRes> bedResponses = new ArrayList<>();

        for (Beds bed : bedsByRoom.getOrDefault(room.getRoomId(), List.of())) {

            String customerId = customerByBed.get(bed.getBedId());

            Customers customer = customersMap.getOrDefault(customerId, null);

            List<InvoicesV1> customerInvoices = invoicesByCustomer.getOrDefault(customerId, List.of());

            List<InvConsolidatedRes> invoiceResponses = customerInvoices
                    .stream()
                    .map(i -> toInvConsolidatedRes(i, customer))
                    .toList();

            if (!includeEmptyBeds && invoiceResponses.isEmpty()) {
                continue;
            }

            int invoiceCount = customerInvoices.size();

            double actualRent = 0;
            double pendingRent = 0;
            double ebAmount = 0;
            double pendingAdvance = 0;
            double pendingNonRefundable = 0;
            double totalPendingAmount = 0;

            for (InvoicesV1 invoice : customerInvoices) {

                double totalAmount = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : 0;
                double invPaidAmount = invoice.getPaidAmount() != null ? invoice.getPaidAmount() : 0;
                double pendingAmount = totalAmount - invPaidAmount;
                double balanceAmount = invoice.getBalanceAmount() != null ? invoice.getBalanceAmount() : 0;
                double deductionAmount = invoice.getDeductionAmount() != null ? invoice.getDeductionAmount() : 0;

                List<InvoiceItems> invoiceItems = invoice.getInvoiceItems();

                if (InvoiceType.RENT.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.REASSIGN_RENT.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.OTHER.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.EB.name().equalsIgnoreCase(invoice.getInvoiceType())){
                    actualRent += totalAmount;
                    pendingRent += pendingAmount;

                    if (invoiceItems != null && !invoiceItems.isEmpty()){
                        ebAmount = invoiceItems
                                .stream()
                                .filter(item ->  com.smartstay.smartstay.ennum.InvoiceItems.EB.name()
                                        .equals(item.getInvoiceItem()))
                                .mapToDouble(InvoiceItems::getAmount)
                                .sum();
                    }
                }

                if (InvoiceType.BOOKING.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.ADVANCE.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.ADDITIONAL_ADVANCE.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.AMOUNT_HOLDING.name().equalsIgnoreCase(invoice.getInvoiceType()) ||
                        InvoiceType.EB_HOLDING.name().equalsIgnoreCase(invoice.getInvoiceType())) {
                    pendingAdvance += balanceAmount;
                }

                pendingNonRefundable += deductionAmount;
            }

            totalPendingAmount = pendingAdvance - pendingNonRefundable - pendingRent;

            String tenantName = null;
            String countryCode = null;
            String mobile = null;
            if (customer != null){
                tenantName = NameUtils.getFullName(customer.getFirstName(), customer.getLastName());
                countryCode = "+" + String.valueOf(customer.getCountryCode());
                mobile = customer.getMobile();
            }

            bedResponses.add(new InvBedRes(bed.getBedId(), bed.getBedName(), tenantName, countryCode,
                    mobile, String.valueOf(actualRent), String.valueOf(pendingRent), String.valueOf(ebAmount),
                    String.valueOf(pendingAdvance), String.valueOf(pendingNonRefundable),
                    String.valueOf(totalPendingAmount), String.valueOf(invoiceCount), invoiceResponses));
        }

        return bedResponses;
    }

    private <T> List<T> paginateList(List<T> data, boolean isPaginated,
                                     int page, int size) {

        if (!isPaginated) {
            return data;
        }

        int fromIndex = page * size;

        if (fromIndex >= data.size()) {
            return List.of();
        }

        int toIndex = Math.min(fromIndex + size, data.size());

        return data.subList(fromIndex, toIndex);
    }

    private List<Object> buildColumnData(InvBedRes bedRes, String floorName, String roomName,
                                         List<String> columnNames) {

        List<Object> row = new ArrayList<>();

        for (String column : columnNames) {
            switch (column) {
                case InvRegisterConsolidatedColumnUtils.FLOOR ->
                        row.add(floorName);
                case InvRegisterConsolidatedColumnUtils.ROOM ->
                        row.add(roomName);
                case InvRegisterConsolidatedColumnUtils.BED ->
                        row.add(bedRes.bedName());
                case InvRegisterConsolidatedColumnUtils.TENANT_NAME ->
                        row.add(bedRes.tenantName());
                case InvRegisterConsolidatedColumnUtils.CONTACT_NUMBER ->
                        row.add(bedRes.tenantCountryCode() + " " + bedRes.tenantMobile());
                case InvRegisterConsolidatedColumnUtils.ACTUAL_RENT ->
                        row.add(bedRes.actualRent());
                case InvRegisterConsolidatedColumnUtils.PENDING_RENT ->
                        row.add(bedRes.pendingRent());
                case InvRegisterConsolidatedColumnUtils.EB_AMOUNT ->
                        row.add(bedRes.ebAmount());
                case InvRegisterConsolidatedColumnUtils.PENDING_ADVANCE ->
                        row.add(bedRes.pendingAdvance());
                case InvRegisterConsolidatedColumnUtils.PENDING_NON_REFUNDABLE ->
                        row.add(bedRes.pendingNonRefundable());
                case InvRegisterConsolidatedColumnUtils.TOTAL_PENDING_AMOUNT ->
                        row.add(bedRes.totalPendingAmount());
                case InvRegisterConsolidatedColumnUtils.INVOICES ->
                        row.add(bedRes.invoiceCount());
            }
        }

        row.add(bedRes.invoices());

        return row;
    }
}
