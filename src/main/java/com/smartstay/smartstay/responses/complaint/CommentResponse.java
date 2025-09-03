package com.smartstay.smartstay.responses.complaint;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record CommentResponse(
        Integer commentId,
        Integer complaintId,
        String commentText,
        String commentedBy,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm")
        java.util.Date commentedAt
) {}
