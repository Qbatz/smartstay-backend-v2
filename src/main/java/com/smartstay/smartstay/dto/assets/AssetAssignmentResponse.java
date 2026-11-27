package com.smartstay.smartstay.dto.assets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetAssignmentResponse {
    private Long assetId;
    private String assetName;
    private String brandName;
    private String productName;
    private String serialNumber;
    private String purchaseDate;
    private Double price;
    private String bankId;
    private String paymentMode;
    private String hostelId;
    private String hostelName;
    private Integer vendorId;
    private String vendorName;
    private Integer floorId;
    private String floorName;
    private Integer roomId;
    private String roomName;
    private Integer bedId;
    private String bedName;
    private String assignedAt;
    private String assignmentStatus;
}
