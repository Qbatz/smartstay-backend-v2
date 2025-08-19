package com.smartstay.smartstay.controllers;


import com.smartstay.smartstay.payloads.asset.AssetRequest;
import com.smartstay.smartstay.payloads.beds.AddBed;
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
public class AssetController {

    @Autowired
    private AssetsService assetsService;


    @GetMapping("/all-assets/{hostelId}")
    public ResponseEntity<?> getAllAssets(@PathVariable("hostelId") String hostelId) {
        return assetsService.getAllAssets(hostelId);
    }

    @GetMapping("/{assetId}")
    public ResponseEntity<?> getAssetId(@Valid @RequestBody AssetRequest request) {
        return assetsService.addAsset(request);
    }

    @PostMapping("/add-assets")
    public ResponseEntity<?> getAssetId(@PathVariable("assetId") int assetId) {
        return assetsService.getAssetById(assetId);
    }

}
