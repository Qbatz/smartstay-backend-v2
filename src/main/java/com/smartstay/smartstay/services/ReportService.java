package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.invoices.InvoiceAggregateDto;
import com.smartstay.smartstay.dto.reports.ElectricityForReports;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.responses.Reports.ReportDetailsResponse;
import com.smartstay.smartstay.responses.Reports.ReportResponse;
import com.smartstay.smartstay.responses.Reports.TenantRegisterResponse;
import com.smartstay.smartstay.responses.transaction.TransactionReportResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;

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
    private AmenityRequestService amenityRequestService;

    @Autowired
    private HostelService hostelService;
    @Autowired
    private ElectricityService electricityService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private FloorsService floorsService;

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

        // Invoices for hostel
        int invoiceCount = invoiceV1Service.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        Double invoiceTotal = invoiceV1Service.sumTotalAmountByHostelIdAndDateRangeExcludingSettlement(hostelId,
                InvoiceType.SETTLEMENT.name(), startDate, endDate);
        Double paidTotal = invoiceV1Service.sumPaidAmountByHostelIdAndDateRangeExcludingSettlement(hostelId,
                InvoiceType.SETTLEMENT.name(), startDate, endDate);
        ReportResponse.InvoiceReport invoiceReport = ReportResponse.InvoiceReport.builder().noOfInvoices(invoiceCount)
                .totalAmount(invoiceTotal).build();

        Double outstandingAmount = 0.0;
        if (invoiceTotal != null && paidTotal != null) {
            outstandingAmount = invoiceTotal - paidTotal;
        }

        // Receipts
        int receiptCount = transactionService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        Double receiptTotal = transactionService.sumPaidAmountByHostelIdAndDateRange(hostelId, startDate, endDate);
        ReportResponse.ReceiptReport receiptReport = ReportResponse.ReceiptReport.builder().totalReceipts(receiptCount)
                .totalAmount(receiptTotal).build();

        // Banking
        int bankTransCount = bankTransactionService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        Double bankBalance = bankingService.sumBalanceByHostelId(hostelId);
        ReportResponse.BankingReport bankingReport = ReportResponse.BankingReport.builder()
                .totalTransactions(bankTransCount).totalAmount(bankBalance).build();

        // Tenants
        int filledBeds = bedsService.countOccupiedByHostelId(hostelId);
        int totalBeds = bedsService.countAllByHostelId(hostelId);
        List<Customers> listCustomers = customersService.findCustomerByHostelId(hostelId,
                Arrays.asList(CustomerStatus.CHECK_IN.name(), CustomerStatus.BOOKED.name(),
                        CustomerStatus.NOTICE.name(), CustomerStatus.SETTLEMENT_GENERATED.name()));

        double occupancyRate = 0.0;
        if (totalBeds > 0) {
            occupancyRate = ((double) filledBeds / totalBeds) * 100;
            occupancyRate = Utils.roundOffWithTwoDigit(occupancyRate);
        }

        List<Customers> checkedInCustomers = listCustomers.stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CHECK_IN.name())).toList();

        List<Customers> noticeCustomer = listCustomers.stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(CustomerStatus.NOTICE.name())
                        || i.getCurrentStatus().equalsIgnoreCase(CustomerStatus.SETTLEMENT_GENERATED.name()))
                .toList();

        List<Customers> vacatedCustomers = listCustomers.stream()
                .filter(i -> i.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())).toList();

        ReportResponse.TenantReport tenantReport = ReportResponse.TenantReport.builder()
                .totalTenants(listCustomers.size()).occupancyRate(occupancyRate)
                .noticeTenantCount(noticeCustomer.size()).activeTenantCount(checkedInCustomers.size())
                .checkoutTenantsCount(vacatedCustomers.size()).build();

        // Expenses
        int expenseCount = expenseService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        Double expenseTotal = expenseService.sumAmountByHostelIdAndDateRange(hostelId, startDate, endDate);
        ReportResponse.ExpenseReport expenseReport = ReportResponse.ExpenseReport.builder().totalExpenses(expenseCount)
                .totalExpenseAmount(expenseTotal).build();

        Double totalRevenue = 0.0;
        if (invoiceTotal != null && expenseTotal != null) {
            totalRevenue = invoiceTotal - expenseTotal;
        }

        // Vendor
        int vendorCount = vendorService.countByHostelId(hostelId);
        ReportResponse.VendorReport vendorReport = ReportResponse.VendorReport.builder().totalVendors(vendorCount)
                .build();

        // Complaints
        int complaintCount = complaintService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        int complaintActiveCount = complaintService.countActiveByHostelIdAndDateRange(hostelId,
                Arrays.asList(ComplaintStatus.PENDING.name(), ComplaintStatus.OPENED.name()), startDate, endDate);
        ReportResponse.ComplaintReport complaintReport = ReportResponse.ComplaintReport.builder()
                .totalComplaints(complaintCount).activeComplaints(complaintActiveCount).build();

        // Requests
        int requestCount = amenityRequestService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        int requestActiveCount = amenityRequestService.countActiveByHostelIdAndDateRange(hostelId,
                Arrays.asList(RequestStatus.PENDING.name(), RequestStatus.OPEN.name(), RequestStatus.INPROGRESS.name()),
                startDate, endDate);
        ReportResponse.RequestReport requestReport = ReportResponse.RequestReport.builder().totalRequests(requestCount)
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
        double totalReturnedAmount = finalSettlements.stream().filter(i -> i.getTotalAmount() < 0).mapToDouble(i -> {
            return i.getTotalAmount() * -1;
        }).sum();
        double totalPayableAmount = finalSettlements.stream().filter(i -> i.getTotalAmount() >= 0)
                .mapToDouble(InvoicesV1::getTotalAmount).sum();

        ReportResponse.FinalSettlementReport settlementReport = ReportResponse.FinalSettlementReport.builder()
                .totalReturnedAmount(totalReturnedAmount).totalPaidAmount(totalPayableAmount).totalAmount(totalAmount)
                .totalSettlements(totalSettlement).build();

        Calendar calPreviousMonth = Calendar.getInstance();
        calPreviousMonth.setTime(startDate);
        calPreviousMonth.add(Calendar.MONTH, -1);

        BillingDates previousBillingDate = hostelService.getBillingRuleOnDate(hostelId, calPreviousMonth.getTime());
        ElectricityForReports ebReports = electricityService.getPreviousMonthEbAmount(hostelId,
                previousBillingDate.currentBillStartDate(), previousBillingDate.currentBillEndDate());

        ReportResponse.ElectricityReport ebReportResponse = ReportResponse.ElectricityReport.builder()
                .totalAmount(ebReports.totalAmount()).totalUnits(ebReports.totalUnits())
                .totalEntries(ebReports.noOfEntries()).build();

        ReportResponse response = ReportResponse.builder().hostelId(hostelId).startDate(Utils.dateToString(startDate))
                .endDate(Utils.dateToString(endDate)).outStandingAmount(outstandingAmount).totalRevenue(totalRevenue)
                .invoices(invoiceReport).receipts(receiptReport).banking(bankingReport).tenantInfo(tenantReport)
                .expense(expenseReport).vendor(vendorReport).complaints(complaintReport).electricity(ebReportResponse)
                .settlement(settlementReport).requests(requestReport).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceReportDetails(String hostelId, String search, List<String> paymentStatus,
            List<String> invoiceModes, List<String> invoiceTypes, List<String> createdBy, String period,
            Double minPaidAmount, Double maxPaidAmount, Double minOutstandingAmount, Double maxOutstandingAmount,
            String customStartDate, String customEndDate, int page, int size) {
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

        BillingDates dates = calculateDateRange(period, hostelId);
        Date startDate = dates.currentBillStartDate();
        Date endDate = dates.currentBillEndDate();

        if (customStartDate != null && !customStartDate.isEmpty()) {
            startDate = Utils.stringToDate(customStartDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }
        if (customEndDate != null && !customEndDate.isEmpty()) {
            endDate = Utils.stringToDate(customEndDate.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }

        Pageable pageable = PageRequest.of(page, size);
        List<InvoicesV1> invoices = invoiceV1Service.getInvoicesForReport(hostelId, startDate, endDate, search,
                paymentStatus, invoiceModes, invoiceTypes, createdBy, minPaidAmount, maxPaidAmount,
                minOutstandingAmount, maxOutstandingAmount, pageable);
        List<ReportDetailsResponse.InvoiceDetail> invoiceDetails = mapToInvoiceDetails(invoices);

        ReportDetailsResponse.FilterOptions options = buildFilterOptions(hostelId);

        return buildReportResponse(hostelId, startDate, endDate, search, paymentStatus, invoiceModes, invoiceTypes,
                createdBy, minPaidAmount, maxPaidAmount, minOutstandingAmount, maxOutstandingAmount, invoices,
                invoiceDetails, options, page, size);
    }

    private BillingDates calculateDateRange(String period, String hostelId) {
        if (period == null || period.isEmpty()) {
            return hostelService.getCurrentBillStartAndEndDates(hostelId);
        }

        Calendar calendar = Calendar.getInstance();
        Date today = new Date();
        calendar.setTime(today);
        Date startDate = null;
        Date endDate = null;

        if (period.equalsIgnoreCase("this month")) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            startDate = calendar.getTime();
            endDate = today;
        } else if (period.equalsIgnoreCase("last month")) {
            calendar.add(Calendar.MONTH, -1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            startDate = calendar.getTime();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            endDate = calendar.getTime();
        } else if (period.equalsIgnoreCase("last 3 month")) {
            calendar.add(Calendar.MONTH, -3);
            startDate = calendar.getTime();
            endDate = today;
        } else if (period.equalsIgnoreCase("6 month")) {
            calendar.add(Calendar.MONTH, -6);
            startDate = calendar.getTime();
            endDate = today;
        }
        return new BillingDates(startDate, endDate, null, null);
    }

    private List<ReportDetailsResponse.InvoiceDetail> mapToInvoiceDetails(List<InvoicesV1> invoices) {
        List<ReportDetailsResponse.InvoiceDetail> details = invoices.stream().map(this::convertToInvoiceDetail)
                .collect(Collectors.toList());

        populateCustomerDetails(details, invoices);
        return details;
    }

    private ReportDetailsResponse.InvoiceDetail convertToInvoiceDetail(InvoicesV1 inv) {
        return ReportDetailsResponse.InvoiceDetail.builder().invoiceId(inv.getInvoiceId())
                .invoiceNumber(inv.getInvoiceNumber()).customerId(inv.getCustomerId())
                .invoiceAmount(inv.getTotalAmount()).baseAmount(inv.getBasePrice()).paidAmount(inv.getPaidAmount())
                .dueAmount(inv.getTotalAmount() - (inv.getPaidAmount() != null ? inv.getPaidAmount() : 0.0))
                .cgst(inv.getCgst()).sgst(inv.getSgst()).gst(inv.getGst())
                .createdAt(Utils.dateToString(inv.getCreatedAt())).createdBy(inv.getCreatedBy())
                .hostelId(inv.getHostelId()).invoiceDate(Utils.dateToString(inv.getInvoiceStartDate()))
                .dueDate(Utils.dateToString(inv.getInvoiceDueDate()))
                .invoiceType(Utils.capitalize(inv.getInvoiceType())).invoiceMode(Utils.capitalize(inv.getInvoiceMode()))
                .paymentStatus(Utils.capitalize(inv.getPaymentStatus()))
                .updatedAt(Utils.dateToString(inv.getUpdatedAt())).isCancelled(inv.isCancelled()).build();
    }

    private void populateCustomerDetails(List<ReportDetailsResponse.InvoiceDetail> details, List<InvoicesV1> invoices) {
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
                    detail.setProfilePic(c.getProfilePic());
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

        List<String> periods = Arrays.asList("This Month", "Last Month", "Last 3 Month", "6 Month");

        return ReportDetailsResponse.FilterOptions.builder().paymentStatus(toFilterItems(PaymentStatus.values()))
                .invoiceModes(toFilterItems(InvoiceMode.values())).invoiceTypes(toFilterItems(InvoiceType.values()))
                .createdBy(createdByOptions).periods(periods).build();
    }

    private <E extends Enum<E>> List<ReportDetailsResponse.FilterItem> toFilterItems(E[] values) {
        return Arrays.stream(values)
                .map(e -> new ReportDetailsResponse.FilterItem(Utils.capitalize(e.name()), e.name()))
                .collect(Collectors.toList());
    }

    private ResponseEntity<?> buildReportResponse(String hostelId, Date startDate, Date endDate, String search,
            List<String> paymentStatus, List<String> invoiceModes, List<String> invoiceTypes, List<String> createdBy,
            Double minPaidAmount, Double maxPaidAmount, Double minOutstandingAmount, Double maxOutstandingAmount,
            List<InvoicesV1> invoices, List<ReportDetailsResponse.InvoiceDetail> invoiceDetails,
            ReportDetailsResponse.FilterOptions options, int page, int size) {

        InvoiceAggregateDto aggregates = invoiceV1Service.getInvoiceAggregatesForReport(hostelId, startDate, endDate,
                search, paymentStatus, invoiceModes, invoiceTypes, createdBy, minPaidAmount, maxPaidAmount,
                minOutstandingAmount, maxOutstandingAmount);

        int totalInvoices = 0;
        Double totalAmount = 0.0;
        Double paidAmount = 0.0;

        if (aggregates != null) {
            if (aggregates.getCount() != null)
                totalInvoices = aggregates.getCount().intValue();
            if (aggregates.getTotalAmount() != null)
                totalAmount = aggregates.getTotalAmount();
            if (aggregates.getPaidAmount() != null)
                paidAmount = aggregates.getPaidAmount();
        }
        Double outStandingAmount = totalAmount - paidAmount;

        int totalPages = (int) Math.ceil((double) totalInvoices / size);

        ReportDetailsResponse response = ReportDetailsResponse.builder().totalInvoices(totalInvoices).currentPage(page)
                .totalPages(totalPages).totalAmount(totalAmount).outStandingAmount(outStandingAmount)
                .paidAmount(paidAmount).startDate(Utils.dateToString(startDate)).endDate(Utils.dateToString(endDate))
                .filterOptions(options).invoiceList(invoiceDetails).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getReceiptDetails(String hostelId, String period, String customStartDate,
            String customEndDate, List<String> invoiceType, List<String> paymentMode, List<String> collectedBy,
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

        TransactionReportResponse response = transactionService.getTransactionReport(hostelId, period, customStartDate,
                customEndDate, invoiceType, paymentMode, collectedBy, page, size);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getExpenseDetails(String hostelId, String period, String customStartDate,
            String customEndDate, List<Long> categoryIds, List<Long> subCategoryIds, List<String> paymentModes,
            List<String> paidTo, List<String> createdBy, int page, int size) {
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
                customEndDate, categoryIds, subCategoryIds, paymentModes, paidTo, createdBy, page, size),
                HttpStatus.OK);
    }

    public ResponseEntity<?> getTenantRegister(String hostelId, String search, List<String> status, List<Integer> rooms,
            List<Integer> floors, String period, String customStartDate, String customEndDate, int page, int size) {
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
                return buildEmptyTenantResponse(startDate, endDate, page, size, hostelId, user.getParentId());
            }
        }

        List<BookingsV1> allBookings = bookingsService.findAllBookingsWithFilters(hostelId, startDate, endDate,
                customerIds, status, rooms, floors);

        Set<String> uniqueTenants = new HashSet<>();
        double activeAmount = 0;
        double noticeAmount = 0;
        double checkoutAmount = 0;
        double inactiveAmount = 0;

        for (BookingsV1 b : allBookings) {
            uniqueTenants.add(b.getCustomerId());
            String bookingStatus = b.getCurrentStatus();
            double amount = (b.getRentAmount() != null ? b.getRentAmount() : 0);

            if (BookingStatus.CHECKIN.name().equalsIgnoreCase(bookingStatus)) {
                activeAmount += amount;
            } else if (BookingStatus.NOTICE.name().equalsIgnoreCase(bookingStatus)) {
                noticeAmount += amount;
            } else if (BookingStatus.VACATED.name().equalsIgnoreCase(bookingStatus)
                    || BookingStatus.TERMINATED.name().equalsIgnoreCase(bookingStatus)) {
                checkoutAmount += amount;
            } else if (BookingStatus.CANCELLED.name().equalsIgnoreCase(bookingStatus)) {
                inactiveAmount += amount;
            }
        }

        TenantRegisterResponse.Summary summary = TenantRegisterResponse.Summary.builder()
                .totalTenants(uniqueTenants.size())
                .activeTenants(new TenantRegisterResponse.SegmentSummary(activeAmount, 0))
                .noticePeriod(new TenantRegisterResponse.SegmentSummary(noticeAmount, 0))
                .checkoutMTD(new TenantRegisterResponse.SegmentSummary(checkoutAmount, 0))
                .inactive(new TenantRegisterResponse.SegmentSummary(inactiveAmount, 0)).build();

        Page<BookingsV1> bookingsPage = bookingsService
                .findBookingsWithFilters(hostelId, startDate, endDate, customerIds, status, rooms, floors, page, size);
        List<BookingsV1> paginatedBookings = bookingsPage.getContent();
        long totalRecords = bookingsPage.getTotalElements();

        List<String> pageCustomerIds = paginatedBookings.stream().map(BookingsV1::getCustomerId)
                .collect(Collectors.toList());
        Map<String, Customers> customerMap = customersService.getCustomerDetails(pageCustomerIds).stream()
                .collect(Collectors.toMap(Customers::getCustomerId, c -> c, (a, b1) -> b1));

        Map<Integer, Long> sharingMap = bedsService.countBedsByRoomForHostel(hostelId).stream()
                .collect(Collectors.toMap(com.smartstay.smartstay.dto.beds.RoomBedCount::getRoomId,
                        com.smartstay.smartstay.dto.beds.RoomBedCount::getBedCount, (a, b1) -> b1));

        List<TenantRegisterResponse.TenantDetail> details = new ArrayList<>();
        for (BookingsV1 b : paginatedBookings) {
            Customers c = customerMap.get(b.getCustomerId());
            String name = (c != null) ? (c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : ""))
                    : "N/A";
            String mobile = (c != null) ? c.getMobile() : "N/A";

            String sharing = "N/A";
            Long bedCount = sharingMap.get(b.getRoomId());
            if (bedCount != null) {
                if (bedCount == 1)
                    sharing = "Single";
                else if (bedCount == 2)
                    sharing = "Double";
                else if (bedCount == 3)
                    sharing = "Triple";
                else
                    sharing = bedCount + " Sharing";
            }

            long diffInMillis = (b.getCheckoutDate() != null ? b.getCheckoutDate().getTime()
                    : System.currentTimeMillis()) - b.getJoiningDate().getTime();
            long days = Math.max(1, diffInMillis / (1000 * 60 * 60 * 24));
            String stayDuration = days + (days == 1 ? " day" : " days");

            details.add(TenantRegisterResponse.TenantDetail.builder().tenantId(b.getCustomerId())
                    .name(Utils.capitalize(name.trim())).mobileNo(mobile).sharing(sharing)
                    .checkInDate(Utils.dateToString(b.getJoiningDate()))
                    .checkOutDate(b.getCheckoutDate() != null ? Utils.dateToString(b.getCheckoutDate()) : null)
                    .checkInAmount(b.getRentAmount() != null ? b.getRentAmount() : 0)
                    .checkOutAmount(b.getRentAmount() != null ? b.getRentAmount() : 0).stayDuration(stayDuration)
                    .build());
        }

        TenantRegisterResponse.Filters filtersData = buildTenantFilters(hostelId, user.getParentId());

        TenantRegisterResponse response = TenantRegisterResponse.builder().status(true)
                .message("Tenant register fetched successfully")
                .dateRange(TenantRegisterResponse.DateRange
                        .builder().from(Utils.dateToString(startDate)).to(Utils.dateToString(endDate)).build())
                .summary(summary).tenants(details)
                .pagination(TenantRegisterResponse.Pagination.builder().currentPage(page).pageSize(size)
                        .totalRecords(totalRecords).totalPages(bookingsPage.getTotalPages()).build())
                .filters(filtersData).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<?> buildEmptyTenantResponse(Date startDate, Date endDate, int page, int size,
            String hostelId, String parentId) {
        TenantRegisterResponse.Summary summary = TenantRegisterResponse.Summary.builder().totalTenants(0)
                .activeTenants(new TenantRegisterResponse.SegmentSummary(0, 0))
                .noticePeriod(new TenantRegisterResponse.SegmentSummary(0, 0))
                .checkoutMTD(new TenantRegisterResponse.SegmentSummary(0, 0))
                .inactive(new TenantRegisterResponse.SegmentSummary(0, 0)).build();
        TenantRegisterResponse.Filters filtersData = buildTenantFilters(hostelId, parentId);
        TenantRegisterResponse response = TenantRegisterResponse.builder().status(true)
                .message("Tenant register fetched successfully")
                .dateRange(TenantRegisterResponse.DateRange.builder().from(Utils.dateToString(startDate))
                        .to(Utils.dateToString(endDate)).build())
                .summary(summary).tenants(new ArrayList<>()).pagination(TenantRegisterResponse.Pagination.builder()
                        .currentPage(page).pageSize(size).totalRecords(0).totalPages(0).build())
                .filters(filtersData).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private TenantRegisterResponse.Filters buildTenantFilters(String hostelId, String parentId) {
        List<TenantRegisterResponse.FilterItem> statusList = Arrays.stream(BookingStatus.values())
                .map(s -> new TenantRegisterResponse.FilterItem(s.name(), Utils.capitalize(s.name())))
                .collect(Collectors.toList());

        List<TenantRegisterResponse.FilterItem> periodList = new ArrayList<>();
        periodList.add(new TenantRegisterResponse.FilterItem("this_month", "This Month"));
        periodList.add(new TenantRegisterResponse.FilterItem("last_month", "Last Month"));
        periodList.add(new TenantRegisterResponse.FilterItem("last_6_months", "Last 6 Months"));

        List<TenantRegisterResponse.FilterItem> floorList = floorsService.getFloorByHostelID(hostelId, parentId)
                .stream()
                .map(f -> new TenantRegisterResponse.FilterItem(f.getFloorId(), Utils.capitalize(f.getFloorName())))
                .collect(Collectors.toList());

        List<TenantRegisterResponse.FilterItem> roomList = roomsService.getAllRoomsByHostelId(hostelId).stream()
                .map(r -> new TenantRegisterResponse.FilterItem(r.getRoomId(), Utils.capitalize(r.getRoomName())))
                .collect(Collectors.toList());

        return TenantRegisterResponse.Filters.builder().tenantStatus(statusList).period(periodList).floor(floorList)
                .room(roomList).build();
    }
}
