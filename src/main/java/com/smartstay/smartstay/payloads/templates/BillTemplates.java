package com.smartstay.smartstay.payloads.templates;

import org.springframework.web.multipart.MultipartFile;

/**
 *
 * Use only for internal communication
 *
 * do not use for front end payload
 *
 * used in hostel service to bill templates
 *
 * while adding hostel add the default configuration
 *
 */
public record BillTemplates(String hostelId,
                            String mobile,
                            String emailId,
                            String hostelName,
                            String userId) {
}
