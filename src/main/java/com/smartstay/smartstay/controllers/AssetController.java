package com.smartstay.smartstay.controllers;


import com.smartstay.smartstay.payloads.asset.AssetRequest;
import com.smartstay.smartstay.payloads.asset.AssignAsset;
import com.smartstay.smartstay.payloads.asset.UpdateAsset;
import com.smartstay.smartstay.payloads.complaints.AssignUser;
import com.smartstay.smartstay.services.AssetsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v2/assets")
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SecurityRequirement(name = "Authorization")
@CrossOrigin("*")
public class AssetController {

    @Autowired
    private AssetsService assetsService;

    @GetMapping("/{hostelId}")
    public ResponseEntity<?> getAllAssets(@PathVariable("hostelId") String hostelId) {
        return assetsService.getAllAssets(hostelId);
    }

    @PostMapping("/{hostelId}")
    public ResponseEntity<?> addAsset(@Valid @RequestBody AssetRequest request,@PathVariable("hostelId") String hostelId) {
        return assetsService.addAsset(request,hostelId);
    }

    @GetMapping("/{hostelId}/{assetId}")
    public ResponseEntity<?> getAssetId(@PathVariable("assetId") int assetId) {
        return assetsService.getAssetById(assetId);
    }

    @PutMapping("/{hostelId}/{assetId}")
    public ResponseEntity<?> updateAsset(@RequestBody UpdateAsset updateAsset, @PathVariable("assetId") int assetId,@PathVariable("hostelId") String hostelId) {
        return assetsService.updateAsset(updateAsset, assetId,hostelId);
    }

    @PutMapping("/assign/{assetId}")
    public ResponseEntity<?> assignAsset(@PathVariable("assetId") int assetId, @Valid @RequestBody AssignAsset request) {
        return assetsService.assignAsset(assetId, request);
    }

    @DeleteMapping("/{assetId}")
    public ResponseEntity<?> deleteAssetById(@PathVariable("assetId") int assetId) {
        return assetsService.deleteAssetById(assetId);
    }

}
