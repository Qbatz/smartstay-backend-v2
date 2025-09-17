package com.smartstay.smartstay.dto.bills;

import java.util.List;

public record BillTemplateResponse(
        Integer templateId,
        String hostelId,
        String signature,
        String logo,
        String emailId,
        String mobile,
        String createdAt,
        Boolean isLogoCustomized,
        Boolean isMobileCustomized,
        Boolean isMailIdCustomized,
        Boolean isSignatureCustomized,
        List<TemplateDetailResponse> templates
) { }
