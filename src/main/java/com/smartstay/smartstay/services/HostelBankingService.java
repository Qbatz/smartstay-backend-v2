package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.HostelBank;
import com.smartstay.smartstay.repositories.HostelBankRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HostelBankingService {

    @Autowired
    private HostelBankRepository hostelBankRepository;

    public ResponseEntity<?> addBankToHostel(String hostelId, String bankAccountId) {
        HostelBank hostelBankMapping = new HostelBank();
        hostelBankMapping.setHostelId(hostelId);
        hostelBankMapping.setBankAccountId(bankAccountId);

        hostelBankRepository.save(hostelBankMapping);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }


    public boolean checkBankAccountExists(List<String> existingAccounts, String hostelId) {
        List<HostelBank> listHostelBank = hostelBankRepository.findByHostelIdAndBankAccountIdIn(hostelId, existingAccounts);

        return listHostelBank != null && !listHostelBank.isEmpty();
    }

    public List<String> getAllBanksAccountNoBasedOnHostel(String hostelId) {
        return hostelBankRepository.findAllByHostelId(hostelId);
    }


}
