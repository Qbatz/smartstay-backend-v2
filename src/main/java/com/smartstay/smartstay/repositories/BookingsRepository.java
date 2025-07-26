package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dto.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingsRepository extends JpaRepository<BookingsV1, String> {

        @Query(value = "SELECT bookings.booking_id, bookings.customer_id, bookings.joining_date, bookings.rent_amount, bookings.hostel_id, cus.first_name, cus.city, cus.country, cus.current_status, cus.email_id, cus.profile_pic FROM smart_stay.bookingsv1 bookings left outer join customers cus on cus.customer_id=bookings.customer_id where bookings.hostel_id=:hostelId", nativeQuery = true)
        List<Bookings> findAllByHostelId(@Param("hostelId") String hostelId);

        @Query(value = "SELECT * FROM bookingsv1 where bed_id=:bedId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
        BookingsV1 findLatestBooking(@Param("bedId") int bedId);
}
