package com.smartstay.smartstay.dto.assets;

import java.util.List;

public record AssetsMobileList(String hostelId,
                               int currentPage,
                               int totalPages,
                               int itemPerPage,
                               List<AssetAssignmentResponse> assetsList) {
}
