package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.AssetsV1;
import com.smartstay.smartstay.responses.assets.AssetResponse;

import java.util.function.Function;

public class AssetMapper implements Function<AssetsV1, AssetResponse> {
    @Override
    public AssetResponse apply(AssetsV1 assets) {
        return new AssetResponse(assets.getAssetId(), assets.getAssetName(), assets.getBrandName());
    }
}