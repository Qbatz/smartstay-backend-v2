package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.AssetsV1;
import com.smartstay.smartstay.responses.assets.AssetResponse;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class AssetMapper implements Function<AssetsV1, AssetResponse> {

    @Override
    public AssetResponse apply(AssetsV1 assets) {

        return new AssetResponse(assets.getAssetId(),
                assets.getAssetName(), assets.getBrandName(),
                assets.getProductName(), assets.getSerialNumber(),
                assets.getPrice(),
                assets.getPurchaseDate() != null ? Utils.dateToString(assets.getPurchaseDate()) : null);
    }

    public List<AssetResponse> mapList(List<AssetsV1> assets) {
        return assets.stream()
                .map(this)
                .toList();
    }
}