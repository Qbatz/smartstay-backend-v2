package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.ComplaintStatus;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.ennum.RequestStatus;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.responses.Reports.ReportResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ReportService {

    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;

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

    public ResponseEntity<?> getReports(String hostelId) {
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

        // Invoices for hostel
        int invoiceCount = invoicesRepository.countByHostelId(hostelId);
        Double invoiceTotal = invoicesRepository.sumTotalAmountByHostelId(hostelId);
        ReportResponse.InvoiceReport invoiceReport = ReportResponse.InvoiceReport.builder()
                .noOfInvoices(invoiceCount)
                .totalAmount(invoiceTotal)
                .build();

        // Receipts
        int receiptCount = transactionRepository.countByHostelId(hostelId);
        Double receiptTotal = transactionRepository.sumPaidAmountByHostelId(hostelId);
        ReportResponse.ReceiptReport receiptReport = ReportResponse.ReceiptReport.builder()
                .totalReceipts(receiptCount)
                .totalAmount(receiptTotal)
                .build();

        // Banking
        int bankTransCount = bankTransactionRepository.countByHostelId(hostelId);
        Double bankBalance = bankingRepository.sumBalanceByHostelId(hostelId);
        ReportResponse.BankingReport bankingReport = ReportResponse.BankingReport.builder()
                .totalTransactions(bankTransCount)
                .totalAmount(bankBalance)
                .build();

        // Tenants
        int tenantCount = customersRepository.countByHostelIdAndStatusIn(hostelId, Arrays.asList(CustomerStatus.CHECK_IN.name(), CustomerStatus.NOTICE.name()));
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
        int expenseCount = expensesRepository.countByHostelId(hostelId);
        Double expenseTotal = expensesRepository.sumAmountByHostelId(hostelId);
        ReportResponse.ExpenseReport expenseReport = ReportResponse.ExpenseReport.builder()
                .totalExpenses(expenseCount)
                .totalExpenseAmount(expenseTotal)
                .build();

        // Vendor
        int vendorCount = vendorRepository.countByHostelId(hostelId);
        ReportResponse.VendorReport vendorReport = ReportResponse.VendorReport.builder()
                .totalVendors(vendorCount)
                .build();

        // Complaints
        int complaintCount = complaintRepository.countByHostelId(hostelId);
        int complaintActiveCount = complaintRepository.countActiveByHostelId(hostelId,
                Arrays.asList(ComplaintStatus.PENDING.name(), ComplaintStatus.OPENED.name()));
        ReportResponse.ComplaintReport complaintReport = ReportResponse.ComplaintReport.builder()
                .totalComplaints(complaintCount)
                .activeComplaints(complaintActiveCount)
                .build();

        // Requests
        int requestCount = amenityRequestRepository.countByHostelId(hostelId);
        int requestActiveCount = amenityRequestRepository.countActiveByHostelId(hostelId,
                Arrays.asList(RequestStatus.PENDING.name(), RequestStatus.OPEN.name(),RequestStatus.INPROGRESS.name()));
        ReportResponse.RequestReport requestReport = ReportResponse.RequestReport.builder()
                .totalRequests(requestCount)
                .activeRequests(requestActiveCount)
                .build();

        ReportResponse response = ReportResponse.builder()
                .hostelId(hostelId)
                .startDate(null)
                .endDate(null)
                .invoices(invoiceReport)
                .receipts(receiptReport)
                .banking(bankingReport)
                .tenantInfo(tenantReport)
                .expense(expenseReport)
                .vendor(vendorReport)
                .complaints(complaintReport)
                .requests(requestReport)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
