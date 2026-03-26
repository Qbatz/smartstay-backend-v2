package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.beds.RoomBedCount;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.dto.dashboard.BedsStatus;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.BookingStatus;
import com.smartstay.smartstay.repositories.BedChangeRequestRepository;
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
    @Autowired
    private BedChangeRequestRepository bedChangeRequestRepository;
    @Autowired
    private ComplaintTypeService complaintTypeService;

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
                buildOccupancyTrendSummary(hostelId, occupancyFilter),
                buildBillingSummary(hostelId, billingFilter),
                buildTenantComplaints(hostelId, complaintRequestFilter),
                buildTenantRequests(hostelId, complaintRequestFilter),
                buildFinanceSummary(hostelId, financeFilter),
                buildRevenueSummary(hostelId, financeFilter),
                buildRevenueTrend(hostelId),
                buildRecentCheckins(hostelId),
                buildOverdueInvoices(hostelId),
                buildRecentActivities(hostelId, complaintRequestFilter),
                buildTenantComplaintList(hostelId),
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String nextCheckout = nextCheckoutDate != null ? sdf.format(nextCheckoutDate) : null;
        return new TenantsSummary(totalTenants, checkInTenants, noticePeriod, nextCheckout);
    }

    private AdvanceSummary buildAdvanceSummary(String hostelId) {

        Double totalAdvance = invoiceV1Service.getTotalAdvanceAmount(hostelId);
        if (totalAdvance == null) totalAdvance = 0.0;


        Double advanceHolding = invoiceV1Service.getAdvanceHoldingAmount(hostelId);
        if (advanceHolding == null) advanceHolding = 0.0;


        List<Advance> advances = customersService.getAdvancesForHostel(hostelId);
        double otherDeduction = 0.0;
        if (advances != null) {
            for (Advance adv : advances) {
                if (adv.getDeductions() != null) {
                    for (Deductions d : adv.getDeductions()) {
                        if (d.getAmount() != null) {
                            otherDeduction += d.getAmount();
                        }
                    }
                }
            }
        }

        return new AdvanceSummary(totalAdvance, advanceHolding, otherDeduction);
    }

    private ExpenseSummary buildExpenseSummary(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter, hostelId);
        List<ExpensesV1> expenses = expenseService.findByHostelIdAndDateRange(hostelId, dates.startDate(), dates.endDate());
        List<ExpenseCategory> categories = expenseCategoryService.findAllByHostelIdAndIsActiveTrue(hostelId);
        Map<Long, String> catMap = categories.stream().collect(Collectors.toMap(ExpenseCategory::getCategoryId, ExpenseCategory::getCategoryName));

        Map<Long, Double> grouped = expenses.stream()
                .filter(e -> e.getCategoryId() != null)
                .collect(Collectors.groupingBy(ExpensesV1::getCategoryId,
                        Collectors.summingDouble(e -> e.getTotalPrice() != null ? e.getTotalPrice() : 0.0)));

        Double totalAmount = grouped.values().stream().mapToDouble(Double::doubleValue).sum();
        final Double total = totalAmount;

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
        int totalRoomsCount = roomsService.getRoomCount(hostelId);
        int totalBedsCount = bedsService.countAllByHostelId(hostelId);

        List<Rooms> rooms = roomsService.findByHostelId(hostelId);
        List<RoomBedCount> totalBedsByRoom = bedsService.countBedsByRoomForHostel(hostelId);
        List<RoomBedCount> occupiedBedsByRoom = bedsService.countOccupiedBedsByRoomForHostel(hostelId);

        Map<Integer, Long> totalBedsMap = totalBedsByRoom.stream()
                .collect(Collectors.toMap(RoomBedCount::getRoomId, RoomBedCount::getBedCount));
        Map<Integer, Long> occupiedBedsMap = occupiedBedsByRoom.stream()
                .collect(Collectors.toMap(RoomBedCount::getRoomId, RoomBedCount::getBedCount));

        int filledRooms = 0;
        int occupiedBedsTotal = 0;

        // shareType -> stats
        Map<Integer, Integer> typeTotalRooms = new HashMap<>();
        Map<Integer, Integer> typeAvailableRooms = new HashMap<>();
        Map<Integer, Long> typeTotalBeds = new HashMap<>();
        Map<Integer, Long> typeOccupiedBeds = new HashMap<>();

        for (Rooms r : rooms) {
            int rId = r.getRoomId();
            Integer st = r.getSharingType();
            if (st == null) st = 0;

            long tBeds = totalBedsMap.getOrDefault(rId, 0L);
            long oBeds = occupiedBedsMap.getOrDefault(rId, 0L);

            typeTotalRooms.put(st, typeTotalRooms.getOrDefault(st, 0) + 1);
            typeTotalBeds.put(st, typeTotalBeds.getOrDefault(st, 0L) + tBeds);
            typeOccupiedBeds.put(st, typeOccupiedBeds.getOrDefault(st, 0L) + oBeds);
            occupiedBedsTotal += (int) oBeds;

            if (tBeds > 0) {
                if (tBeds == oBeds) {
                    filledRooms++;
                } else {
                    typeAvailableRooms.put(st, typeAvailableRooms.getOrDefault(st, 0) + 1);
                }
            }
        }

        List<SharingInfo> sharingInfoList = new ArrayList<>();
        List<Integer> sortedTypes = typeTotalRooms.keySet().stream().sorted().collect(Collectors.toList());

        for (Integer type : sortedTypes) {
            int tRooms = typeTotalRooms.getOrDefault(type, 0);
            int aRooms = typeAvailableRooms.getOrDefault(type, 0);
            long tBeds = typeTotalBeds.getOrDefault(type, 0L);
            long oBeds = typeOccupiedBeds.getOrDefault(type, 0L);
            long aBeds = Math.max(0L, tBeds - oBeds);

            String typeName = Utils.capitalize((type == 0 ? "0" : (type == 1 ? "Single" : type)) + " Sharing");
            double occupancy = tBeds > 0 ? ((double) oBeds / tBeds) * 100 : 0.0;

            sharingInfoList.add(new SharingInfo(typeName, tRooms, aRooms, (int) tBeds, (int) oBeds, (int) aBeds,
                    Utils.roundOffWithTwoDigit(occupancy)));
        }

        int availableRooms = totalRoomsCount - filledRooms;
        int availableBeds = totalBedsCount - occupiedBedsTotal;

        return new RoomsAndBedInfo(totalRoomsCount, filledRooms, availableRooms, totalBedsCount, occupiedBedsTotal, availableBeds, sharingInfoList);
    }

    private OccupancyTrendSummary buildOccupancyTrendSummary(String hostelId, String filter) {
        List<OccupancyPoint> trend = buildOccupancyTrend(hostelId, filter);
        
        double avgOccupied = trend.stream().mapToInt(OccupancyPoint::occupied).average().orElse(0.0);
        double avgVacant = trend.stream().mapToInt(OccupancyPoint::vacant).average().orElse(0.0);
        
        return new OccupancyTrendSummary(
            Utils.roundOffWithTwoDigit(avgOccupied),
            Utils.roundOffWithTwoDigit(avgVacant),
            trend
        );
    }

    private List<OccupancyPoint> buildOccupancyTrend(String hostelId, String filter) {
        List<OccupancyPoint> trend = new ArrayList<>();
        DashboardDateRange dates = getDateRange(filter, hostelId);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dates.startDate());

        int totalBeds = bedsService.countAllByHostelId(hostelId);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");

        while (!cal.getTime().after(dates.endDate())) {
            Date date = cal.getTime();
            int occupied = bookingsService.countByStatusAndDate(hostelId, "CHECKIN", date) +
                    bookingsService.countByStatusAndDate(hostelId, "NOTICE", date);
            int booked = bookingsService.countBookedByDate(hostelId, date);
            int vacant = Math.max(0, totalBeds - occupied);

            trend.add(new OccupancyPoint(sdf.format(date), booked, occupied, vacant));
            cal.add(Calendar.DATE, 1);
            if (trend.size() > 90)
                break;
        }
        return trend;
    }

    private BillingSummary buildBillingSummary(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter, hostelId);
        Map<String, Object> summary = invoiceV1Service.getBillingSummary(hostelId, dates.startDate(),
                dates.endDate());
        Integer totalInvoiceGenerated = summary.get("totalInvoiceGenerated") != null
                ? ((Number) summary.get("totalInvoiceGenerated")).intValue()
                : 0;
        Double totalInvoiced = summary.get("totalInvoiced") != null
                ? ((Number) summary.get("totalInvoiced")).doubleValue()
                : 0.0;
        Double totalPaid = summary.get("totalPaid") != null
                ? ((Number) summary.get("totalPaid")).doubleValue()
                : 0.0;
        double totalPending = totalInvoiced - totalPaid;

        Double refundedAmount = invoiceV1Service.getRefundedAmount(hostelId, dates.startDate(), dates.endDate());
        if (refundedAmount == null) refundedAmount = 0.0;

        String collectionRate = totalInvoiced > 0 
            ? Utils.roundOffWithTwoDigit((totalPaid / totalInvoiced) * 100) + "%" 
            : "0%";

        DashboardDateRange prevDates = getPreviousDateRange(dates, filter);
        Map<String, Object> prevSummary = invoiceV1Service.getBillingSummary(hostelId, prevDates.startDate(), prevDates.endDate());
        Double prevTotalInvoiced = prevSummary.get("totalInvoiced") != null 
            ? ((Number) prevSummary.get("totalInvoiced")).doubleValue() 
            : 0.0;

        String fromLastMonth = calculateTrend(totalInvoiced, prevTotalInvoiced) + "%";

        return new BillingSummary(
                totalInvoiceGenerated, 
                Utils.roundOffWithTwoDigit(totalInvoiced),
                Utils.roundOffWithTwoDigit(totalPaid), 
                Utils.roundOffWithTwoDigit(totalPending),
                Utils.roundOffWithTwoDigit(refundedAmount),
                collectionRate,
                fromLastMonth);
    }

    private StatusSummary buildTenantComplaints(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter, hostelId);
        Map<String, Object> summary = complaintsService.getComplaintStatusSummary(hostelId, dates.startDate(),
                dates.endDate());
        Integer total = summary != null && summary.get("total") != null ? ((Number) summary.get("total")).intValue() : 0;
        Integer pending = summary != null && summary.get("pending") != null ? ((Number) summary.get("pending")).intValue() : 0;
        Integer resolved = summary != null && summary.get("resolved") != null ? ((Number) summary.get("resolved")).intValue() : 0;
        Integer inProgress = summary != null && summary.get("inProgress") != null ? ((Number) summary.get("inProgress")).intValue() : 0;
        return new StatusSummary(total, inProgress, pending, resolved);
    }

    private StatusSummary buildTenantRequests(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter, hostelId);

        Map<String, Object> amenitySummary = amenityRequestService.getRequestStatusSummary(hostelId, dates.startDate(), dates.endDate());
        Map<String, Object> bedChangeSummary = bedChangeRequestRepository.getRequestStatusSummary(hostelId, dates.startDate(), dates.endDate());

        int total = 0, pending = 0, resolved = 0, inProgress = 0;

        if (amenitySummary != null) {
            total += amenitySummary.get("total") != null ? ((Number) amenitySummary.get("total")).intValue() : 0;
            pending += amenitySummary.get("pending") != null ? ((Number) amenitySummary.get("pending")).intValue() : 0;
            resolved += amenitySummary.get("resolved") != null ? ((Number) amenitySummary.get("resolved")).intValue() : 0;
            inProgress += amenitySummary.get("inProgress") != null ? ((Number) amenitySummary.get("inProgress")).intValue() : 0;
        }

        if (bedChangeSummary != null) {
            total += bedChangeSummary.get("total") != null ? ((Number) bedChangeSummary.get("total")).intValue() : 0;
            pending += bedChangeSummary.get("pending") != null ? ((Number) bedChangeSummary.get("pending")).intValue() : 0;
            resolved += bedChangeSummary.get("resolved") != null ? ((Number) bedChangeSummary.get("resolved")).intValue() : 0;
            inProgress += bedChangeSummary.get("inProgress") != null ? ((Number) bedChangeSummary.get("inProgress")).intValue() : 0;
        }

        return new StatusSummary(total, inProgress, pending, resolved);
    }

    private FinanceSummary buildFinanceSummary(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter, hostelId);
        Double income = invoiceV1Service.getTotalPaidAmountIncludePartial(hostelId, dates.startDate(), dates.endDate());
        Double expense = expenseService.sumAmountByHostelIdAndDateRange(hostelId, dates.startDate(),
                dates.endDate());
        income = income != null ? income : 0.0;
        expense = expense != null ? expense : 0.0;

        Double refunded = invoiceV1Service.getRefundedAmount(hostelId, dates.startDate(), dates.endDate());
        if (refunded == null) refunded = 0.0;

        // Trending
        DashboardDateRange prevDates = getPreviousDateRange(dates, filter);
        Double prevIncome = invoiceV1Service.getTotalPaidAmountIncludePartial(hostelId, prevDates.startDate(), prevDates.endDate());
        Double prevExpense = expenseService.sumAmountByHostelIdAndDateRange(hostelId, prevDates.startDate(),
                prevDates.endDate());
        prevIncome = prevIncome != null ? prevIncome : 0.0;
        prevExpense = prevExpense != null ? prevExpense : 0.0;

        Double prevRefunded = invoiceV1Service.getRefundedAmount(hostelId, prevDates.startDate(), prevDates.endDate());
        if (prevRefunded == null) prevRefunded = 0.0;

        Integer incomeTrend = calculateTrend(income, prevIncome);
        Integer expenseTrend = calculateTrend(expense, prevExpense);
        Double netProfit = Utils.roundOffWithTwoDigit(income - expense - refunded);
        Double prevNetProfit = prevIncome - prevExpense - prevRefunded;
        Integer profitTrend = calculateTrend(netProfit, prevNetProfit);

        return new FinanceSummary(income, incomeTrend, expense, expenseTrend, netProfit, profitTrend);
    }

    private RevenueSummary buildRevenueSummary(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter, hostelId);
        Map<String, Object> summary = invoiceV1Service.getBillingSummary(hostelId, dates.startDate(), dates.endDate());

        Double collected = invoiceV1Service.getTotalPaidAmountIncludePartial(hostelId, dates.startDate(), dates.endDate());
        if (collected == null) collected = 0.0;
        Double totalInvoiced = summary.get("totalInvoiced") != null ? ((Number) summary.get("totalInvoiced")).doubleValue() : 0.0;
        Double outstanding = Math.max(0.0, totalInvoiced - collected);

        DashboardDateRange prevDates = getPreviousDateRange(dates, filter);
        Map<String, Object> prevSummary = invoiceV1Service.getBillingSummary(hostelId, prevDates.startDate(), prevDates.endDate());

        Double prevCollected = invoiceV1Service.getTotalPaidAmountIncludePartial(hostelId, prevDates.startDate(), prevDates.endDate());
        if (prevCollected == null) prevCollected = 0.0;
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

            Double collected = invoiceV1Service.getTotalPaidAmountIncludePartial(hostelId, start, end);
            if (collected == null) collected = 0.0;
            Double totalInvoiced = summary.get("totalInvoiced") != null ? ((Number) summary.get("totalInvoiced")).doubleValue() : 0.0;
            Double outstanding = Math.max(0.0, totalInvoiced - collected);

            trend.add(new RevenueTrendPoint(sdf.format(start), collected, outstanding));
        }
        return trend;
    }

    private DashboardDateRange getPreviousDateRange(DashboardDateRange current, String filter) {
        // For billing-cycle-based filters, walk one cycle before current.startDate()
        Calendar prevCal = Calendar.getInstance();
        prevCal.setTime(current.startDate());
        prevCal.add(Calendar.DAY_OF_MONTH, -1); // one day before current start = end of previous period

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
            case "Last Month": {
                // Previous billing cycle: one cycle before the supplied start date
                long cycleLength = current.endDate().getTime() - current.startDate().getTime();
                end.setTime(new Date(current.startDate().getTime() - 1));
                start.setTime(new Date(end.getTime().getTime() - cycleLength));
                break;
            }
            case "Last 3 Months": {
                // Previous 3 billing cycles: shift the entire window back by the same duration
                long windowLength = current.endDate().getTime() - current.startDate().getTime();
                end.setTime(new Date(current.startDate().getTime() - 1));
                start.setTime(new Date(end.getTime().getTime() - windowLength));
                break;
            }
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        return bookings.stream().map(b -> {
            Customers c = customersService.getCustomerInformation(b.getCustomerId());
            Rooms r = (b.getRoomId() > 0) ? roomsService.findRoomByRoomId(b.getRoomId()) : null;
            Beds bed = (b.getBedId() > 0) ? bedsService.findBedById(b.getBedId()) : null;

            String customerName = c != null ? (c.getFirstName() + (c.getLastName() != null ? " " + c.getLastName() : "")) : null;
            if (customerName != null) customerName = Utils.capitalize(customerName);
            
            String profilePic = c != null ? c.getProfilePic() : null;
            String tenantId = c != null ? c.getCustomerId() : null;
            String initials = c != null ? Utils.getInitials(c.getFirstName(), c.getLastName()) : null;

            String roomName = r != null ? r.getRoomName() : null;
            if (roomName != null) roomName = Utils.capitalize(roomName);
            
            String sharingType = r != null ? Utils.capitalize((r.getSharingType() != null && r.getSharingType() == 1 ? "SINGLE" : r.getSharingType()) + " SHARING") : null;
            
            String bedName = bed != null ? bed.getBedName() : null;
            if (bedName != null) bedName = Utils.capitalize(bedName);
            
            String joiningDate = b.getExpectedJoiningDate() != null ? sdf.format(b.getExpectedJoiningDate()) : null;
            String status = b.getCurrentStatus() != null ? Utils.capitalize(b.getCurrentStatus()) : null;

            if (b.getCurrentStatus().equalsIgnoreCase(BookingStatus.BOOKED.name())) {
                joiningDate = sdf.format(b.getExpectedJoiningDate());
            }

            return new RecentCheckin(tenantId,initials,customerName, profilePic, roomName, sharingType, bedName, joiningDate, status);
        }).collect(Collectors.toList());
    }

    private List<OverdueInvoice> buildOverdueInvoices(String hostelId) {
        List<InvoicesV1> invoices = invoiceV1Service.findTopOverdueInvoices(hostelId,
                PageRequest.of(0, 5));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        return invoices.stream().map(inv -> {
            Customers c = customersService.getCustomerInformation(inv.getCustomerId());
            String customerName = c != null ? (c.getFirstName() + (c.getLastName() != null ? " " + c.getLastName() : "")) : null;
            if (customerName != null) customerName = Utils.capitalize(customerName);
            
            String profilePic = c != null ? c.getProfilePic() : null;
            String initials = c != null ? Utils.getInitials(c.getFirstName(), c.getLastName()) : null;
            
            Double paidAmount = inv.getPaidAmount() != null ? inv.getPaidAmount() : 0.0;
            Double totalAmount = inv.getTotalAmount() != null ? inv.getTotalAmount() : 0.0;
            String dueDate = inv.getInvoiceDueDate() != null ? sdf.format(inv.getInvoiceDueDate()) : null;
            String status = inv.getPaymentStatus() != null ? Utils.capitalize(inv.getPaymentStatus()) : null;
            double balanceDue = totalAmount - paidAmount;

            return new OverdueInvoice(inv.getInvoiceId(),
                    inv.getInvoiceNumber(),
                    customerName,
                    inv.getCustomerId(),
                    profilePic,
                    initials,
                    totalAmount,
                    paidAmount,
                    dueDate,
                    Utils.dateToString(inv.getInvoiceStartDate()),
                    balanceDue,
                    status);
        }).collect(Collectors.toList());
    }

    private List<DashboardRequest> buildRecentActivities(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter, hostelId);
        List<DashboardRequest> activities = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Fetch Amenity Requests
        List<AmenityRequest> requests = amenityRequestService.findTopRequestsByDate(hostelId, dates.startDate(), dates.endDate(), PageRequest.of(0, 5));
        for (AmenityRequest r : requests) {
            Customers cus = customersService.getCustomerInformation(r.getCustomerId());
            BookingsV1 booking = (cus != null) ? bookingsService.getBookingsByCustomerId(cus.getCustomerId()) : null;
            Rooms rm = (booking != null && booking.getRoomId() > 0) ? roomsService.findRoomByRoomId(booking.getRoomId()) : null;
            String roomName = (rm != null) ? rm.getRoomName() : null;

            activities.add(new DashboardRequest(
                r.getAmenityRequestId(),
                cus != null ? (cus.getFirstName() + (cus.getLastName() != null ? " " + cus.getLastName() : "")) : "Unknown",
                cus != null ? Utils.getInitials(cus.getFirstName(), cus.getLastName()) : null,
                cus != null ? cus.getProfilePic() : null,
                "Amenity Request",
                r.getCurrentStatus(),
                r.getCreatedAt() != null ? sdf.format(r.getCreatedAt()) : null,
                r.getDescription(),
                roomName
            ));
        }

        // Fetch Bed Change Requests
        List<BedChangeRequest> bedChanges = bedChangeRequestRepository.findTopRequestsByDate(hostelId, dates.startDate(), dates.endDate(), PageRequest.of(0, 5));
        for (BedChangeRequest b : bedChanges) {
            Customers cus = customersService.getCustomerInformation(b.getCustomerId());
            Rooms rm = (b.getRoomId() != null && b.getRoomId() > 0) ? roomsService.findRoomByRoomId(b.getRoomId()) : null;
            String roomName = (rm != null) ? rm.getRoomName() : null;

            activities.add(new DashboardRequest(
                b.getId(),
                cus != null ? (cus.getFirstName() + (cus.getLastName() != null ? " " + cus.getLastName() : "")) : "Unknown",
                cus != null ? Utils.getInitials(cus.getFirstName(), cus.getLastName()) : null,
                cus != null ? cus.getProfilePic() : null,
                "Bed Change",
                b.getCurrentStatus(),
                b.getCreatedAt() != null ? sdf.format(b.getCreatedAt()) : null,
                b.getReason(),
                roomName
            ));
        }

        return activities.stream()
            .sorted((a, b1) -> {
                try {
                    if (a.date() == null) return 1;
                    if (b1.date() == null) return -1;
                    return sdf.parse(b1.date()).compareTo(sdf.parse(a.date()));
                } catch (Exception e) {
                    return 0;
                }
            })
            .limit(5)
            .collect(Collectors.toList());
    }

    private DashboardDateRange getDateRange(String filter, String hostelId) {
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
            case "This Month": {
                BillingDates bd = hostelService.getCurrentBillStartAndEndDates(hostelId);
                startDate = bd.currentBillStartDate();
                endDate = bd.currentBillEndDate();
                break;
            }
            case "Last Month": {

                BillingDates currentBd = hostelService.getCurrentBillStartAndEndDates(hostelId);

                Calendar prevCycleCal = Calendar.getInstance();
                prevCycleCal.setTime(currentBd.currentBillStartDate());
                prevCycleCal.add(Calendar.DAY_OF_MONTH, -1);
                BillingDates prevBd = hostelService.getBillStartAndEndDateBasedOnDate(hostelId, prevCycleCal.getTime());
                startDate = prevBd.currentBillStartDate();
                endDate = prevBd.currentBillEndDate();
                break;
            }
            case "Last 3 Months": {
                BillingDates currentBd = hostelService.getCurrentBillStartAndEndDates(hostelId);

                Calendar walkBackCal = Calendar.getInstance();
                walkBackCal.setTime(currentBd.currentBillStartDate());
                walkBackCal.add(Calendar.DAY_OF_MONTH, -1);
                BillingDates cycle1 = hostelService.getBillStartAndEndDateBasedOnDate(hostelId, walkBackCal.getTime());
                walkBackCal.setTime(cycle1.currentBillStartDate());
                walkBackCal.add(Calendar.DAY_OF_MONTH, -1);
                BillingDates cycle2 = hostelService.getBillStartAndEndDateBasedOnDate(hostelId, walkBackCal.getTime());
                walkBackCal.setTime(cycle2.currentBillStartDate());
                walkBackCal.add(Calendar.DAY_OF_MONTH, -1);
                BillingDates cycle3 = hostelService.getBillStartAndEndDateBasedOnDate(hostelId, walkBackCal.getTime());
                startDate = cycle3.currentBillStartDate();
                endDate = currentBd.currentBillEndDate();
                break;
            }
            default: // Default 7 days
                cal.add(Calendar.DAY_OF_MONTH, -7);
                startDate = cal.getTime();
                break;
        }
        return new DashboardDateRange(startDate, endDate);
    }

    private List<TenantComplaint> buildTenantComplaintList(String hostelId) {
        List<ComplaintsV1> complaints = complaintsService.findTopComplaints(hostelId, PageRequest.of(0, 5));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        return complaints.stream().map(c -> {
            Customers cus = customersService.getCustomerInformation(c.getCustomerId());
            Rooms rm = (c.getRoomId() != null && c.getRoomId() > 0) ? roomsService.findRoomByRoomId(c.getRoomId()) : null;
            ComplaintTypeV1 ct = (c.getComplaintTypeId() != null) ? complaintTypeService.getComplaintType(c.getComplaintTypeId()) : null;

            String fullName = cus != null ? (cus.getFirstName() + (cus.getLastName() != null ? " " + cus.getLastName() : "")) : "Unknown";
            String initials = cus != null ? Utils.getInitials(cus.getFirstName(), cus.getLastName()) : null;
            String profilePic = cus != null ? cus.getProfilePic() : null;
            String roomName = (rm != null) ? rm.getRoomName() : null;
            String complaintTypeName = (ct != null) ? ct.getComplaintTypeName() : null;
            String formattedDate = (c.getCreatedAt() != null) ? sdf.format(c.getCreatedAt()) : null;

            return new TenantComplaint(
                    c.getCustomerId(),
                    complaintTypeName,
                    roomName,
                    c.getDescription(),
                    fullName,
                    initials,
                    profilePic,
                    formattedDate,
                    complaintTypeName
            );
        }).collect(Collectors.toList());
    }
}
