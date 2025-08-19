package com.smartstay.smartstay.controllers;


import com.smartstay.smartstay.services.AssetsService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> getAssetId(@PathVariable("assetId") int assetId) {
        return assetsService.getAssetById(assetId);
    }

}
