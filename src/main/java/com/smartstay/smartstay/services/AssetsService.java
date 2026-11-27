package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.assets.AssetAssignmentResponseMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.assets.AssetAssignmentResponse;
import com.smartstay.smartstay.dto.assets.AssetsMobileList;
import com.smartstay.smartstay.dto.bank.OldBankToNewBankInfo;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.payloads.asset.AssetRequest;
import com.smartstay.smartstay.payloads.asset.AssignAsset;
import com.smartstay.smartstay.payloads.asset.UpdateAsset;
import com.smartstay.smartstay.repositories.*;
import com.smartstay.smartstay.responses.assets.BankForAssets;
import com.smartstay.smartstay.responses.assets.InitializeAssets;
import com.smartstay.smartstay.responses.assets.Vendors;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssetsService {

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    HostelV1Repository hostelV1Repository;

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
    @Autowired
    private BankTransactionService bankTransactionService;
    @Autowired
    private SubscriptionService subscriptionService;
    @Autowired
    private BankingServiceV2 bankingServiceV2;
    @Autowired
    private BankingService bankingService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private VendorService vendorService;
    @Autowired
    private FloorsService floorsService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private BedsService bedsService;

    public ResponseEntity<?> getAllAssets(String hostelId, int page, int size, String keyword) {

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

        HostelV1 hostel = hostelService.getHostelInfo(hostelId);
        if (hostel == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        String searchKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();

        List<AssetsV1> assets = searchKeyword == null
                ? assetsRepository.findAllByHostelIdAndIsActiveTrueAndIsDeletedFalseOrderByCreatedAtDesc(hostelId)
                : assetsRepository.findAllByHostelIdAndIsActiveTrueAndIsDeletedFalseAndAssetNameContainingIgnoreCaseOrderByCreatedAtDesc
                (hostelId, searchKeyword);

        Set<Integer> vendorIds = new HashSet<>();
        Set<Integer> floorIds = new HashSet<>();
        Set<Integer> roomIds = new HashSet<>();
        Set<Integer> bedIds = new HashSet<>();
        Set<String> bankIds = new HashSet<>();

        for (AssetsV1 asset : assets) {

            vendorIds.add(asset.getVendorId());
            if (asset.getFloorId() != null){
                floorIds.add(asset.getFloorId());
            }
            if (asset.getRoomId() != null){
                roomIds.add(asset.getRoomId());
            }
            if (asset.getBedId() != null){
                bedIds.add(asset.getBedId());
            }
            if (asset.getBankId() != null){
                bankIds.add(asset.getBankId());
            }
        }

        List<VendorV1> vendors = vendorService
                .getAllVendorsByHostelIdAndVendorIds(hostelId, new ArrayList<>(vendorIds));
        Map<Integer, VendorV1> vendorMap = vendors.stream()
                .collect(Collectors.toMap(VendorV1::getVendorId, vendor -> vendor));

        List<Floors> floors = floorsService
                .getByFloorIds(floorIds);
        Map<Integer, Floors> floorsMap = floors.stream()
                .collect(Collectors.toMap(Floors::getFloorId, floor -> floor));

        List<Rooms> rooms = roomsService
                .getAllByRoomIds(roomIds);
        Map<Integer, Rooms> roomsMap = rooms.stream()
                .collect(Collectors.toMap(Rooms::getRoomId, room -> room));

        List<Beds> beds = bedsService
                .getAllByBedIds(bedIds);
        Map<Integer, Beds> bedsMap = beds.stream()
                .collect(Collectors.toMap(Beds::getBedId, bed -> bed));

        List<BankingV2> banks = bankingServiceV2
                .findAllByBankId(bankIds);
        Map<String, BankingV2> bankingMap = banks.stream()
                .collect(Collectors.toMap(BankingV2::getBankId, bank -> bank));

        List<AssetAssignmentResponse> listAssets = assets.stream()
                .map(i -> {

                    VendorV1 vendor = vendorMap.getOrDefault(i.getVendorId(), null);
                    Floors floor = floorsMap.getOrDefault(i.getFloorId(), null);
                    Rooms room = roomsMap.getOrDefault(i.getRoomId(), null);
                    Beds bed = bedsMap.getOrDefault(i.getBedId(), null);
                    BankingV2 bank = bankingMap.getOrDefault(i.getBankId(), null);

                    return new AssetAssignmentResponseMapper(hostel, vendor, floor, room, bed, bank)
                            .apply(i);
                })
                .toList();

        if ("web".equalsIgnoreCase(authentication.getSource())) {
            return new ResponseEntity<>(listAssets, HttpStatus.OK);
        }

        int totalAssets = listAssets.size();
        int itemPerPage = size <= 0 ? 10 : size;
        int currentPage = page <= 0 ? 1 : page;
        int totalPages = (int) Math.ceil((double) totalAssets / itemPerPage);

        long offset = (long) (currentPage - 1) * itemPerPage;
        int fromIndex = (int) Math.min(offset, totalAssets);
        int toIndex = (int) Math.min(offset + itemPerPage, totalAssets);
        List<AssetAssignmentResponse> pagedAssets = new ArrayList<>(listAssets.subList(fromIndex, toIndex));

        AssetsMobileList response = new AssetsMobileList(hostelId, currentPage, totalPages, itemPerPage, pagedAssets);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> addAsset(AssetRequest request, String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.INVALID_USER, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_ASSETS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        boolean hostelV1 = userHostelService.checkHostelAccess(user.getUserId(), hostelId);
        if (!hostelV1) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        if (!bankingServiceV2.checkBankExist(request.bankingId())) {
            return new ResponseEntity<>(Utils.INVALID_BANKING, HttpStatus.FORBIDDEN);
        }
        OldBankToNewBankInfo oldBankToNewBankInfo = bankingService.getOldBankNewBankInfo(request.bankingId());
        if (oldBankToNewBankInfo == null) {
            return new ResponseEntity<>(Utils.INVALID_BANKING, HttpStatus.FORBIDDEN);
        }


        AssetsV1 asset = new AssetsV1();
        if (request.vendorId() != null && request.vendorId() != 0) {
            VendorV1 vendorV1 = vendorRepository.findByVendorIdAndHostelId(request.vendorId(), hostelId);
            if (vendorV1 == null) {
                return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.FORBIDDEN);
            }
            asset.setVendorId(request.vendorId());
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
        Date purchaseDate = new Date();
        if (request.purchaseDate() != null) {
            String formattedDate = request.purchaseDate().replace("/", "-");
            purchaseDate = Utils.stringToDate(formattedDate, Utils.USER_INPUT_DATE_FORMAT);
            asset.setPurchaseDate(purchaseDate);
        }
        asset.setPrice(request.price());
        asset.setBankId(oldBankToNewBankInfo.bankId());
        asset.setBankingMethodId(oldBankToNewBankInfo.paymentMethodId());
        asset.setCreatedBy(user.getUserId());
        asset.setCreatedAt(new Date());
        asset.setIsActive(true);
        asset.setIsDeleted(false);
        asset.setHostelId(hostelId);
        asset.setParentId(user.getParentId());
        asset.setPlatform(authentication.getSource());
        AssetsV1 av1 = assetsRepository.save(asset);

        bankTransactionService.addAssetsTransaction(av1.getAssetId(), request.price(), purchaseDate, oldBankToNewBankInfo.bankId(), oldBankToNewBankInfo.paymentMethodId(), hostelId, request.serialNumber());
        usersService.addUserLog(hostelId, String.valueOf(av1.getAssetId()), ActivitySource.ASSETS, ActivitySourceType.CREATE, user);
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

        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        AssetsV1 asset = assetsRepository.findByAssetId(assetId);
        if (asset == null) {
            return new ResponseEntity<>(Utils.INVALID_ASSET, HttpStatus.NOT_FOUND);
        }

       if (request.vendorId()!=null){
           VendorV1 vendorV1 = vendorRepository.findByVendorIdAndHostelId(request.vendorId(), hostelId);
              if (vendorV1 == null) {
                return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.FORBIDDEN);
              }else {
                asset.setVendorId(request.vendorId());
              }
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
            Date purchaseDate = Utils.stringToDate(formattedDate, Utils.USER_INPUT_DATE_FORMAT);
            if (purchaseDate == null) {
                return new ResponseEntity<>(Utils.INVALID_PURCHASE_DATE, HttpStatus.BAD_REQUEST);
            }
            if (asset.getAssignedAt() != null && purchaseDate.after(asset.getAssignedAt())) {
                return new ResponseEntity<>(Utils.PURCHASE_DATE_AFTER_ASSIGNED_DATE, HttpStatus.BAD_REQUEST);
            }
            asset.setPurchaseDate(purchaseDate);
        }
        if (request.price() != null) asset.setPrice(request.price());
        if (request.modeOfPayment() != null) {
//            boolean bankingExist = bankingRepository.existsByHostelIdAndBankId(hostelId,request.modeOfPayment());
//            if (!bankingExist) {
//                return new ResponseEntity<>(Utils.INVALID_BANKING, HttpStatus.FORBIDDEN);
//            }
            if (!bankingServiceV2.checkBankExist(request.modeOfPayment())) {
                return new ResponseEntity<>(Utils.INVALID_BANKING, HttpStatus.FORBIDDEN);
            }
            asset.setBankId(request.modeOfPayment());
        }
        if (request.createdBy() != null) asset.setCreatedBy(request.createdBy());
        if (request.isActive() != null) asset.setIsActive(request.isActive());
        asset.setUpdatedAt(new java.util.Date());
        assetsRepository.save(asset);

        usersService.addUserLog(hostelId, String.valueOf(assetId), ActivitySource.ASSETS, ActivitySourceType.UPDATE, user);

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

        AssetsV1 asset = assetsRepository
                .findByAssetIdAndIsActiveTrueAndIsDeletedFalse(Long.valueOf(id));
        if (asset == null) {
            return new ResponseEntity<>(Utils.INVALID_ASSET, HttpStatus.BAD_REQUEST);
        }

        String hostelId = asset.getHostelId();
        Integer vendorId = asset.getVendorId();
        Integer floorId = asset.getFloorId();
        Integer roomId = asset.getRoomId();
        Integer bedId = asset.getBedId();
        String bankId = asset.getBankId();

        HostelV1 hostel = null;
        VendorV1 vendor = null;
        Floors floor = null;
        Rooms room = null;
        Beds bed = null;
        BankingV2 bank = null;

        if (hostelId != null) {
            hostel = hostelService.getHostelInfo(hostelId);
        }
        vendor = vendorService.findVendorByHostelIdAndVendorId(hostelId, vendorId);
        if (floorId != null) {
            floor = floorsService.getByFloorId(floorId);
        }
        if (roomId != null) {
            room = roomsService.findRoomByRoomId(roomId);
        }
        if (bedId != null) {
            bed = bedsService.findBedById(bedId);
        }
        if (bankId != null) {
            bank = bankingServiceV2.getBankDetails(bankId);
        }

        AssetAssignmentResponse assetResponse = new AssetAssignmentResponseMapper
                (hostel, vendor, floor, room, bed, bank)
                .apply(asset);

        return new ResponseEntity<>(assetResponse, HttpStatus.OK);
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

        if (!subscriptionService.validateSubscription(request.hostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
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
        if (existingAsset == null) {
            return new ResponseEntity<>("No Asset found", HttpStatus.BAD_REQUEST);
        }
        if (!subscriptionService.validateSubscription(existingAsset.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        if (existingAsset.getFloorId() != null || existingAsset.getRoomId() != null || existingAsset.getBedId() != null) {
            return new ResponseEntity<>(Utils.CANNOT_DELETE_ASSIGNED_ASSETS, HttpStatus.BAD_REQUEST);
        }
        existingAsset.setIsDeleted(true);
        existingAsset.setUpdatedAt(new Date());
        assetsRepository.save(existingAsset);
        bankTransactionService.deleteAssets(existingAsset.getHostelId(), existingAsset.getAssetId());
        return new ResponseEntity<>("Deleted", HttpStatus.OK);
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

    public Double getAllAssetsValue(String hostelId) {
        List<AssetsV1> listAsstes = assetsRepository.findAllByHostelId(hostelId);
        double assetValue = listAsstes
                .stream()
                .mapToDouble(AssetsV1::getPrice)
                .sum();

        return Utils.roundOfDouble(assetValue);
    }

    public ResponseEntity<?> initializeAssets(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_ASSETS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        List<Vendors> listVendors = vendorService.getAllVendorsForAssets(hostelId);
        List<BankForAssets> listBankForAssets = bankingServiceV2.getAllBankForAssets(hostelId);
        InitializeAssets initializeAssets = new InitializeAssets(listVendors, listBankForAssets);
        return new ResponseEntity<>(initializeAssets, HttpStatus.OK);
    }
}
