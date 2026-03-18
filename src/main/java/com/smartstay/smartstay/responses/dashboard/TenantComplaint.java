package com.smartstay.smartstay.responses.dashboard;

public record TenantComplaint(
        String tenantId,
        String complaintName,
                              String roomName, 
                              String complaintDescription, 
                              String fullName, 
                              String initial, 
                              String profileurl, 
                              String complaintDate, 
                              String complaintType) {
}
