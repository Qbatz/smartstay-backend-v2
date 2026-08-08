package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceNotes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notesId;
    private String invoiceId;
    private String hostelId;
    private String customerId;
    private String description;
    private String notes;
}
