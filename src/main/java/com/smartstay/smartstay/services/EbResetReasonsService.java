package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.EBResetReasons;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.repositories.EBResetReasonsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class EbResetReasonsService {
    @Autowired
    private EBResetReasonsRepository ebResetReasonsRepository;

    public void resetReason(Long id, Users users, Date resetDate, String reason, String hostelId) {
        EBResetReasons ebResetReasons = new EBResetReasons();
        ebResetReasons.setResetReason(reason);
        ebResetReasons.setHostelId(hostelId);
        ebResetReasons.setResetReadingId(id);
        ebResetReasons.setResetBy(users.getUserId());
        ebResetReasons.setCreatedBy(users.getUserId());
        ebResetReasons.setResetOn(resetDate);


        ebResetReasonsRepository.save(ebResetReasons);
    }
}
