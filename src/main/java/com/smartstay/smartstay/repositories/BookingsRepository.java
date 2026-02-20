package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dto.Bookings;
import com.smartstay.smartstay.dto.booking.BedBookingStatus;
import com.smartstay.smartstay.dto.booking.BookedCustomer;
import com.smartstay.smartstay.dto.booking.BookedCustomerInfoElectricity;
import com.smartstay.smartstay.dto.customer.CustomersBookingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BookingsRepository extends JpaRepository<BookingsV1, String> {

        @Query(value = "SELECT bookings.booking_id, bookings.customer_id, bookings.joining_date, bookings.rent_amount, "
                        + "bookings.hostel_id, cus.first_name, cus.city, cus.state, cus.country, cus.current_status, cus.email_id, "
                        + "cus.profile_pic FROM bookingsv1 bookings left outer join customers cus "
                        + "on cus.customer_id=bookings.customer_id where bookings.hostel_id=:hostelId", nativeQuery = true)
        List<Bookings> findAllByHostelId(@Param("hostelId") String hostelId);

        @Query(value = "SELECT * FROM bookingsv1 where bed_id=:bedId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
        BookingsV1 findLatestBooking(@Param("bedId") int bedId);

        @Query(value = "SELECT * FROM bookingsv1 where bed_id=:bedId and current_status in ('NOTICE', 'VACATED', 'TERMINATED', 'CANCELLED') ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
        BookingsV1 findCheckingOutDetails(@Param("bedId") int bedId);

        @Query(value = """
                        SELECT * FROM bookingsv1 where bed_id=:bedId and current_status in ('CHECKIN', 'NOTICE')
                        """, nativeQuery = true)
        List<BookingsV1> findOccupiedDetails(@Param("bedId") Integer bedId);

        @Query(value = """
                        SELECT * FROM bookingsv1 where bed_id=:bedId and current_status in ('BOOKED')
                        """, nativeQuery = true)
        List<BookingsV1> findBookedDetails(@Param("bedId") Integer bedId);

        BookingsV1 findByCustomerId(@Param("customerId") String customerId);

        BookingsV1 findByCustomerIdAndHostelId(@Param("customerId") String customerId,
                        @Param("hostelId") String hostelId);

        @Query(value = """
                        SELECT bookingv1.bed_id as bedId, bookingv1.floor_id as floorId, bookingv1.room_id as roomId, bookingv1.is_booked as isBooked,
                        bookingv1.booking_amount as bookingAmount, bookingv1.checkout_date as checkoutDate,
                        bookingv1.rent_amount as rentAmount, bookingv1.leaving_date as requestedCheckoutDate,
                        bookingv1.notice_date as noticeDate,  bookingv1.joining_date as joiningDate, bookingv1.booking_id as bookingId,
                        bookingv1.current_status as currentStatus, bookingv1.reason_for_leaving as reasonForLeaving,
                        bookingv1.expected_joining_date as expectedJoiningDate, usr.first_name as firstName,
                        usr.last_name as lastName, room.room_name as roomName, flr.floor_name as floorName,
                        bed.bed_name as bedName  FROM bookingsv1 bookingv1
                        left outer join users usr on usr.user_id=bookingv1.created_by
                        left outer join rooms room on room.room_id=bookingv1.room_id
                        left outer join floors flr on flr.floor_id=bookingv1.floor_id
                        left outer join beds bed on bed.bed_id=bookingv1.bed_id
                        where bookingv1.customer_id=:customerId  order by bookingv1.joining_date desc limit 1
                        """, nativeQuery = true)
        CustomersBookingDetails getCustomerBookingDetails(@Param("customerId") String customerId);

        @Query(value = """
                        SELECT customers.first_name as firstName, customers.last_name as lastName, booking.customer_id as customerId,
                        booking.bed_id as bedId, booking.floor_id as floorId, booking.room_id as roomId, rms.room_name as roomName,
                        flrs.floor_name as floorName, bed.bed_name as bedName, booking.joining_date as joiningDate,
                        booking.leaving_date as leavingDate  FROM bookingsv1 booking
                        INNER JOIN customers customers on customers.customer_id=booking.customer_id
                        left outer join rooms rms on rms.room_id=booking.room_id
                        left outer join floors flrs on flrs.floor_id=booking.floor_id
                        left outer join beds bed on bed.bed_id=booking.bed_id
                        where booking.current_status in ('CHECKIN', 'NOTICE') and booking.room_id in (:listRooms)
                         and booking.joining_date <=DATE(:endDate) and booking.leaving_date IS NULL OR booking.leaving_date >= DATE(:startDate)
                        """, nativeQuery = true)
        List<BookedCustomer> findBookingsByListRooms(@Param("listRooms") List<Integer> listRooms,
                        @Param("startDate") Date startDate, @Param("endDate") Date endDate);

        @Query(value = """
                        SELECT * FROM bookingsv1 WHERE current_status = 'CHECKIN';
                        """, nativeQuery = true)
        List<BookingsV1> findAllCheckedInUsers();

        @Query(value = """
                        SELECT booking.customer_id as customerId, booking.hostel_id as hostelId, booking.joining_date as joiningDate,
                        booking.leaving_date as leavingDate, booking.room_id as roomId, booking.floor_id as floorId,
                        booking.bed_id as bedId, flrs.floor_name as floorName, rms.room_name as roomName, bed.bed_name as bedName
                        FROM bookingsv1 booking inner join beds bed on bed.bed_id=booking.bed_id inner join rooms rms on rms.room_id=booking.room_id
                        INNER join floors flrs on flrs.floor_id=booking.floor_id WHERE booking.room_id=:roomId and
                        booking.current_status in ('CHECKIN', 'NOTICE') and booking.joining_date <=DATE(:endDate)
                        and (booking.leaving_date is NULL or booking.leaving_date >= DATE(:startDate))
                        """, nativeQuery = true)
        List<BookedCustomerInfoElectricity> getBookingInfoForElectricity(@Param("roomId") Integer roomId,
                        @Param("startDate") Date startDate, @Param("endDate") Date endDate);

        @Query(value = """
                        SELECT * FROM bookingsv1 booking WHERE booking.customer_id=:customerId AND booking.joining_date <= DATE(:endDate) AND (booking.leaving_date IS NULL OR leaving_date >= DATE(:startDate));
                        """, nativeQuery = true)
        BookingsV1 findByCustomerIdAndJoiningDate(String customerId, @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        @Query(value = """
                        SELECT * FROM bookingsv1 booking where booking.bed_id=:bedId
                        ORDER BY booking.joining_date DESC LIMIT 1
                        """, nativeQuery = true)
        BookingsV1 checkBedsAvailabilityForDate(@Param("bedId") Integer bedId);

        @Query(value = """
                        SELECT * FROM bookingsv1 b
                        WHERE b.bed_id = :bedId
                          AND b.customer_id <> :customerId
                          AND DATE(b.expected_joining_date) = DATE(DATE_ADD(:expectedJoiningDate, INTERVAL 1 DAY))
                        """, nativeQuery = true)
        List<BookingsV1> findNextDayBookingForSameBed(@Param("bedId") int bedId, @Param("customerId") String customerId,
                        @Param("expectedJoiningDate") Date expectedJoiningDate);

        @Query(value = """
                        SELECT bed_id as bedId, current_status as currentStatus, joining_date as joiningDate,
                        leaving_date as leavingDate, customer_id as customerId FROM bookingsv1 where current_status in ('BOOKED', 'NOTICE', 'CHECKIN') and bed_id in (:listBedIds)
                        """, nativeQuery = true)
        List<BedBookingStatus> findByBedBookingStatus(List<Integer> listBedIds);

        List<BookingsV1> findByHostelIdAndCurrentStatusIn(String hostelId, List<String> currentStatuses);

        @Query("""
                        SELECT booking from BookingsV1 booking WHERE booking.bedId=:bedId and booking.currentStatus in ('CHECKIN', 'NOTICE')
                        """)
        List<BookingsV1> checkBookingsByBedIdAndStatus(Integer bedId);

        @Query("""
                        SELECT booking FROM BookingsV1 booking where booking.hostelId=:hostelId and booking.currentStatus='BOOKED'
                        """)
        List<BookingsV1> findBookingsByHostelId(String hostelId);

        @Query("""
                        SELECT booking FROM BookingsV1 booking where booking.hostelId=:hostelId and booking.currentStatus='BOOKED' AND
                        booking.expectedJoiningDate >= DATE(:startDate) and booking.expectedJoiningDate <= DATE(:startDate)
                        """)
        List<BookingsV1> findBookingsWithDate(String hostelId, Date startDate);

        @Query(value = """
                        SELECT * FROM bookingsv1 WHERE checkout_date IS NULL and DATE(leaving_date)<=DATE(:todaysDate) AND current_status ='NOTICE'
                        """, nativeQuery = true)
        List<BookingsV1> checkAnyCheckoutMissing(@Param("todaysDate") Date todaysDate);

        @Query(value = """
                        SELECT booking FROM BookingsV1 booking WHERE DATE(booking.joiningDate) IS NULL AND DATE(booking.expectedJoiningDate) < DATE(:todaysDate) AND booking.isBooked=true
                        """)
        List<BookingsV1> checkAnyMissingCheckIn(@Param("todaysDate") Date todaysDate);

        @Query("""
                        SELECT b FROM BookingsV1 b
                               WHERE b.bedId IN :bedIds
                               AND b.bookingDate = (
                                   SELECT MAX(b2.bookingDate)
                                   FROM BookingsV1 b2
                                   WHERE b2.bedId = b.bedId
                               )
                        """)
        List<BookingsV1> findLatestBookingsForBeds(@Param("bedIds") List<Integer> bedIds);

        @Query(value = """
                        SELECT * FROM bookingsv1 where hostel_id=:hostelId AND customer_id IN (:customerIds) AND current_status IN ('CHECKIN', 'NOTICE')
                        """, nativeQuery = true)
        List<BookingsV1> findBookingsByListOfCustomersAndHostelId(List<String> customerIds, String hostelId);

        @Query("""
                        SELECT booking FROM BookingsV1 booking WHERE booking.bedId=:bedId AND booking.customerId !=:customerId
                        AND booking.currentStatus IN ('CHECKIN')
                        """)
        BookingsV1 findByBedIdAndCustomerIdNot(Integer bedId, String customerId);

        @Query("""
                        SELECT booking FROM BookingsV1 booking WHERE booking.hostelId=:hostelId AND booking.currentStatus IN ('VACATED',
                        'NOTICE', 'CHECKIN', 'TERMINATED')
                        """)
        List<BookingsV1> findAllBookingsByHostelId(String hostelId);

        @Query("""
                        SELECT booking FROM BookingsV1 booking WHERE booking.bedId=:bedId AND booking.currentStatus IN ('BOOKED')
                        AND booking.customerId !=:customerId
                        """)
        List<BookingsV1> findAllBookingsByHostelIdExcludeCurrentCheckIn(Integer bedId, String customerId);

        @Query(value = """
                        SELECT * FROM bookingsv1 WHERE bed_id=:bedId AND DATE(joining_date) <= DATE(:date) AND (checkout_date IS NULL OR DATE(checkout_date) > DATE(:date))
                        """, nativeQuery = true)
        List<BookingsV1> findAllBookingsBasedOnBedIdAndDate(@Param("bedId") Integer bedId, @Param("date") Date date);

        @Query(value = """
                        SELECT * FROM bookingsv1 WHERE hostel_id = :hostelId
                        AND (joining_date <= DATE(:endDate) AND (checkout_date IS NULL OR checkout_date >= DATE(:startDate)))
                        ORDER BY joining_date DESC
                        """, nativeQuery = true)
        List<BookingsV1> findBookingsForTenantRegister(@Param("hostelId") String hostelId,
                        @Param("startDate") Date startDate, @Param("endDate") Date endDate,
                        org.springframework.data.domain.Pageable pageable);

        @Query(value = """
                        SELECT * FROM bookingsv1 WHERE hostel_id = :hostelId
                        AND (joining_date <= DATE(:endDate) AND (checkout_date IS NULL OR checkout_date >= DATE(:startDate)))
                        """, nativeQuery = true)
        List<BookingsV1> findAllBookingsForTenantRegister(@Param("hostelId") String hostelId,
                        @Param("startDate") Date startDate, @Param("endDate") Date endDate);

        @Query(value = """
                        SELECT COUNT(*) FROM bookingsv1 WHERE hostel_id = :hostelId
                        AND (joining_date <= DATE(:endDate) AND (checkout_date IS NULL OR checkout_date >= DATE(:startDate)))
                        """, nativeQuery = true)
        long countBookingsForTenantRegister(@Param("hostelId") String hostelId, @Param("startDate") Date startDate,
                        @Param("endDate") Date endDate);

        @Query(value = """
                        SELECT b FROM BookingsV1 b WHERE b.hostelId = :hostelId 
                        AND 
                (b.joiningDate IS NOT NULL AND DATE(b.joiningDate) <= DATE(:endDate) 
                OR 
                (b.joiningDate IS NULL AND DATE(b.expectedJoiningDate) <= DATE(:endDate))) 
                AND 
                (b.currentStatus <> 'CANCELLED' OR (b.currentStatus = 'CANCELLED' AND DATE(b.cancelDate) >= DATE(:startDate) AND 
                DATE(b.cancelDate) <= DATE(:endDate))) AND 
                (b.checkoutDate IS NULL OR DATE(b.checkoutDate) >= DATE(:startDate)) 
                        AND (:customerIds IS NULL OR b.customerId IN :customerIds) 
                        AND (:statuses IS NULL OR b.currentStatus IN :statuses) 
                        AND (:roomIds IS NULL OR b.roomId IN :roomIds) 
                        AND (:floorIds IS NULL OR b.floorId IN :floorIds) ORDER BY b.joiningDate DESC""")
        org.springframework.data.domain.Page<BookingsV1> findBookingsWithFilters(@Param("hostelId") String hostelId,
                        @Param("startDate") Date startDate, @Param("endDate") Date endDate,
                        @Param("customerIds") List<String> customerIds, @Param("statuses") List<String> statuses,
                        @Param("roomIds") List<Integer> roomIds, @Param("floorIds") List<Integer> floorIds,
                        org.springframework.data.domain.Pageable pageable);

        @Query(value = """
                        SELECT b FROM BookingsV1 b WHERE b.hostelId = :hostelId 
                        AND 
                (b.joiningDate IS NOT NULL AND DATE(b.joiningDate) <= DATE(:endDate) 
                OR 
                (b.joiningDate IS NULL AND DATE(b.expectedJoiningDate) <= DATE(:endDate))) 
                AND 
                (b.currentStatus <> 'CANCELLED' OR (b.currentStatus = 'CANCELLED' AND DATE(b.cancelDate) >= DATE(:startDate) AND 
                DATE(b.cancelDate) <= DATE(:endDate))) AND 
                (b.checkoutDate IS NULL OR DATE(b.checkoutDate) >= DATE(:startDate)) 
                        AND (:customerIds IS NULL OR b.customerId IN :customerIds) 
                        AND (:statuses IS NULL OR b.currentStatus IN :statuses) 
                        AND (:roomIds IS NULL OR b.roomId IN :roomIds) 
                        AND (:floorIds IS NULL OR b.floorId IN :floorIds)""")
        List<BookingsV1> findAllBookingsWithFilters(@Param("hostelId") String hostelId,
                        @Param("startDate") Date startDate, @Param("endDate") Date endDate,
                        @Param("customerIds") List<String> customerIds, @Param("statuses") List<String> statuses,
                        @Param("roomIds") List<Integer> roomIds, @Param("floorIds") List<Integer> floorIds);


}
