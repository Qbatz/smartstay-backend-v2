package com.smartstay.smartstay.payloads.templates;

/**
 * Adding template is possible only for existing hostel which create before 10/sep/2025
 *
 * Post 10/Sep/2025 templates created automatically when hostels are created
 *
 * do not use it- further
 */
public record AddTemplate(String mobile,
                          String mailId,
                          boolean logoCustomized,
                          boolean mobileCustomized,
                          boolean mailIdCustomized,
                          boolean signatureCustomized) {
}
