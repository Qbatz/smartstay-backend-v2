package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.RoomBedCount;
import com.smartstay.smartstay.dto.dashboard.BedsStatus;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.responses.dashboard.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private record DashboardDateRange(Date startDate, Date endDate) {
    }

    @Autowired
    private HostelService hostelService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private BookingsService bookingsService;
    @Autowired
    private InvoiceV1Service invoiceV1Service;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private Authentication authentication;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private AssetsService assetsService;
    @Autowired
    private ElectricityService electricityService;

    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private ExpenseCategoryService expenseCategoryService;
    @Autowired
    private ComplaintsService complaintsService;
    @Autowired
    private AmenityRequestService amenityRequestService;

    private static final List<String> DASHBOARD_FILTERS = Arrays.asList("Today", "This Week", "This Month",
            "Last Month", "Last 3 Months");

    public ResponseEntity<?> getDashboardInfo(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_DASHBOARD, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        BillingDates billingDates = hostelService.getCurrentBillStartAndEndDates(hostelId);
        int totalRooms = 0;
        Integer totalBeds = 0;
        Integer freebeds = 0;
        Integer occupiedBeds = 0;
        Integer totalCustomers = 0;
        Integer bookedBeds = 0;
        Double assetsValue = 0.0;
        Double advanceAmount = 0.0;
        Double electricityAmount = 0.0;
        Double nextMonthProjections = 0.0;
        Double currentMonthProfit = 0.0;
        Double otherProfit = 0.0;
        Integer pendingInvoiceCount = 0;

        BedsStatus bedsStatus = bedsService.getBedCountsForDashboard(hostelId);
        if (bedsStatus != null) {
            totalBeds = bedsStatus.totalBeds();
            freebeds = bedsStatus.freeBeds();
            occupiedBeds = bedsStatus.occupiedBeds();
            bookedBeds = bedsStatus.bookedBeds();
        }

        totalRooms = roomsService.getRoomCount(hostelId);
        totalCustomers = bookingsService.getAllCheckedInCustomersCount(hostelId);
        assetsValue = assetsService.getAllAssetsValue(hostelId);
        advanceAmount = customersService.getAdvanceAmountFromAllCustomers(hostelId);
        electricityAmount = electricityService.getPreviousMonthEbAmount(hostelId);
        nextMonthProjections = bookingsService.getNextMonthProjections(hostelId, billingDates);
        pendingInvoiceCount = invoiceV1Service.getPendingInvoiceCounts(hostelId);

        Dashboard dashboard = new Dashboard(
                hostelId,
                totalRooms,
                totalBeds,
                freebeds,
                occupiedBeds,
                bookedBeds,
                totalCustomers,
                assetsValue,
                advanceAmount,
                electricityAmount,
                nextMonthProjections,
                currentMonthProfit,
                otherProfit,
                pendingInvoiceCount);

        return new ResponseEntity<>(dashboard, HttpStatus.OK);
    }

    public ResponseEntity<?> getDashboardInfoNew(String hostelId, String billingFilter, String complaintRequestFilter,
            String financeFilter, String occupancyFilter) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_DASHBOARD, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        DashboardNew dashboardNew = new DashboardNew(
                buildRoomsAndBedInfo(hostelId),
                buildOccupancy(hostelId),
                buildTenantsSummary(hostelId),
                buildAdvanceSummary(hostelId),
                buildExpenseSummary(hostelId, financeFilter),
                buildOccupancyTrend(hostelId, occupancyFilter),
                buildBillingSummary(hostelId, billingFilter),
                buildTenantComplaints(hostelId, complaintRequestFilter),
                buildTenantRequests(hostelId, complaintRequestFilter),
                buildFinanceSummary(hostelId, financeFilter),
                buildRevenueSummary(hostelId, financeFilter),
                buildRevenueTrend(hostelId),
                buildRecentCheckins(hostelId),
                buildOverdueInvoices(hostelId),
                buildRecentComplaints(hostelId),
                buildRecentRequests(hostelId),
                DASHBOARD_FILTERS);

        return new ResponseEntity<>(dashboardNew, HttpStatus.OK);
    }

    private Occupancy buildOccupancy(String hostelId) {
        com.smartstay.smartstay.dto.dashboard.BedsStatus bedsStatus = bedsService.getBedCountsForDashboard(hostelId);
        Integer occupiedBeds = bedsStatus != null ? bedsStatus.occupiedBeds() : 0;
        Integer freeBeds = bedsStatus != null ? bedsStatus.freeBeds() : 0;
        Integer totalBeds = bedsStatus != null ? bedsStatus.totalBeds() : 1;
        String occupancyRate = Utils.roundOffWithTwoDigit((occupiedBeds * 100.0) / Math.max(1, totalBeds)) + "%";
        return new Occupancy(occupiedBeds, freeBeds, occupancyRate, "0%");
    }

    private TenantsSummary buildTenantsSummary(String hostelId) {
        Integer totalTenants = bookingsService.getAllCheckedInCustomersCount(hostelId);
        Integer checkInTenants = bookingsService.countByHostelIdAndCurrentStatus(hostelId, "CHECKIN");
        Integer noticePeriod = bookingsService.countByHostelIdAndCurrentStatus(hostelId, "NOTICE");
        Date nextCheckoutDate = bookingsService.findEarliestLeavingDate(hostelId, new Date());
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String nextCheckout = nextCheckoutDate != null ? sdf.format(nextCheckoutDate) : null;
        return new TenantsSummary(totalTenants, checkInTenants, noticePeriod, nextCheckout);
    }

    private AdvanceSummary buildAdvanceSummary(String hostelId) {
        Double totalAdvance = customersService.getAdvanceAmountFromAllCustomers(hostelId);
        return new AdvanceSummary(totalAdvance != null ? totalAdvance : 0.0, 0.0, 0.0);
    }

    private ExpenseSummary buildExpenseSummary(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter);
        List<ExpensesV1> expenses = expenseService.findByHostelIdAndDateRange(hostelId, dates.startDate(), dates.endDate());
        List<ExpenseCategory> categories = expenseCategoryService.findAllByHostelIdAndIsActiveTrue(hostelId);
        Map<Long, String> catMap = categories.stream().collect(Collectors.toMap(ExpenseCategory::getCategoryId, ExpenseCategory::getCategoryName));

        Map<Long, Double> grouped = expenses.stream()
                .filter(e -> e.getCategoryId() != null)
                .collect(Collectors.groupingBy(ExpensesV1::getCategoryId,
                        Collectors.summingDouble(e -> e.getTotalPrice() != null ? e.getTotalPrice() : 0.0)));

        Double totalAmount = grouped.values().stream().mapToDouble(Double::doubleValue).sum();
        final Double total = totalAmount; // For use in lambda

        List<ExpenseBreakdown> breakdown = grouped.entrySet().stream()
                .map(e -> {
                    String name = Utils.capitalize(catMap.getOrDefault(e.getKey(), "Unknown"));
                    Double amount = e.getValue();
                    Double percentage = total > 0 ? (amount / total) * 100 : 0.0;
                    return new ExpenseBreakdown(name, amount, Utils.roundOffWithTwoDigit(percentage));
                })
                .collect(Collectors.toList());

        return new ExpenseSummary(totalAmount, breakdown);
    }

    private RoomsAndBedInfo buildRoomsAndBedInfo(String hostelId) {
        int totalRooms = roomsService.getRoomCount(hostelId);
        int totalBeds = bedsService.countAllByHostelId(hostelId);

        List<Rooms> rooms = roomsService.findByHostelId(hostelId);
        List<RoomBedCount> totalBedsByRoom = bedsService.countBedsByRoomForHostel(hostelId);
        List<RoomBedCount> occupiedBedsByRoom = bedsService.countOccupiedBedsByRoomForHostel(hostelId);

        Map<Integer, Long> totalBedsMap = totalBedsByRoom.stream()
                .collect(Collectors.toMap(RoomBedCount::getRoomId, RoomBedCount::getBedCount));
        Map<Integer, Long> occupiedBedsMap = occupiedBedsByRoom.stream()
                .collect(Collectors.toMap(RoomBedCount::getRoomId, RoomBedCount::getBedCount));

        int filledRooms = 0;
        Map<Integer, Long> bedsByType = new HashMap<>();
        Map<Integer, Long> filledBedsByType = new HashMap<>();

        for (Rooms r : rooms) {
            int rId = r.getRoomId();
            Integer st = r.getSharingType();
            if (st == null)
                continue;

            long tBeds = totalBedsMap.getOrDefault(rId, 0L);
            long oBeds = occupiedBedsMap.getOrDefault(rId, 0L);

            if (tBeds > 0 && tBeds == oBeds) {
                filledRooms++;
            }

            bedsByType.put(st, bedsByType.getOrDefault(st, 0L) + tBeds);
            filledBedsByType.put(st, filledBedsByType.getOrDefault(st, 0L) + oBeds);
        }

        List<SharingInfo> sharingInfoList = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : bedsByType.entrySet()) {
            Integer type = entry.getKey();
            long typeTotalBeds = entry.getValue();
            long typeFilledBeds = filledBedsByType.getOrDefault(type, 0L);
            String typeName = Utils.capitalize((type == 1 ? "SINGLE" : type) + " SHARING");
            double occupancy = typeTotalBeds > 0 ? ((double) typeFilledBeds / typeTotalBeds) * 100 : 0.0;
            sharingInfoList.add(new SharingInfo(typeName, (int) typeTotalBeds, (int) typeFilledBeds,
                    Utils.roundOffWithTwoDigit(occupancy)));
        }

        return new RoomsAndBedInfo(totalRooms, filledRooms, totalBeds, sharingInfoList);
    }

    private List<OccupancyPoint> buildOccupancyTrend(String hostelId, String filter) {
        List<OccupancyPoint> trend = new ArrayList<>();
        DashboardDateRange dates = getDateRange(filter);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dates.startDate());

        int totalBeds = bedsService.countAllByHostelId(hostelId);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM");

        while (!cal.getTime().after(dates.endDate())) {
            Date date = cal.getTime();
            int occupied = bookingsService.countByStatusAndDate(hostelId, "CHECKIN", date) +
                    bookingsService.countByStatusAndDate(hostelId, "NOTICE", date);
            int booked = bookingsService.countBookedByDate(hostelId, date);
            int vacant = Math.max(0, totalBeds - occupied);

            trend.add(new OccupancyPoint(sdf.format(date), booked, occupied, vacant));
            cal.add(Calendar.DATE, 1);
            if (trend.size() > 90)
                break; // Maximum 90 days
        }
        return trend;
    }

    private BillingSummary buildBillingSummary(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter);
        Map<String, Object> summary = invoiceV1Service.getBillingSummary(hostelId, dates.startDate(),
                dates.endDate());
        Integer totalInvoiceGenerated = summary.get("totalInvoiceGenerated") != null
                ? ((Number) summary.get("totalInvoiceGenerated")).intValue()
                : 0;
        Double totalInvoiced = summary.get("totalInvoiced") != null
                ? ((Number) summary.get("totalInvoiced")).doubleValue()
                : 0.0;
        Double totalPaid = summary.get("totalPaid") != null ? ((Number) summary.get("totalPaid")).doubleValue() : 0.0;
        return new BillingSummary(totalInvoiceGenerated, Utils.roundOffWithTwoDigit(totalInvoiced),
                Utils.roundOffWithTwoDigit(totalPaid), Utils.roundOffWithTwoDigit(totalInvoiced - totalPaid));
    }

    private StatusSummary buildTenantComplaints(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter);
        Map<String, Object> summary = complaintsService.getComplaintStatusSummary(hostelId, dates.startDate(),
                dates.endDate());
        Integer total = summary != null && summary.get("total") != null ? ((Number) summary.get("total")).intValue() : 0;
        Integer pending = summary != null && summary.get("pending") != null ? ((Number) summary.get("pending")).intValue() : 0;
        Integer resolved = summary != null && summary.get("resolved") != null ? ((Number) summary.get("resolved")).intValue() : 0;
        Integer inProgress = summary != null && summary.get("inProgress") != null ? ((Number) summary.get("inProgress")).intValue() : 0;
        return new StatusSummary(total, inProgress, pending, resolved);
    }

    private StatusSummary buildTenantRequests(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter);
        Map<String, Object> summary = amenityRequestService.getRequestStatusSummary(hostelId, dates.startDate(),
                dates.endDate());
        Integer total = summary != null && summary.get("total") != null ? ((Number) summary.get("total")).intValue() : 0;
        Integer pending = summary != null && summary.get("pending") != null ? ((Number) summary.get("pending")).intValue() : 0;
        Integer resolved = summary != null && summary.get("resolved") != null ? ((Number) summary.get("resolved")).intValue() : 0;
        return new StatusSummary(total, 0, pending, resolved);
    }

    private FinanceSummary buildFinanceSummary(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter);
        Double income = invoiceV1Service.getTotalPaidAmount(hostelId, dates.startDate(), dates.endDate());
        Double expense = expenseService.sumAmountByHostelIdAndDateRange(hostelId, dates.startDate(),
                dates.endDate());
        income = income != null ? income : 0.0;
        expense = expense != null ? expense : 0.0;

        // Trending
        DashboardDateRange prevDates = getPreviousDateRange(dates, filter);
        Double prevIncome = invoiceV1Service.getTotalPaidAmount(hostelId, prevDates.startDate(), prevDates.endDate());
        Double prevExpense = expenseService.sumAmountByHostelIdAndDateRange(hostelId, prevDates.startDate(),
                prevDates.endDate());
        prevIncome = prevIncome != null ? prevIncome : 0.0;
        prevExpense = prevExpense != null ? prevExpense : 0.0;

        Integer incomeTrend = calculateTrend(income, prevIncome);
        Integer expenseTrend = calculateTrend(expense, prevExpense);

        return new FinanceSummary(income, incomeTrend, expense, expenseTrend, Utils.roundOffWithTwoDigit(income - expense));
    }

    private RevenueSummary buildRevenueSummary(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter);
        Map<String, Object> summary = invoiceV1Service.getBillingSummary(hostelId, dates.startDate(), dates.endDate());
        Double collected = summary.get("totalPaid") != null ? ((Number) summary.get("totalPaid")).doubleValue() : 0.0;
        Double totalInvoiced = summary.get("totalInvoiced") != null ? ((Number) summary.get("totalInvoiced")).doubleValue() : 0.0;
        Double outstanding = Math.max(0.0, totalInvoiced - collected);

        DashboardDateRange prevDates = getPreviousDateRange(dates, filter);
        Map<String, Object> prevSummary = invoiceV1Service.getBillingSummary(hostelId, prevDates.startDate(), prevDates.endDate());
        Double prevCollected = prevSummary.get("totalPaid") != null ? ((Number) prevSummary.get("totalPaid")).doubleValue() : 0.0;
        Double prevTotalInvoiced = prevSummary.get("totalInvoiced") != null ? ((Number) prevSummary.get("totalInvoiced")).doubleValue() : 0.0;
        Double prevOutstanding = Math.max(0.0, prevTotalInvoiced - prevCollected);

        return new RevenueSummary(
                new TrendValue(collected, calculateTrend(collected, prevCollected)),
                new TrendValue(outstanding, calculateTrend(outstanding, prevOutstanding)));
    }

    private List<RevenueTrendPoint> buildRevenueTrend(String hostelId) {
        List<RevenueTrendPoint> trend = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM");

        for (int i = 5; i >= 0; i--) {
            Calendar mCal = Calendar.getInstance();
            mCal.add(Calendar.MONTH, -i);
            mCal.set(Calendar.DAY_OF_MONTH, 1);
            Date start = mCal.getTime();
            mCal.set(Calendar.DAY_OF_MONTH, mCal.getActualMaximum(Calendar.DAY_OF_MONTH));
            Date end = mCal.getTime();

            Map<String, Object> summary = invoiceV1Service.getBillingSummary(hostelId, start, end);
            Double collected = summary.get("totalPaid") != null ? ((Number) summary.get("totalPaid")).doubleValue() : 0.0;
            Double totalInvoiced = summary.get("totalInvoiced") != null ? ((Number) summary.get("totalInvoiced")).doubleValue() : 0.0;
            Double outstanding = Math.max(0.0, totalInvoiced - collected);

            trend.add(new RevenueTrendPoint(sdf.format(start), collected, outstanding));
        }
        return trend;
    }

    private DashboardDateRange getPreviousDateRange(DashboardDateRange current, String filter) {
        Calendar start = Calendar.getInstance();
        start.setTime(current.startDate());
        Calendar end = Calendar.getInstance();
        end.setTime(current.endDate());

        switch (filter) {
            case "Today":
                start.add(Calendar.DATE, -1);
                end.add(Calendar.DATE, -1);
                break;
            case "This Week":
                start.add(Calendar.WEEK_OF_YEAR, -1);
                end.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "This Month":
                start.add(Calendar.MONTH, -1);
                end.add(Calendar.MONTH, -1);
                break;
            case "Last Month":
                start.add(Calendar.MONTH, -1);
                end.add(Calendar.MONTH, -1);
                break;
            case "Last 3 Months":
                start.add(Calendar.MONTH, -3);
                end.add(Calendar.MONTH, -3);
                break;
            default:
                start.add(Calendar.DATE, -7);
                end.add(Calendar.DATE, -7);
                break;
        }
        return new DashboardDateRange(start.getTime(), end.getTime());
    }

    private Integer calculateTrend(Double current, Double previous) {
        if (previous == null || previous == 0.0) {
            return current > 0 ? 100 : 0;
        }
        double change = ((current - previous) / previous) * 100;
        return (int) Math.round(change);
    }

    private List<RecentCheckin> buildRecentCheckins(String hostelId) {
        List<BookingsV1> bookings = bookingsService.findTopCheckins(hostelId, PageRequest.of(0, 5));
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        return bookings.stream().map(b -> {
            Customers c = customersService.getCustomerInformation(b.getCustomerId());
            Rooms r = (b.getRoomId() > 0) ? roomsService.findRoomByRoomId(b.getRoomId()) : null;
            Beds bed = (b.getBedId() > 0) ? bedsService.findBedById(b.getBedId()) : null;

            String customerName = c != null ? (c.getFirstName() + (c.getLastName() != null ? " " + c.getLastName() : "")) : null;
            if (customerName != null) customerName = Utils.capitalize(customerName);
            
            String profilePic = c != null ? c.getProfilePic() : null;
            
            String roomName = r != null ? r.getRoomName() : null;
            if (roomName != null) roomName = Utils.capitalize(roomName);
            
            String sharingType = r != null ? Utils.capitalize((r.getSharingType() != null && r.getSharingType() == 1 ? "SINGLE" : r.getSharingType()) + " SHARING") : null;
            
            String bedName = bed != null ? bed.getBedName() : null;
            if (bedName != null) bedName = Utils.capitalize(bedName);
            
            String joiningDate = b.getJoiningDate() != null ? sdf.format(b.getJoiningDate()) : null;
            String status = b.getCurrentStatus() != null ? Utils.capitalize(b.getCurrentStatus()) : null;

            return new RecentCheckin(customerName, profilePic, roomName, sharingType, bedName, joiningDate, status);
        }).collect(Collectors.toList());
    }

    private List<OverdueInvoice> buildOverdueInvoices(String hostelId) {
        List<InvoicesV1> invoices = invoiceV1Service.findTopOverdueInvoices(hostelId,
                PageRequest.of(0, 5));
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        return invoices.stream().map(inv -> {
            Customers c = customersService.getCustomerInformation(inv.getCustomerId());
            String customerName = c != null ? (c.getFirstName() + (c.getLastName() != null ? " " + c.getLastName() : "")) : null;
            if (customerName != null) customerName = Utils.capitalize(customerName);
            
            Double paidAmount = inv.getPaidAmount() != null ? inv.getPaidAmount() : 0.0;
            Double totalAmount = inv.getTotalAmount() != null ? inv.getTotalAmount() : 0.0;
            String dueDate = inv.getInvoiceDueDate() != null ? sdf.format(inv.getInvoiceDueDate()) : null;
            String status = inv.getPaymentStatus() != null ? Utils.capitalize(inv.getPaymentStatus()) : null;

            return new OverdueInvoice(inv.getInvoiceId(), inv.getInvoiceNumber(), customerName, totalAmount, paidAmount, dueDate,
                    status);
        }).collect(Collectors.toList());
    }

    private List<RecentComplaint> buildRecentComplaints(String hostelId) {
        List<ComplaintsV1> complaints = complaintsService.findTopComplaints(hostelId,
                PageRequest.of(0, 5));
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        return complaints.stream().map(comp -> {
            Customers c = customersService.getCustomerInformation(comp.getCustomerId());
            String customerName = c != null ? (c.getFirstName() + (c.getLastName() != null ? " " + c.getLastName() : "")) : null;
            if (customerName != null) customerName = Utils.capitalize(customerName);
            
            String profilePic = c != null ? c.getProfilePic() : null;
            String complaintType = Utils.capitalize("COMPLAINT");
            String status = comp.getStatus() != null ? Utils.capitalize(comp.getStatus()) : null;
            String date = comp.getCreatedAt() != null ? sdf.format(comp.getCreatedAt()) : null;

            return new RecentComplaint(
                    comp.getComplaintId(),
                    customerName,
                    profilePic,
                    complaintType,
                    status,
                    date,
                    comp.getDescription());
        }).collect(Collectors.toList());
    }

    private List<RecentRequest> buildRecentRequests(String hostelId) {
        List<AmenityRequest> requests = amenityRequestService.findTopRequests(hostelId,
                PageRequest.of(0, 5));
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        return requests.stream().map(req -> {
            Customers c = customersService.getCustomerInformation(req.getCustomerId());
            String customerName = c != null ? (c.getFirstName() + (c.getLastName() != null ? " " + c.getLastName() : "")) : null;
            if (customerName != null) customerName = Utils.capitalize(customerName);
            
            String profilePic = c != null ? c.getProfilePic() : null;
            String requestType = Utils.capitalize("AMENITY REQUEST");
            String status = req.getCurrentStatus() != null ? Utils.capitalize(req.getCurrentStatus()) : null;
            String date = req.getCreatedAt() != null ? sdf.format(req.getCreatedAt()) : null;

            return new RecentRequest(
                    req.getAmenityRequestId(),
                    customerName,
                    profilePic,
                    requestType,
                    status,
                    date);
        }).collect(Collectors.toList());
    }

    private DashboardDateRange getDateRange(String filter) {
        Calendar cal = Calendar.getInstance();
        Date endDate = cal.getTime();
        Date startDate;

        switch (filter) {
            case "Today":
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                startDate = cal.getTime();
                break;
            case "This Week":
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                startDate = cal.getTime();
                break;
            case "This Month":
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = cal.getTime();
                break;
            case "Last Month":
                cal.add(Calendar.MONTH, -1);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = cal.getTime();
                break;
            case "Last 3 Months":
                cal.add(Calendar.MONTH, -3);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = cal.getTime();
                break;
            default: // Default 7 days
                cal.add(Calendar.DAY_OF_MONTH, -7);
                startDate = cal.getTime();
                break;
        }
        return new DashboardDateRange(startDate, endDate);
    }
}
