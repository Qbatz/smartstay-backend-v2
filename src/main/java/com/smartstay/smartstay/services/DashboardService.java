package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Rooms;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.beds.RoomBedCount;
import com.smartstay.smartstay.dto.dashboard.BedsStatus;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.responses.dashboard.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private InvoicesV1Repository invoicesV1Repository;
    @Autowired
    private ComplaintRepository complaintRepository;
    @Autowired
    private AmenityRequestRepository amenityRequestRepository;
    @Autowired
    private BookingsRepository bookingsRepository;
    @Autowired
    private ExpensesRepository expensesRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BedsRepository bedsRepository;

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
                buildOccupancyTrend(hostelId, occupancyFilter),
                buildBillingSummary(hostelId, billingFilter),
                buildTenantComplaints(hostelId, complaintRequestFilter),
                buildTenantRequests(hostelId, complaintRequestFilter),
                buildFinanceSummary(hostelId, financeFilter),
                buildRecentCheckins(hostelId),
                buildOverdueInvoices(hostelId),
                buildRecentComplaints(hostelId),
                buildRecentRequests(hostelId),
                DASHBOARD_FILTERS);

        return new ResponseEntity<>(dashboardNew, HttpStatus.OK);
    }

    private RoomsAndBedInfo buildRoomsAndBedInfo(String hostelId) {
        int totalRooms = roomRepository.getCountOfRoomsBasedOnHostel(hostelId);
        int totalBeds = bedsRepository.countAllByHostelId(hostelId);

        List<Rooms> rooms = roomRepository.findByHostelId(hostelId);
        List<RoomBedCount> totalBedsByRoom = bedsRepository
                .countBedsByRoomForHostel(hostelId);
        List<RoomBedCount> occupiedBedsByRoom = bedsRepository
                .countOccupiedBedsByRoomForHostel(hostelId);

        Map<Integer, Long> totalMap = totalBedsByRoom.stream()
                .collect(Collectors.toMap(RoomBedCount::getRoomId, RoomBedCount::getBedCount));
        Map<Integer, Long> occupiedMap = occupiedBedsByRoom.stream()
                .collect(Collectors.toMap(RoomBedCount::getRoomId, RoomBedCount::getBedCount));

        int filledRooms = 0;
        Map<Integer, List<Integer>> sharingGroups = new HashMap<>(); // sharingType -> list of roomIds

        for (Rooms room : rooms) {
            Long total = totalMap.getOrDefault(room.getRoomId(), 0L);
            Long occupied = occupiedMap.getOrDefault(room.getRoomId(), 0L);
            if (total > 0 && total.equals(occupied)) {
                filledRooms++;
            }
            sharingGroups.computeIfAbsent(room.getSharingType(), k -> new ArrayList<>()).add(room.getRoomId());
        }

        List<SharingInfo> sharingInfoList = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : sharingGroups.entrySet()) {
            int sharingType = entry.getKey();
            List<Integer> roomIds = entry.getValue();

            long typeTotalBeds = roomIds.stream().mapToLong(id -> totalMap.getOrDefault(id, 0L)).sum();
            long typeFilledBeds = roomIds.stream().mapToLong(id -> occupiedMap.getOrDefault(id, 0L)).sum();

            double occupancy = typeTotalBeds > 0 ? (double) typeFilledBeds / typeTotalBeds * 100 : 0;
            String typeName = (sharingType == 1 ? "Single" : sharingType) + " Sharing";

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

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM");

        while (!cal.getTime().after(dates.endDate())) {
            Date date = cal.getTime();
            int occupied = bookingsRepository.countByStatusAndDate(hostelId, "CHECKIN", date) +
                    bookingsRepository.countByStatusAndDate(hostelId, "NOTICE", date);
            int booked = bookingsRepository.countBookedByDate(hostelId, date);

            trend.add(new OccupancyPoint(sdf.format(date), booked, occupied));
            cal.add(Calendar.DATE, 1);
            if (trend.size() > 90)
                break; // Maximum 90 days
        }
        return trend;
    }

    private BillingSummary buildBillingSummary(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter);
        Map<String, Object> summary = invoicesV1Repository.getBillingSummary(hostelId, dates.startDate(),
                dates.endDate());
        Integer totalInvoiceGenerated = summary.get("totalInvoiceGenerated") != null
                ? ((Number) summary.get("totalInvoiceGenerated")).intValue()
                : 0;
        Double totalInvoiced = summary.get("totalInvoiced") != null
                ? ((Number) summary.get("totalInvoiced")).doubleValue()
                : 0.0;
        Double totalPaid = summary.get("totalPaid") != null ? ((Number) summary.get("totalPaid")).doubleValue() : 0.0;
        return new BillingSummary(totalInvoiceGenerated, Utils.roundOffWithTwoDigit(totalPaid),
                Utils.roundOffWithTwoDigit(totalInvoiced - totalPaid));
    }

    private StatusSummary buildTenantComplaints(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter);
        Map<String, Object> summary = complaintRepository.getComplaintStatusSummary(hostelId, dates.startDate(),
                dates.endDate());
        Integer total = summary.get("total") != null ? ((Number) summary.get("total")).intValue() : 0;
        Integer pending = summary.get("pending") != null ? ((Number) summary.get("pending")).intValue() : 0;
        Integer resolved = summary.get("resolved") != null ? ((Number) summary.get("resolved")).intValue() : 0;
        return new StatusSummary(total, pending, resolved);
    }

    private StatusSummary buildTenantRequests(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter);
        Map<String, Object> summary = amenityRequestRepository.getRequestStatusSummary(hostelId, dates.startDate(),
                dates.endDate());
        Integer total = summary.get("total") != null ? ((Number) summary.get("total")).intValue() : 0;
        Integer pending = summary.get("pending") != null ? ((Number) summary.get("pending")).intValue() : 0;
        Integer resolved = summary.get("resolved") != null ? ((Number) summary.get("resolved")).intValue() : 0;
        return new StatusSummary(total, pending, resolved);
    }

    private FinanceSummary buildFinanceSummary(String hostelId, String filter) {
        DashboardDateRange dates = getDateRange(filter);
        Double income = invoicesV1Repository.getTotalPaidAmount(hostelId, dates.startDate(), dates.endDate());
        Double expense = expensesRepository.sumAmountByHostelIdAndDateRange(hostelId, dates.startDate(),
                dates.endDate());
        income = income != null ? income : 0.0;
        expense = expense != null ? expense : 0.0;
        return new FinanceSummary(income, expense, Utils.roundOffWithTwoDigit(income - expense));
    }

    private List<RecentCheckin> buildRecentCheckins(String hostelId) {
        List<Map<String, Object>> data = bookingsRepository.findLatestCheckins(hostelId, PageRequest.of(0, 5));
        return data.stream().map(m -> new RecentCheckin(
                (String) m.get("customerName"),
                (String) m.get("profilePic"),
                (String) m.get("roomName"),
                (String) m.get("bedName"),
                (Date) m.get("joiningDate"),
                (String) m.get("status"))).collect(Collectors.toList());
    }

    private List<OverdueInvoice> buildOverdueInvoices(String hostelId) {
        List<Map<String, Object>> data = invoicesV1Repository.findOverdueInvoices(hostelId, PageRequest.of(0, 5));
        return data.stream().map(m -> new OverdueInvoice(
                (String) m.get("invoiceId"),
                (String) m.get("invoiceNumber"),
                (String) m.get("customerName"),
                m.get("totalAmount") != null ? ((Number) m.get("totalAmount")).doubleValue() : 0.0,
                m.get("paidAmount") != null ? ((Number) m.get("paidAmount")).doubleValue() : 0.0,
                (Date) m.get("dueDate"),
                (String) m.get("status"))).collect(Collectors.toList());
    }

    private List<RecentComplaint> buildRecentComplaints(String hostelId) {
        List<Map<String, Object>> data = complaintRepository.findLatestComplaints(hostelId, PageRequest.of(0, 5));
        return data.stream().map(m -> new RecentComplaint(
                m.get("complaintId") != null ? ((Number) m.get("complaintId")).intValue() : 0,
                (String) m.get("customerName"),
                (String) m.get("profilePic"),
                (String) m.get("type"),
                (String) m.get("status"),
                (Date) m.get("date"))).collect(Collectors.toList());
    }

    private List<RecentRequest> buildRecentRequests(String hostelId) {
        List<Map<String, Object>> data = amenityRequestRepository.findLatestRequests(hostelId, PageRequest.of(0, 5));
        return data.stream().map(m -> new RecentRequest(
                m.get("requestId") != null ? ((Number) m.get("requestId")).longValue() : 0L,
                (String) m.get("customerName"),
                (String) m.get("profilePic"),
                (String) m.get("type"),
                (String) m.get("status"),
                (Date) m.get("date"))).collect(Collectors.toList());
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
