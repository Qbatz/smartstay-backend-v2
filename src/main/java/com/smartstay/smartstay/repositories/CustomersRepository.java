package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dto.customer.CheckoutCustomers;
import com.smartstay.smartstay.dto.customer.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, String> {
    boolean existsByMobile(String mobileNo);

    boolean existsByEmailId(String emailId);
    boolean existsByHostelIdAndCustomerId(String hostelId,String customerId);
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Customers c " +
            "WHERE c.hostelId = :hostelId " +
            "AND c.customerId = :customerId " +
            "AND c.currentStatus IN (:statuses)")
    boolean existsByHostelIdAndCustomerIdAndStatusesIn(@Param("hostelId") String hostelId,
                                                          @Param("customerId") String customerId,
                                                          @Param("statuses") List<String> statuses);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Customers c " +
            "WHERE c.hostelId = :hostelId " +
            "AND c.currentStatus IN (:statuses)")
    boolean existsByHostelIdAndCurrentStatusIn(@Param("hostelId") String hostelId,
                                               @Param("statuses") List<String> statuses);

    @Query(value = """
            SELECT cus.first_name AS firstName, cus.city, cus.mobile, cus.state, cus.joining_date, 
            cus.created_at, cus.country, cus.current_status AS currentStatus, 
            cus.customer_id as customerId, cus.email_id AS emailId, 
            cus.profile_pic AS profilePic, cus.joining_date as actualJoiningDate, cus.exp_joining_date as joiningDate, 
            ct.country_code as countryCode, booking.floor_id as floorId, booking.room_id as roomId, booking.bed_id as bedId, flr.floor_name as floorName,
            rms.room_name as roomName, bed.bed_name as bedName, booking.expected_joining_date as expectedJoiningDate,
            cus.created_at as createdAt FROM customers cus inner join countries as ct on ct.country_id = cus.country
            left outer join bookingsv1 booking on booking.customer_id=cus.customer_id left outer join floors flr on flr.floor_id=booking.floor_id
            left outer join rooms rms on rms.room_id=booking.room_id left outer join beds bed on bed.bed_id=booking.bed_id
            WHERE cus.hostel_id = :hostelId
            AND (:name IS NULL OR LOWER(cus.first_name) LIKE LOWER(CONCAT('%', :name, '%'))
                                   OR LOWER(cus.last_name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:status IS NULL OR cus.current_status = :status) and cus.current_status in ('INACTIVE', 'CHECK_IN', 'BOOKED', 'NOTICE', 'SETTLEMENT_GENERATED') 
               order by cus.created_at desc
            """, nativeQuery = true)
    List<CustomerData> getCustomerData(
            @Param("hostelId") String hostelId,
            @Param("name") String name,
            @Param("status") String status
    );

    @Query(value = """
            SELECT cus.first_name AS firstName, cus.city, cus.mobile, cus.state, cus.joining_date, 
            cus.created_at, cus.country, cus.current_status AS currentStatus, 
            cus.customer_id as customerId, cus.email_id AS emailId, 
            cus.profile_pic AS profilePic, cus.joining_date as actualJoiningDate, cus.exp_joining_date as joiningDate, 
            ct.country_code as countryCode, booking.floor_id as floorId, booking.room_id as roomId, booking.bed_id as bedId, flr.floor_name as floorName,
            rms.room_name as roomName, bed.bed_name as bedName, booking.expected_joining_date as expectedJoiningDate,
            cus.created_at as createdAt, booking.checkout_date as checkoutDate FROM customers cus inner join countries as ct on ct.country_id = cus.country
            left outer join bookingsv1 booking on booking.customer_id=cus.customer_id left outer join floors flr on flr.floor_id=booking.floor_id
            left outer join rooms rms on rms.room_id=booking.room_id left outer join beds bed on bed.bed_id=booking.bed_id
            WHERE cus.hostel_id = :hostelId
            AND (:name IS NULL OR LOWER(cus.first_name) LIKE LOWER(CONCAT('%', :name, '%'))
                                   OR LOWER(cus.last_name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND cus.current_status in ('VACATED') 
               order by cus.created_at desc
            """, nativeQuery = true)
    List<CheckoutCustomers> getCheckedOutCustomerData(
            @Param("hostelId") String hostelId,
            @Param("name") String name
    );

    @Query("SELECT COUNT(c) FROM Customers c where c.mobile=:mobile and c.customerId!=:customerId")
    int findCustomersByMobile(@Param("customerId") String customerId, @Param("mobile") String mobile);

           
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Customers c " +
            "WHERE c.emailId = :emailId and c.emailId != '' AND c.hostelId = :hostelId " +
            "AND c.currentStatus NOT IN (:statuses)")
    boolean existsByEmailIdAndHostelIdAndStatusesNotIn(@Param("emailId") String emailId,
                                                       @Param("hostelId") String hostelId,
                                                       @Param("statuses") List<String> statuses);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM Customers c " +
            "WHERE c.mobile = :mobile AND c.hostelId = :hostelId " +
            "AND c.currentStatus NOT IN (:statuses)")
    boolean existsByMobileAndHostelIdAndStatusesNotIn(@Param("mobile") String mobile,
                                                      @Param("hostelId") String hostelId,
                                                      @Param("statuses") List<String> statuses);
    List<Customers> findByCustomerIdIn(List<String> customerId);

    @Query("""
            SELECT cus FROM Customers cus where cus.hostelId=:hostelId AND currentStatus in ('NOTICE', 'CHECK_IN')
            """)
    List<Customers> findCheckedInCustomerByHostelId(String hostelId);

}
