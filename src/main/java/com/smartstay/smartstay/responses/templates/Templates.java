package com.smartstay.smartstay.responses.templates;

import java.util.List;

public record Templates(int templateId,
                        String hostelId,
                        String signature,
                        String logo,
                        String emailId,
                        String mobile,
                        String createdAt,
                        boolean isLogoCustomized,
                        boolean isMobileCustomized,
                        boolean isMailIdCustomized,
                        boolean isSignatureCustomized,
                        List<TemplateTypes> templates) {
}
