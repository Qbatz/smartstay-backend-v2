package com.smartstay.smartstay.responses.dashboard;

import java.util.List;

public record OccupancyTrendSummary(Double avgOccupied, Double avgVacant, List<OccupancyPoint> occupancyTrend) {
}
