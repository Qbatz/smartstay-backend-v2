package com.smartstay.smartstay.Wrappers.banking;

import com.smartstay.smartstay.dao.QrBankType;
import com.smartstay.smartstay.responses.banking.QrBankTypeResponse;
import com.smartstay.smartstay.util.Utils;

import java.util.function.Function;

public class QrBankTypeMapper implements Function<QrBankType, QrBankTypeResponse> {

    @Override
    public QrBankTypeResponse apply(QrBankType entity) {
        return new QrBankTypeResponse(
                entity.getId(),
                entity.getType() != null ? entity.getType().name() : null,
                entity.getName(),
                entity.getImage(),
                Utils.dateToTableFormat(entity.getCreatedAt()),
                Utils.dateToTableFormat(entity.getUpdatedAt()),
                entity.getCreatedBy(),
                entity.getUpdatedBy());
    }
}
