package com.smartstay.smartstay.responses.complaint;

import com.fasterxml.jackson.annotation.JsonFormat;

import static com.smartstay.smartstay.util.Utils.OUTPUT_DATE_FORMAT;

public record CommentResponse(
        Integer commentId,
        Integer complaintId,
        String commentText,
        String commentedBy,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = OUTPUT_DATE_FORMAT, timezone = "Asia/Kolkata")
        java.util.Date commentedAt
) {}
