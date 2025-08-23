package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.AssetMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.payloads.asset.AssetRequest;
import com.smartstay.smartstay.payloads.asset.UpdateAsset;
import com.smartstay.smartstay.repositories.AssetsRepository;
import com.smartstay.smartstay.repositories.HostelV1Repository;
import com.smartstay.smartstay.repositories.RolesRepository;
import com.smartstay.smartstay.repositories.VendorRepository;
import com.smartstay.smartstay.responses.assets.AssetResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetsService {

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    HostelV1Repository hostelV1Repository;

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
        List<AssetsV1> listAssets = assetsRepository.findAllByHostelId(hostelId);
        List<AssetResponse> assetResponse = listAssets.stream().map(item -> new AssetMapper().apply(item)).toList();
        return new ResponseEntity<>(assetResponse, HttpStatus.OK);
    }


    public ResponseEntity<?> addAsset(AssetRequest request) {
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
        VendorV1 vendorV1 = vendorRepository.findByVendorIdAndHostelId(request.vendorId(), request.hostelId());
        HostelV1 hostelV1 = hostelV1Repository.findByHostelIdAndParentId(request.hostelId(), users.getParentId());
        if (hostelV1 == null) {
            return new ResponseEntity<>("Invalid Hostel", HttpStatus.FORBIDDEN);
        }
        if (vendorV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.FORBIDDEN);
        }

        AssetsV1 asset = new AssetsV1();
        asset.setAssetName(request.assetName());
        asset.setProductName(request.productName());
        asset.setVendorId(request.vendorId());
        asset.setBrandName(request.brandName());
        asset.setSerialNumber(request.serialNumber());
        if (request.purchaseDate() != null) {
            String formattedDate = request.purchaseDate().replace("-", "/");
            asset.setPurchaseDate(Utils.stringToDate(formattedDate, Utils.DATE_FORMAT_YY));
        }
        asset.setPrice(request.price());
        asset.setModeOfPayment(request.modeOfPayment());
        asset.setCreatedBy(request.createdBy());
        asset.setCreatedAt(new java.util.Date());
        asset.setIsActive(true);
        asset.setHostelId(request.hostelId());
        asset.setParentId(user.getParentId());
        AssetsV1 saved = assetsRepository.save(asset);
        return new ResponseEntity<>(new AssetResponse(saved.getAssetId(), saved.getAssetName(), saved.getBrandName()), HttpStatus.OK);
    }

    public ResponseEntity<?> updateAsset(UpdateAsset request, int assetId) {
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
        if (request.assetName() != null) asset.setAssetName(request.assetName());
        if (request.productName() != null) asset.setProductName(request.productName());
        if (request.brandName() != null) asset.setBrandName(request.brandName());
        if (request.serialNumber() != null) asset.setSerialNumber(request.serialNumber());
        if (request.purchaseDate() != null) {
            String formattedDate = request.purchaseDate().replace("-", "/");
            asset.setPurchaseDate(Utils.stringToDate(formattedDate, Utils.DATE_FORMAT_YY));
        }
        if (request.price() != null) asset.setPrice(request.price());
        if (request.modeOfPayment() != null) asset.setModeOfPayment(request.modeOfPayment());
        if (request.createdBy() != null) asset.setCreatedBy(request.createdBy());
        if (request.isActive() != null) asset.setIsActive(request.isActive());
        asset.setUpdatedAt(new java.util.Date());
        asset.setParentId(user.getParentId());

        AssetsV1 saved = assetsRepository.save(asset);

        return new ResponseEntity<>(
                new AssetResponse(saved.getAssetId(), saved.getAssetName(), saved.getBrandName()),
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
        AssetsV1 asset = assetsRepository.findByAssetId(id);
        if (asset != null) {
            AssetResponse assetResponse = new AssetMapper().apply(asset);
            return new ResponseEntity<>(assetResponse, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.INVALID, HttpStatus.NO_CONTENT);

    }
}
