package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.AssetMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.AssetsV1;
import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.repositories.AssetsRepository;
import com.smartstay.smartstay.repositories.RolesRepository;
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
