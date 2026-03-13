package com.smartstay.smartstay.dto.reminders;

public record DueReminders(String customerId,
                           String invoiceId,
                           Double dueAmount,
                           Double invoiceAmount,
                           String invoiceNumber) {

}
