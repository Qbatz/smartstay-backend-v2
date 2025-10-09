package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dto.Bookings;
import com.smartstay.smartstay.dto.booking.BookedCustomer;
import com.smartstay.smartstay.dto.customer.CustomersBookingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BookingsRepository extends JpaRepository<BookingsV1, String> {

    @Query(value = "SELECT bookings.booking_id, bookings.customer_id, bookings.joining_date, bookings.rent_amount, bookings.hostel_id, cus.first_name, cus.city, cus.state, cus.country, cus.current_status, cus.email_id, cus.profile_pic FROM bookingsv1 bookings left outer join customers cus on cus.customer_id=bookings.customer_id where bookings.hostel_id=:hostelId", nativeQuery = true)
    List<Bookings> findAllByHostelId(@Param("hostelId") String hostelId);

    @Query(value = "SELECT * FROM bookingsv1 where bed_id=:bedId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    BookingsV1 findLatestBooking(@Param("bedId") int bedId);

    @Query(value = "SELECT * FROM bookingsv1 where bed_id=:bedId and current_status in ('NOTICE', 'VACATED', 'TERMINATED', 'CANCELLED') ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    BookingsV1 findCheckingOutDetails(@Param("bedId") int bedId);

    BookingsV1 findByCustomerId(@Param("customerId") String customerId);

    BookingsV1 findByCustomerIdAndHostelId(@Param("customerId") String customerId, @Param("hostelId") String hostelId);

    @Query(value = """
            SELECT bookingv1.bed_id as bedId, bookingv1.floor_id as floorId, bookingv1.room_id as roomId, 
            bookingv1.rent_amount as rentAmount, bookingv1.leaving_date as leavingDate, 
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
    List<BookedCustomer> findBookingsByListRooms(@Param("listRooms") List<Integer> listRooms, @Param("startDate") Date startDate, @Param("endDate") Date endDate);


}
