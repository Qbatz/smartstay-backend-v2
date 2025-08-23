package com.smartstay.smartstay.responses.complaint;

import java.util.Date;

public record ComplaintResponse(
        Integer complaintId,
        Date complaintDate,
        Integer status,
        String complaintType
) {}
