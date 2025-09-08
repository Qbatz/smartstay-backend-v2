package com.smartstay.smartstay.responses.assets;


public record AssetResponse(int id, String assetName, String brandName, String productName, String serialNumber, Double price, String purchaseDate) {
}
