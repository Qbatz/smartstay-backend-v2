package com.smartstay.smartstay.Wrappers.assets;

import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.assets.AssetAssignmentResponse;
import com.smartstay.smartstay.util.BankUtils;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class AssetAssignmentResponseMapper implements Function<AssetsV1, AssetAssignmentResponse> {

    private final HostelV1 hostel;
    private final VendorV1 vendor;
    private final Floors floor;
    private final Rooms room;
    private final Beds bed;
    private final BankingV2 bank;

    public AssetAssignmentResponseMapper(HostelV1 hostel,
                                         VendorV1 vendor,
                                         Floors floor,
                                         Rooms room,
                                         Beds bed,
                                         BankingV2 bank) {
        this.hostel = hostel;
        this.vendor = vendor;
        this.floor = floor;
        this.room = room;
        this.bed = bed;
        this.bank = bank;
    }

    @Override
    public AssetAssignmentResponse apply(AssetsV1 asset) {

        String purchaseDate = null;
        double price = 0;
        String hostelName = null;
        String vendorName = null;
        String floorName = null;
        String roomName = null;
        String bedName = null;
        String assignedAt = null;
        String assignmentStatus = "Unassigned";
        String paymentMode = null;

        if (asset.getPurchaseDate() != null) {
            purchaseDate = Utils.dateToString(asset.getPurchaseDate());
        }
        if (asset.getPrice() != null) {
            price = asset.getPrice();
        }
        if (hostel != null) {
            hostelName = hostel.getHostelName();
        }
        if (vendor != null) {
            vendorName = NameUtils.getFullName(vendor.getFirstName(), vendor.getLastName());
        }
        if (floor != null) {
            floorName = floor.getFloorName();
        }
        if (room != null) {
            roomName = room.getRoomName();
        }
        if (bed != null) {
            bedName = bed.getBedName();
        }
        if (asset.getAssignedAt() != null) {
            assignedAt = Utils.dateToString(asset.getAssignedAt());
        }
        if (asset.getFloorId() != null && asset.getRoomId() != null && asset.getBedId() != null) {
            assignmentStatus = "Assigned";
        }
        if (bank != null) {
            paymentMode = BankUtils.getBankName(bank, asset.getBankingMethodId()).toString();
        }

        return new AssetAssignmentResponse(asset.getAssetId(), asset.getAssetName(),
                asset.getBrandName(), asset.getProductName(), asset.getSerialNumber(),
                purchaseDate, price, asset.getBankId(), paymentMode, asset.getHostelId(),
                hostelName, asset.getVendorId(), vendorName, asset.getFloorId(), floorName,
                asset.getRoomId(), roomName, asset.getBedId(), bedName, assignedAt,
                assignmentStatus);
    }
}
