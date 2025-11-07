package com.smartstay.smartstay.responses.dashboard;

public record Dashboard(Integer totalRooms,
                        Integer totalBeds,
                        Integer freeBeds,
                        Integer occupiedBeds,
                        Integer bookedBeds,
                        Integer totalCustomers,
                        Double totalAssetsValue,
                        Double advances,
                        Double electricityAmount,
                        Double nextMonthProjection,
                        Double currentMonthProfit,
                        Double otherProfit,
                        int pendingInvoiceCount) {
}
