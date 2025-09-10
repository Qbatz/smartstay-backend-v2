package com.smartstay.smartstay.repositories;

import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dto.invoices.Invoices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoicesV1Repository extends JpaRepository<InvoicesV1, String> {

    @Query(value = """
            select invc.invoice_id as invoiceId, invc.amount, invc.gst, invc.cgst, 
            invc.created_at as createdAt, invc.created_by as createdBy, invc.customer_id as customerId, 
            invc.hostel_id as hostelId, invc.invoice_generated_date as invoiceGeneratedAt, 
            invc.invoice_due_date as invoiceDueDate, invc.invoice_type as invoiceType, 
            invc.payment_status as paymentStatus, invc.updated_at as updatedAt, 
            invc.invoice_number as invoiceNumber, customers.first_name as firstName, customers.last_name as lastName, 
            advance.advance_amount as advanceAmount, advance.deductions as deductions from 
            invoicesv1 invc inner join customers customers on customers.customer_id=invc.customer_id 
            left outer join advance advance on advance.customer_id=invc.customer_id where invc.hostel_id=:hostelId AND invc.invoice_type not in('BOOKING')
            """, nativeQuery = true)
    List<Invoices> findByHostelId(@Param("hostelId") String hostelId);

}
