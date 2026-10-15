package com.smartstay.smartstay.responses.assets;

import java.util.List;

public record InitializeAssets(List<Vendors> listVendors,
                               List<BankForAssets> listBanks) {
}
