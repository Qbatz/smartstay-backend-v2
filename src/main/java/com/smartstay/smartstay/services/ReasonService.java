package com.smartstay.smartstay.services;


import com.smartstay.smartstay.dao.Reasons;
import com.smartstay.smartstay.repositories.ReasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReasonService {

    @Autowired
    public ReasonRepository reasonRepository;


    public void SaveReason(Reasons reasons){
        reasonRepository.save(reasons);
    }
}
