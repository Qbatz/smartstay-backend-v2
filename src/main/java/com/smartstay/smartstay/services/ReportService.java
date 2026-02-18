package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.BedDetails;
import com.smartstay.smartstay.dto.beds.RoomBedCount;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.invoices.InvoiceAggregateDto;
import com.smartstay.smartstay.dto.reports.ComplaintsReportFilterRequest;
import com.smartstay.smartstay.dto.reports.ComplaintsReportResponse;
import com.smartstay.smartstay.dto.reports.ElectricityForReports;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.ennum.PaymentStatus;
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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
                ReportResponse.InvoiceReport invoiceReport = ReportResponse.InvoiceReport.builder()
                                .noOfInvoices(invoiceCount)
                                .totalAmount(invoiceTotal).build();

                Double outstandingAmount = 0.0;
                if (invoiceTotal != null && paidTotal != null) {
                        outstandingAmount = invoiceTotal - paidTotal;
                }

                // Receipts
                int receiptCount = transactionService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
                Double receiptTotal = transactionService.sumPaidAmountByHostelIdAndDateRange(hostelId, startDate,
                                endDate);
                ReportResponse.ReceiptReport receiptReport = ReportResponse.ReceiptReport.builder()
                                .totalReceipts(receiptCount)
                                .totalAmount(receiptTotal).build();

                // Banking
                int bankTransCount = bankTransactionService.countByHostelIdAndDateRange(hostelId, startDate, endDate);
                Double bankBalance = bankingService.sumBalanceByHostelId(hostelId);
                ReportResponse.BankingReport bankingReport = ReportResponse.BankingReport.builder()
                                .totalTransactions(bankTransCount).totalAmount(bankBalance).build();

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
                                .totalTenants(uniqueTenants.size()).occupancyRate(occupancyRate)
                                .noticeTenantCount(noticeCount).activeTenantCount(activeCount)
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
                                Arrays.asList(RequestStatus.PENDING.name(), RequestStatus.OPEN.name(),
                                                RequestStatus.INPROGRESS.name()),
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
                                .endDate(Utils.dateToString(endDate)).outStandingAmount(outstandingAmount)
                                .totalRevenue(totalRevenue)
                                .invoices(invoiceReport).receipts(receiptReport).banking(bankingReport)
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

                return buildReportResponse(hostelId, startDate, endDate, search, paymentStatus, invoiceModes,
                                invoiceTypes,
                                createdBy, minPaidAmount, maxPaidAmount, minOutstandingAmount, maxOutstandingAmount,
                                invoices,
                                invoiceDetails, options, page, size);
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
                } else if (period.equalsIgnoreCase("last 3 month") || period.equalsIgnoreCase("last_3_month")) {
                        cal.setTime(billingDates.currentBillStartDate());
                        cal.add(Calendar.MONTH, -3);
                        BillingDates bDates = hostelService.getBillingRuleOnDate(hostelId, cal.getTime());
                        return new BillingDates(bDates.currentBillStartDate(), billingDates.currentBillEndDate(), bDates.dueDate(), bDates.dueDays());
                } else if (period.equalsIgnoreCase("last 6 months") || period.equalsIgnoreCase("last_6_months")) {
                        cal.setTime(billingDates.currentBillStartDate());
                        cal.add(Calendar.MONTH, -6);
                        BillingDates bDates = hostelService.getBillingRuleOnDate(hostelId, cal.getTime());
                        return new BillingDates(bDates.currentBillStartDate(), billingDates.currentBillEndDate(), bDates.dueDate(), bDates.dueDays());
                }
                return billingDates;
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
                                .paymentStatus(Utils.capitalize(inv.getPaymentStatus()))
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

                return ReportDetailsResponse.FilterOptions.builder()
                                .paymentStatus(toFilterItems(PaymentStatus.values()))
                                .invoiceModes(toFilterItems(InvoiceMode.values()))
                                .invoiceTypes(toFilterItems(InvoiceType.values()))
                                .createdBy(createdByOptions).periods(periods).build();
        }

        private <E extends Enum<E>> List<ReportDetailsResponse.FilterItem> toFilterItems(E[] values) {
                return Arrays.stream(values)
                                .map(e -> new ReportDetailsResponse.FilterItem(Utils.capitalize(e.name()), e.name()))
                                .collect(Collectors.toList());
        }

        private ResponseEntity<?> buildReportResponse(String hostelId, Date startDate, Date endDate, String search,
                        List<String> paymentStatus, List<String> invoiceModes, List<String> invoiceTypes,
                        List<String> createdBy,
                        Double minPaidAmount, Double maxPaidAmount, Double minOutstandingAmount,
                        Double maxOutstandingAmount,
                        List<InvoicesV1> invoices, List<ReportDetailsResponse.InvoiceDetail> invoiceDetails,
                        ReportDetailsResponse.FilterOptions options, int page, int size) {

                InvoiceAggregateDto aggregates = invoiceV1Service.getInvoiceAggregatesForReport(hostelId, startDate,
                                endDate,
                                search, paymentStatus, invoiceModes, invoiceTypes, createdBy, minPaidAmount,
                                maxPaidAmount,
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
                Double refundAmount = 0.0;
                if (aggregates != null && aggregates.getRefundAmount() != null) {
                        refundAmount = aggregates.getRefundAmount();
                }
                Double profit = totalAmount - refundAmount;
                Double outStandingAmount = totalAmount - paidAmount;

                int totalPages = (int) Math.ceil((double) totalInvoices / size);

                ReportDetailsResponse response = ReportDetailsResponse.builder().totalInvoices(totalInvoices)
                                .currentPage(page)
                                .totalPages(totalPages).totalAmount(totalAmount).refundAmount(refundAmount)
                                .profit(profit).outStandingAmount(outStandingAmount)
                                .paidAmount(paidAmount).startDate(Utils.dateToString(startDate))
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
                        String customEndDate, List<Long> categoryIds, List<Long> subCategoryIds,
                        List<String> paymentModes,
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
                                customEndDate, categoryIds, subCategoryIds, paymentModes, paidTo, createdBy, page,
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
                                return buildEmptyTenantResponse(startDate, endDate, page, size, hostelId,
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
                        }
                        else if (BookingStatus.BOOKED.name().equalsIgnoreCase(bookingStatus)) {
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

                Page<BookingsV1> bookingsPage = bookingsService
                                .findBookingsWithFilters(hostelId, startDate, endDate, customerIds, status, rooms,
                                                floors, page, size);
                List<BookingsV1> paginatedBookings = bookingsPage.getContent();
                long totalRecords = bookingsPage.getTotalElements();

                List<String> pageCustomerIds = paginatedBookings.stream().map(BookingsV1::getCustomerId)
                                .collect(Collectors.toList());
                Map<String, Customers> customerMap = customersService.getCustomerDetails(pageCustomerIds).stream()
                                .collect(Collectors.toMap(Customers::getCustomerId, c -> c, (a, b1) -> b1));

                Map<Integer, Long> sharingMap = bedsService.countBedsByRoomForHostel(hostelId).stream()
                                .collect(Collectors.toMap(RoomBedCount::getRoomId,
                                                RoomBedCount::getBedCount, (a, b1) -> b1));

                List<TenantRegisterResponse.TenantDetail> details = new ArrayList<>();
                for (BookingsV1 b : paginatedBookings) {
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
                                }
                                else {
                                        days = Utils.findNumberOfDays(b.getJoiningDate(),new Date());
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
                                        .build());
                }

                TenantRegisterResponse.Filters filtersData = buildTenantFilters(hostelId, user.getParentId());

                TenantRegisterResponse response = TenantRegisterResponse.builder().status(true)
                                .message("Tenant register fetched successfully")
                                .dateRange(TenantRegisterResponse.DateRange
                                                .builder().from(Utils.dateToString(startDate))
                                                .to(Utils.dateToString(endDate)).build())
                                .summary(summary).tenants(details)
                                .pagination(TenantRegisterResponse.Pagination.builder().currentPage(page).pageSize(size)
                                                .totalRecords(totalRecords).totalPages(bookingsPage.getTotalPages())
                                                .build())
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
                                .dateRange(TenantRegisterResponse.DateRange.builder()
                                                .from(Utils.dateToString(startDate))
                                                .to(Utils.dateToString(endDate)).build())
                                .summary(summary).tenants(new ArrayList<>())
                                .pagination(TenantRegisterResponse.Pagination.builder()
                                                .currentPage(page).pageSize(size).totalRecords(0).totalPages(0).build())
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
                        }
                        else {
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
                                                .map(i2 -> new TenantRegisterResponse.FilterItem(i2, i2+""))
                                                .toList();
                                        if (i == 1) {
                                              return new TenantRegisterResponse.SharingTypeFilter(1, Utils.SHARING_TYPE_SINGLE, filterItems);
                                        }
                                        else if (i == 2) {
                                                return new TenantRegisterResponse.SharingTypeFilter(2, Utils.SHARING_TYPE_TWO, filterItems);
                                        }
                                        else if (i == 3) {
                                                return new TenantRegisterResponse.SharingTypeFilter(3, Utils.SHARING_TYPE_THREE, filterItems);
                                        }
                                        else {
                                                return new TenantRegisterResponse.SharingTypeFilter(i,  i + " Sharing", filterItems);
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
}
