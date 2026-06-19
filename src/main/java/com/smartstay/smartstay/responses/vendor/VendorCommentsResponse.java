package com.smartstay.smartstay.responses.vendor;

import java.util.List;

public record VendorCommentsResponse(
        long totalComments,
        int currentPage,
        int totalPages,
        int itemPerPage,
        List<VendorCommentResponse> comments) {
}
