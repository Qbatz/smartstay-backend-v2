package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.dto.reports.ElectricityForReports;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.responses.Reports.ReportDetailsResponse;
import com.smartstay.smartstay.responses.Reports.ReportResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class ReportService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private InvoiceV1Service invoicesService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private InvoicesV1Repository invoicesRepository;
    @Autowired
    private TransactionV1Repository transactionRepository;
    @Autowired
    private BankingRepository bankingRepository;
    @Autowired
    private BankTransactionRepository bankTransactionRepository;
    @Autowired
    private CustomersRepository customersRepository;
    @Autowired
    private BedsRepository bedsRepository;
    @Autowired
    private ExpensesRepository expensesRepository;
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private ComplaintRepository complaintRepository;
    @Autowired
    private AmenityRequestRepository amenityRequestRepository;

    @Autowired
    private HostelService hostelService;
    @Autowired
    private ElectricityService electricityService;

    public ResponseEntity<?> getReports(String hostelId) {
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

        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);

        Date startDate = billingDates.currentBillStartDate();
        Date endDate = billingDates.currentBillEndDate();

        // Invoices for hostel
        int invoiceCount = invoicesRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        Double invoiceTotal = invoicesRepository.sumTotalAmountByHostelIdAndDateRangeExcludingSettlement(
                hostelId, InvoiceType.SETTLEMENT.name(), startDate, endDate);
        Double paidTotal = invoicesRepository.sumPaidAmountByHostelIdAndDateRangeExcludingSettlement(hostelId,
                InvoiceType.SETTLEMENT.name(), startDate, endDate);
        ReportResponse.InvoiceReport invoiceReport = ReportResponse.InvoiceReport.builder()
                .noOfInvoices(invoiceCount)
                .totalAmount(invoiceTotal)
                .build();

        Double outstandingAmount = 0.0;
        if (invoiceTotal != null && paidTotal != null) {
            outstandingAmount = invoiceTotal - paidTotal;
        }

        // Receipts
        int receiptCount = transactionRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        Double receiptTotal = transactionRepository.sumPaidAmountByHostelIdAndDateRange(hostelId, startDate,
                endDate);
        ReportResponse.ReceiptReport receiptReport = ReportResponse.ReceiptReport.builder()
                .totalReceipts(receiptCount)
                .totalAmount(receiptTotal)
                .build();

        // Banking
        int bankTransCount = bankTransactionRepository.countByHostelIdAndDateRange(hostelId, startDate,
                endDate);
        Double bankBalance = bankingRepository.sumBalanceByHostelId(hostelId);
        ReportResponse.BankingReport bankingReport = ReportResponse.BankingReport.builder()
                .totalTransactions(bankTransCount)
                .totalAmount(bankBalance)
                .build();

        // Tenants
        int tenantCount = customersRepository.countByHostelIdAndStatusIn(hostelId,
                Arrays.asList(CustomerStatus.CHECK_IN.name(), CustomerStatus.NOTICE.name()));
        int filledBeds = bedsRepository.countOccupiedByHostelId(hostelId);
        int totalBeds = bedsRepository.countAllByHostelId(hostelId);
        double occupancyRate = 0.0;
        if (totalBeds > 0) {
            occupancyRate = ((double) filledBeds / totalBeds) * 100;
            occupancyRate = Utils.roundOffWithTwoDigit(occupancyRate);
        }
        ReportResponse.TenantReport tenantReport = ReportResponse.TenantReport.builder()
                .totalTenants(tenantCount)
                .occupancyRate(occupancyRate)
                .build();

        // Expenses
        int expenseCount = expensesRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        Double expenseTotal = expensesRepository.sumAmountByHostelIdAndDateRange(hostelId, startDate, endDate);
        ReportResponse.ExpenseReport expenseReport = ReportResponse.ExpenseReport.builder()
                .totalExpenses(expenseCount)
                .totalExpenseAmount(expenseTotal)
                .build();

        Double totalRevenue = 0.0;
        if (invoiceTotal != null && expenseTotal != null) {
            totalRevenue = invoiceTotal - expenseTotal;
        }

        // Vendor
        int vendorCount = vendorRepository.countByHostelId(hostelId);
        ReportResponse.VendorReport vendorReport = ReportResponse.VendorReport.builder()
                .totalVendors(vendorCount)
                .build();

        // Complaints
        int complaintCount = complaintRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        int complaintActiveCount = complaintRepository.countActiveByHostelIdAndDateRange(hostelId,
                Arrays.asList(ComplaintStatus.PENDING.name(), ComplaintStatus.OPENED.name()), startDate,
                endDate);
        ReportResponse.ComplaintReport complaintReport = ReportResponse.ComplaintReport.builder()
                .totalComplaints(complaintCount)
                .activeComplaints(complaintActiveCount)
                .build();

        // Requests
        int requestCount = amenityRequestRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
        int requestActiveCount = amenityRequestRepository.countActiveByHostelIdAndDateRange(hostelId,
                Arrays.asList(RequestStatus.PENDING.name(), RequestStatus.OPEN.name(),
                        RequestStatus.INPROGRESS.name()),
                startDate, endDate);
        ReportResponse.RequestReport requestReport = ReportResponse.RequestReport.builder()
                .totalRequests(requestCount)
                .activeRequests(requestActiveCount)
                .build();

        List<InvoicesV1> finalSettlements = invoicesService.getCurrentMonthFinalSettlement(hostelId, startDate, endDate);
        int totalSettlement = finalSettlements.size();
        double totalAmount = finalSettlements
                .stream()
                .mapToDouble(i -> {
                    if (i.getTotalAmount() < 0) {
                        return i.getTotalAmount() * -1;
                    }
                    return i.getTotalAmount();
                })
                .sum();
        double totalReturnedAmount = finalSettlements
                .stream()
                .filter(i -> i.getTotalAmount() < 0)
                .mapToDouble(i -> {
                    return i.getTotalAmount() * -1;
                })
                .sum();
        double totalPayableAmount = finalSettlements
                .stream()
                .filter(i -> i.getTotalAmount() >= 0)
                .mapToDouble(InvoicesV1::getTotalAmount)
                .sum();

        ReportResponse.FinalSettlementReport settlementReport = ReportResponse
                .FinalSettlementReport
                .builder()
                .totalReturnedAmount(totalReturnedAmount)
                .totalPaidAmount(totalPayableAmount)
                .totalAmount(totalAmount)
                .totalSettlements(totalSettlement)
                .build();

        Calendar calPreviousMonth = Calendar.getInstance();
        calPreviousMonth.setTime(startDate);
        calPreviousMonth.add(Calendar.MONTH, -1);

        BillingDates previousBillingDate = hostelService.getBillingRuleOnDate(hostelId, calPreviousMonth.getTime());
        ElectricityForReports ebReports = electricityService.getPreviousMonthEbAmount(hostelId, previousBillingDate.currentBillStartDate(), previousBillingDate.currentBillEndDate());


        ReportResponse.ElectricityReport ebReportResponse = ReportResponse.ElectricityReport
                .builder()
                .totalAmount(ebReports.totalAmount())
                .totalUnits(ebReports.totalUnits())
                .totalEntries(ebReports.noOfEntries())
                .build();



        ReportResponse response = ReportResponse.builder()
                .hostelId(hostelId)
                .startDate(Utils.dateToString(billingDates.currentBillStartDate()))
                .endDate(Utils.dateToString(billingDates.currentBillEndDate()))
                .outStandingAmount(outstandingAmount)
                .totalRevenue(totalRevenue)
                .invoices(invoiceReport)
                .receipts(receiptReport)
                .banking(bankingReport)
                .tenantInfo(tenantReport)
                .expense(expenseReport)
                .vendor(vendorReport)
                .complaints(complaintReport)
                .requests(requestReport)
                .electricity(ebReportResponse)
                .settlement(settlementReport)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getInvoiceReportDetails(String hostelId, String search, List<String> paymentStatus,
            List<String> invoiceModes, List<String> invoiceTypes, List<String> createdBy, String period, int page,
            int size) {
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

        Pageable pageable = PageRequest.of(page, size);
        List<InvoicesV1> invoices = invoicesRepository.findInvoicesByFilters(hostelId, startDate, endDate, search,
                paymentStatus, invoiceModes, invoiceTypes, createdBy, pageable);
        List<ReportDetailsResponse.InvoiceDetail> invoiceDetails = mapToInvoiceDetails(invoices);

        ReportDetailsResponse.FilterOptions options = buildFilterOptions(hostelId);

        return buildReportResponse(hostelId, startDate, endDate, search, paymentStatus, invoiceModes, invoiceTypes,
                createdBy, invoices, invoiceDetails, options, page, size);
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
        }
        return new BillingDates(startDate, endDate, null, null);
    }

    private List<ReportDetailsResponse.InvoiceDetail> mapToInvoiceDetails(List<InvoicesV1> invoices) {
        List<ReportDetailsResponse.InvoiceDetail> details = invoices.stream()
                .map(this::convertToInvoiceDetail)
                .collect(Collectors.toList());

        populateCustomerDetails(details, invoices);
        return details;
    }

    private ReportDetailsResponse.InvoiceDetail convertToInvoiceDetail(InvoicesV1 inv) {
        return ReportDetailsResponse.InvoiceDetail.builder()
                .invoiceId(inv.getInvoiceId())
                .invoiceNumber(inv.getInvoiceNumber())
                .customerId(inv.getCustomerId())
                .invoiceAmount(inv.getTotalAmount())
                .baseAmount(inv.getBasePrice())
                .paidAmount(inv.getPaidAmount())
                .dueAmount(inv.getTotalAmount() - (inv.getPaidAmount() != null ? inv.getPaidAmount() : 0.0))
                .cgst(inv.getCgst())
                .sgst(inv.getSgst())
                .gst(inv.getGst())
                .createdAt(Utils.dateToString(inv.getCreatedAt()))
                .createdBy(inv.getCreatedBy())
                .hostelId(inv.getHostelId())
                .invoiceDate(Utils.dateToString(inv.getInvoiceStartDate()))
                .dueDate(Utils.dateToString(inv.getInvoiceDueDate()))
                .invoiceType(inv.getInvoiceType())
                .invoiceMode(inv.getInvoiceMode())
                .paymentStatus(inv.getPaymentStatus())
                .updatedAt(Utils.dateToString(inv.getUpdatedAt()))
                .isCancelled(inv.isCancelled())
                .build();
    }

    private void populateCustomerDetails(List<ReportDetailsResponse.InvoiceDetail> details, List<InvoicesV1> invoices) {
        List<String> customerIds = invoices.stream()
                .map(InvoicesV1::getCustomerId)
                .distinct()
                .collect(Collectors.toList());

        if (!customerIds.isEmpty()) {
            Map<String, com.smartstay.smartstay.dao.Customers> customerMap = customersRepository
                    .findByCustomerIdIn(customerIds)
                    .stream()
                    .collect(Collectors.toMap(com.smartstay.smartstay.dao.Customers::getCustomerId, c -> c));

            details.forEach(detail -> {
                com.smartstay.smartstay.dao.Customers c = customerMap.get(detail.getCustomerId());
                if (c != null) {
                    detail.setFirstName(c.getFirstName());
                    detail.setLastName(c.getLastName());
                    detail.setFullName(c.getFirstName() + " " + c.getLastName());
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
        }
        else if (firstName != null) {
            if (firstName.length() > 1) {
                initials.append(firstName.toUpperCase().charAt(1));
            }
        }
        return initials.toString();
    }

    private ReportDetailsResponse.FilterOptions buildFilterOptions(String hostelId) {
        List<Object[]> creators = invoicesRepository.findDistinctCreatedBy(hostelId);
        List<ReportDetailsResponse.UserFilterItem> createdByOptions = new ArrayList<>();
        if (creators != null) {
            for (Object[] row : creators) {
                String uId = (String) row[0];
                String fName = (String) row[1];
                String lName = (String) row[2];
                String fullName = (fName != null ? fName : "") + " " + (lName != null ? lName : "");
                createdByOptions.add(ReportDetailsResponse.UserFilterItem.builder()
                        .userId(uId)
                        .name(fullName.trim())
                        .build());
            }
        }

        return ReportDetailsResponse.FilterOptions.builder()
                .paymentStatus(toFilterItems(PaymentStatus.values()))
                .invoiceModes(toFilterItems(InvoiceMode.values()))
                .invoiceTypes(toFilterItems(InvoiceType.values()))
                .createdBy(createdByOptions)
                .build();
    }

    private <E extends Enum<E>> List<ReportDetailsResponse.FilterItem> toFilterItems(E[] values) {
        return Arrays.stream(values)
                .map(e -> new ReportDetailsResponse.FilterItem(e.name(), e.name()))
                .collect(Collectors.toList());
    }

    private ResponseEntity<?> buildReportResponse(String hostelId, Date startDate, Date endDate, String search,
            List<String> paymentStatus, List<String> invoiceModes,
            List<String> invoiceTypes, List<String> createdBy,
            List<InvoicesV1> invoices, List<ReportDetailsResponse.InvoiceDetail> invoiceDetails,
            ReportDetailsResponse.FilterOptions options, int page, int size) {

        List<Object[]> aggregates = invoicesRepository.findInvoiceAggregatesByFilters(hostelId, startDate, endDate,
                search, paymentStatus, invoiceModes, invoiceTypes, createdBy);

        int totalInvoices = 0;
        Double totalAmount = 0.0;
        Double paidAmount = 0.0;

        if (aggregates != null && !aggregates.isEmpty()) {
            Object[] row = aggregates.get(0);
            if (row[0] != null)
                totalInvoices = ((Number) row[0]).intValue();
            if (row[1] != null)
                totalAmount = ((Number) row[1]).doubleValue();
            if (row[2] != null)
                paidAmount = ((Number) row[2]).doubleValue();
        }
        Double outStandingAmount = totalAmount - paidAmount;

        int totalPages = (int) Math.ceil((double) totalInvoices / size);

        ReportDetailsResponse response = ReportDetailsResponse.builder()
                .totalInvoices(totalInvoices)
                .currentPage(page)
                .totalPages(totalPages)
                .totalAmount(totalAmount)
                .outStandingAmount(outStandingAmount)
                .paidAmount(paidAmount)
                .filterOptions(options)
                .invoiceList(invoiceDetails)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
