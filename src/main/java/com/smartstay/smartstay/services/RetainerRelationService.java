package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.RetainerRelations;
import com.smartstay.smartstay.payloads.retainer.LoadBalance;
import com.smartstay.smartstay.repositories.RetainerRelationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RetainerRelationService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private RetainerRelationsRepository retainerRelationsRepository;

    public void addRelationForDeposit(String customerId, String hostelId, LoadBalance loadBalance, boolean isRegisteredRelation, InvoicesV1 invoice) {
        RetainerRelations retainerRelations = new RetainerRelations();
        if (isRegisteredRelation) {
            retainerRelations.setRelationId(loadBalance.relationId());
        }
        else {
            retainerRelations.setRelationName(loadBalance.relationName());
            retainerRelations.setRelationMobile(loadBalance.mobile());
        }
        retainerRelations.setCustomerId(customerId);
        retainerRelations.setHostelId(hostelId);
        retainerRelations.setInvoiceId(invoice.getInvoiceId());
        retainerRelationsRepository.save(retainerRelations);

    }
}
