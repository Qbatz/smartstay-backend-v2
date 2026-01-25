package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.ComplaintComments;
import com.smartstay.smartstay.dao.ComplaintsV1;
import com.smartstay.smartstay.responses.complaint.ComplaintResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ComplaintRepository extends JpaRepository<ComplaintsV1, String> {


    ComplaintsV1 findByComplaintIdAndParentId(int complaintId, String parentId);

    @Query(value = """
    SELECT cus.* 
    FROM customers cus
    JOIN complaintsv1 c ON c.customer_id = cus.customer_id
    WHERE c.complaint_id = :complaintId
      AND c.parent_id = :parentId
      AND cus.current_status IN (:currentStatus)
""", nativeQuery = true)
    ComplaintsV1 findCustomerForComplaint(
            @Param("complaintId") int complaintId,
            @Param("parentId") String parentId,
            @Param("currentStatus") List<String> currentStatus
    );

    boolean existsByComplaintTypeIdAndIsActive(Integer complaintTypeId, Boolean isActive);

    @Query(value = """
            SELECT 
                c.complaint_id AS complaintId,
                c.customer_id AS customerId,
                CONCAT(cus.first_name, ' ', cus.last_name) AS customerName,
                cus.profile_pic AS customerProfile,
                c.hostel_id AS hostelId,

                c.floor_id AS floorId,
                f.floor_name AS floorName,

                c.room_id AS roomId,
                r.room_name AS roomName,

                c.bed_id AS bedId,
                b.bed_name AS bedName,

                c.complaint_date AS complaintDate,
                c.description AS description,

                 c.assignee_id AS assigneeId,
                 c.assigned_date AS assignedDate,
                 CONCAT_WS(' ', usr.first_name, usr.last_name) AS assigneeName,

                ct.complaint_type_id AS complaintTypeId,
                ct.complaint_type_name AS complaintTypeName,

                c.status AS status,
                COUNT(cc.comment_id) AS commentCount
            FROM complaintsv1 c
            JOIN complaint_typev1 ct 
                ON c.complaint_type_id = ct.complaint_type_id
            JOIN customers cus 
                ON c.customer_id = cus.customer_id
            LEFT JOIN floors f ON c.floor_id = f.floor_id
            LEFT JOIN rooms r ON c.room_id = r.room_id
            LEFT JOIN beds b ON c.bed_id = b.bed_id
            LEFT JOIN users usr ON c.assignee_id = usr.user_id
            LEFT JOIN complaint_comments cc ON cc.complaint_id = c.complaint_id
            WHERE c.hostel_id = :hostelId and c.parent_id = :parentId AND c.is_active=1 and c.is_deleted=0
            AND cus.current_status IN (:currentStatus)
             AND (:customerName IS NULL OR CONCAT(cus.first_name, ' ', cus.last_name) LIKE %:customerName%)
                      AND (:status IS NULL OR c.status = :status)
                      AND (:startDate IS NULL OR c.complaint_date >= :startDate)
                      AND (:endDate IS NULL OR c.complaint_date <= :endDate) 
            GROUP BY c.complaint_id, c.customer_id, cus.first_name, cus.last_name, cus.profile_pic,
                     c.hostel_id, c.floor_id, f.floor_name, c.room_id, r.room_name,
                     c.bed_id, b.bed_name, c.complaint_date, c.description,
                     c.assignee_id, usr.first_name, usr.last_name,
                     ct.complaint_type_id, ct.complaint_type_name, c.status 
            ORDER BY c.complaint_id DESC
            """, nativeQuery = true)
    List<Map<String, Object>> getAllComplaintsRaw(@Param("hostelId") String hostelId, @Param("parentId") String parentId,
                                                  @Param("customerName") String customerName,
                                                  @Param("status") String status,
                                                  @Param("startDate") Date startDate,
                                                  @Param("endDate") Date endDate,
                                                  @Param("currentStatus") List<String> currentStatus
    );


    @Query(value = """
            SELECT 
                cc.comment_id AS commentId,
                cc.complaint_id AS complaintId,
                cc.comment AS commentText,
                cc.comment_date AS commentDate,
                cc.user_name AS userName
            FROM complaint_comments cc
            WHERE cc.complaint_id = :complaintId
            ORDER BY cc.comment_date DESC
            """, nativeQuery = true)
    List<Map<String, Object>> getCommentsByComplaintId(@Param("complaintId") Integer complaintId);


    @Query(value = """
    SELECT 
        c.complaint_id AS complaintId,
        c.customer_id AS customerId,
        CONCAT(cus.first_name, ' ', cus.last_name) AS customerName,
        cus.profile_pic AS customerProfile,
        c.hostel_id AS hostelId,

        c.floor_id AS floorId,
        f.floor_name AS floorName,

        c.room_id AS roomId,
        r.room_name AS roomName,

        c.bed_id AS bedId,
        b.bed_name AS bedName,

        c.complaint_date AS complaintDate,
        c.description AS description,

        c.assignee_id AS assigneeId,
        c.assigned_date AS assignedDate,
        CONCAT_WS(' ', usr.first_name, usr.last_name) AS assigneeName,

        ct.complaint_type_id AS complaintTypeId,
        ct.complaint_type_name AS complaintTypeName,

        c.status AS status,
          COUNT(cc.comment_id) AS commentCount
    FROM complaintsv1 c
    JOIN complaint_typev1 ct 
        ON c.complaint_type_id = ct.complaint_type_id
    JOIN customers cus 
        ON c.customer_id = cus.customer_id
    LEFT JOIN floors f 
        ON c.floor_id = f.floor_id
    LEFT JOIN rooms r 
        ON c.room_id = r.room_id
     LEFT JOIN users usr
        ON c.assignee_id = usr.user_id    
    LEFT JOIN beds b 
        ON c.bed_id = b.bed_id
                 LEFT JOIN complaint_comments cc ON cc.complaint_id = c.complaint_id
    WHERE c.complaint_id = :complaintId and c.parent_id =:parentId and c.is_active=1
     AND cus.current_status IN (:currentStatus)
    """, nativeQuery = true)
    Map<String, Object> getComplaintsWithType(@Param("complaintId") int complaintId,
                                            @Param("parentId") String parentId,
                                              @Param("currentStatus") List<String> currentStatus
    );


    @Query(value = """
    SELECT 
        COUNT(*) AS totalComplaints
    FROM complaintsv1 c
    WHERE c.hostel_id = :hostelId 
      AND c.parent_id = :parentId 
      AND c.is_active = 1 and c.is_deleted=0
    """, nativeQuery = true)
    Map<String, Object> getComplaintSummary(
            @Param("hostelId") String hostelId,
            @Param("parentId") String parentId
    );

    List<ComplaintsV1> findByHostelIdOrderByComplaintDateDesc(String hostelId);
    @Query("SELECT COUNT(c) FROM ComplaintsV1 c WHERE c.hostelId = :hostelId AND c.isActive = true AND c.isDeleted = false AND DATE(c.complaintDate) >= DATE(:startDate) AND DATE(c.complaintDate) <= DATE(:endDate)")
    int countByHostelIdAndDateRange(@Param("hostelId") String hostelId, @Param("startDate") Date startDate,
                                    @Param("endDate") Date endDate);

    @Query("SELECT COUNT(c) FROM ComplaintsV1 c WHERE c.hostelId = :hostelId AND c.status IN :statuses AND c.isActive = true AND c.isDeleted = false AND DATE(c.complaintDate) >= DATE(:startDate) AND DATE(c.complaintDate) <= DATE(:endDate)")
    int countActiveByHostelIdAndDateRange(@Param("hostelId") String hostelId, @Param("statuses") List<String> statuses,
                                          @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
