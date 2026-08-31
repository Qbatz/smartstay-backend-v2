package com.smartstay.smartstay.dto.kyc;

import java.util.Date;

public record KycUsage(String hostelId,
                       String hostelName,
                       Long count,
                       Date latestRequest,
                       Date latestVerified,
                       Long requestCount) {
}
