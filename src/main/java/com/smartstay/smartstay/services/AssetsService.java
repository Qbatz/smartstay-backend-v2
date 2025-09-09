package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.assets.AssetAssignmentResponse;
import com.smartstay.smartstay.payloads.asset.AssetRequest;
import com.smartstay.smartstay.payloads.asset.AssignAsset;
import com.smartstay.smartstay.payloads.asset.UpdateAsset;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AssetsService {

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    HostelV1Repository hostelV1Repository;

    @Autowired
    BankingRepository bankingRepository;

    @Autowired
    FloorRepository floorRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    BedsRepository bedRepository;

    @Autowired
    VendorRepository vendorRepository;
    @Autowired
    AssetsRepository assetsRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;

    @Autowired
    private UserHostelService userHostelService;

    public ResponseEntity<?> getAllAssets(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        Users users = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(users.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_ASSETS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        List<AssetAssignmentResponse> listAssets = assetsRepository.findAssetAssignmentDetails(hostelId);
        return new ResponseEntity<>(listAssets, HttpStatus.OK);
    }


    public ResponseEntity<?> addAsset(AssetRequest request,String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        Users users = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(users.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_ASSETS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        boolean hostelV1 = userHostelService.checkHostelAccess(users.getUserId(), hostelId);
        if (!hostelV1) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        AssetsV1 asset = new AssetsV1();
        if (request.vendorId() != null && request.vendorId() != 0) {
            VendorV1 vendorV1 = vendorRepository.findByVendorIdAndHostelId(request.vendorId(), hostelId);
            if (vendorV1 == null) {
                return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.FORBIDDEN);
            }
            asset.setVendorId(request.vendorId());
        }
        boolean bankingV1 = bankingRepository.existsByHostelIdAndBankId(hostelId,request.bankingId());
        if (!bankingV1) {
            return new ResponseEntity<>(Utils.INVALID_BANKING, HttpStatus.FORBIDDEN);
        }

        boolean assetNameExists = assetsRepository.existsByAssetNameAndIsDeletedFalseAndHostelId(request.assetName(),hostelId);
        if (assetNameExists) {
            return new ResponseEntity<>(Utils.ASSET_NAME_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }
        if (request.serialNumber() != null && !request.serialNumber().isEmpty()) {
            boolean serialNumberExists = assetsRepository.existsBySerialNumberAndIsDeletedFalseAndHostelId(request.serialNumber(),hostelId);
            if (serialNumberExists) {
                return new ResponseEntity<>(Utils.SERIAL_NUMBER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }
        }


        asset.setAssetName(request.assetName());
        asset.setProductName(request.productName());

        asset.setBrandName(request.brandName());
        asset.setSerialNumber(request.serialNumber());
        if (request.purchaseDate() != null) {
            String formattedDate = request.purchaseDate().replace("/", "-");
            asset.setPurchaseDate(Utils.stringToDate(formattedDate, Utils.USER_INPUT_DATE_FORMAT));
        }
        asset.setPrice(request.price());
        asset.setModeOfPayment(bankingRepository.findByBankId(request.bankingId()));
        asset.setCreatedBy(users.getUserId());
        asset.setCreatedAt(new java.util.Date());
        asset.setIsActive(true);
        asset.setIsDeleted(false);
        asset.setHostelId(hostelId);
        asset.setParentId(user.getParentId());
        assetsRepository.save(asset);
        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);
    }

    public ResponseEntity<?> updateAsset(UpdateAsset request, int assetId, String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }

        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());
        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_ASSETS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        AssetsV1 asset = assetsRepository.findByAssetId(assetId);
        if (asset == null) {
            return new ResponseEntity<>(Utils.INVALID_ASSET, HttpStatus.NOT_FOUND);
        }
        if (request.assetName() != null) {
            boolean assetNameExists = assetsRepository.existsByAssetNameAndIsDeletedFalseAndAssetIdNotAndHostelId(request.assetName(), assetId, hostelId);
            if (assetNameExists) {
                return new ResponseEntity<>(Utils.ASSET_NAME_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }
            asset.setAssetName(request.assetName());
        }
        if (request.productName() != null) asset.setProductName(request.productName());
        if (request.brandName() != null) asset.setBrandName(request.brandName());
        if (request.serialNumber() != null) {
            boolean serialNumberExists = assetsRepository.existsBySerialNumberAndIsDeletedFalseAndAssetIdNotAndHostelId(request.serialNumber(), assetId, hostelId);
            if (serialNumberExists) {
                return new ResponseEntity<>(Utils.SERIAL_NUMBER_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
            }
            asset.setSerialNumber(request.serialNumber());
        }
        if (request.purchaseDate() != null) {
            String formattedDate = request.purchaseDate().replace("/", "-");
            asset.setPurchaseDate(Utils.stringToDate(formattedDate, Utils.USER_INPUT_DATE_FORMAT));
        }
        if (request.price() != null) asset.setPrice(request.price());
        if (request.modeOfPayment() != null) {
            boolean bankingExist = bankingRepository.existsByHostelIdAndBankId(hostelId,request.modeOfPayment());
            if (!bankingExist) {
                return new ResponseEntity<>(Utils.INVALID_BANKING, HttpStatus.FORBIDDEN);
            }
            asset.setModeOfPayment(bankingRepository.findByBankId(request.modeOfPayment()));
        }
        if (request.createdBy() != null) asset.setCreatedBy(request.createdBy());
        if (request.isActive() != null) asset.setIsActive(request.isActive());
        asset.setUpdatedAt(new java.util.Date());
        assetsRepository.save(asset);

        return new ResponseEntity<>(
                Utils.UPDATED,
                HttpStatus.OK
        );
    }



    public ResponseEntity<?> getAssetById(Integer id) {
        if (id == null || id == 0) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);
        }
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        RolesV1 rolesV1 = rolesRepository.findByRoleId(user.getRoleId());

        if (rolesV1 == null) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_ASSETS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        AssetAssignmentResponse asset = assetsRepository.findAssetAssignmentDetailsById(id);
        if (asset != null) {
            return new ResponseEntity<>(asset, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);

    }

    public ResponseEntity<?> assignAsset(int assetId, AssignAsset request) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }

        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }

        RolesV1 role = rolesRepository.findByRoleId(user.getRoleId());
        if (role == null || !rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_ASSETS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        AssetsV1 asset = assetsRepository.findByAssetIdAndHostelId(assetId, request.hostelId());
        if (asset == null) {
            return new ResponseEntity<>(Utils.INVALID_ASSET, HttpStatus.NOT_FOUND);
        }

        if (request.floorId() != null) {
            Floors floor = floorRepository.findByFloorIdAndHostelId(request.floorId(), request.hostelId());
            if (floor == null) {
                return new ResponseEntity<>(Utils.INVALID_FLOOR, HttpStatus.BAD_REQUEST);
            }
            asset.setFloorId(request.floorId());
        }

        if (request.roomId() != null) {
            Rooms room = validateRoom(request, user);
            if (room == null) {
                return new ResponseEntity<>(Utils.N0_ROOM_FOUND_FLOOR, HttpStatus.BAD_REQUEST);
            }
            asset.setRoomId(request.roomId());
        }

        if (request.bedId() != null) {
            Beds bed = validateBed(request, user);
            if (bed == null) {
                return new ResponseEntity<>(Utils.N0_BED_FOUND_ROOM, HttpStatus.BAD_REQUEST);
            }
            asset.setBedId(request.bedId());
        }

        asset.setUpdatedAt(new Date());
        if (request.assignedAt() != null) {
            asset.setAssignedAt(Utils.stringToDate(
                    request.assignedAt().replace("/", "-"),
                    Utils.USER_INPUT_DATE_FORMAT
            ));
        }

        assetsRepository.save(asset);

        return ResponseEntity.ok(Utils.ASSIGNED);
    }

    public ResponseEntity<?> deleteAssetById(int assetId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users users = usersService.findUserByUserId(userId);
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ASSETS, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        AssetsV1 existingAsset = assetsRepository.findByAssetId(assetId);
        if (existingAsset != null) {
            existingAsset.setIsDeleted(true);
            existingAsset.setUpdatedAt(new Date());
            assetsRepository.save(existingAsset);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        }
        return new ResponseEntity<>("No Asset found", HttpStatus.BAD_REQUEST);

    }


    private Rooms validateRoom(AssignAsset request, Users user) {
        Rooms room = roomRepository.findByRoomIdAndParentIdAndHostelId(
                request.roomId(), user.getParentId(), request.hostelId()
        );
        if (room == null) return null;

        if (request.floorId() != null) {
            room = roomRepository.findByRoomIdAndParentIdAndHostelIdAndFloorId(
                    request.roomId(), user.getParentId(), request.hostelId(), request.floorId()
            );
        }
        return room;
    }

    private Beds validateBed(AssignAsset request, Users user) {
        Beds bed = bedRepository.findByBedIdAndParentIdAndHostelId(
                request.bedId(), user.getParentId(), request.hostelId()
        );
        if (bed == null) return null;

        if (request.roomId() != null) {
            bed = bedRepository.findByBedIdAndRoomIdAndHostelId(
                    request.bedId(), request.roomId(), request.hostelId()
            );
        }
        return bed;
    }

}
