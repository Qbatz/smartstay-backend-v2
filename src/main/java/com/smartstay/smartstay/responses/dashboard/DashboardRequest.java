package com.smartstay.smartstay.responses.dashboard;

public record DashboardRequest(Long id, String customerName, String profilePic, String type, String status, String date, String description) {
}
