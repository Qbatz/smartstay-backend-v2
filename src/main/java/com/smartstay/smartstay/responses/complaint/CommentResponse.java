package com.smartstay.smartstay.responses.complaint;

import com.fasterxml.jackson.annotation.JsonFormat;

import static com.smartstay.smartstay.util.Utils.OUTPUT_DATE_FORMAT;

public record CommentResponse(
        Integer commentId,
        Integer complaintId,
        String commentText,
        String commentedBy,
        String commentAt,
        String initials,
        String profilePic
) {}
