package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.LoginHistory;
import com.smartstay.smartstay.repositories.LoginHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class LoginHistoryService {

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    public void login(String userId, String parentId, String source, String platform) {
        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setSource(source);
        loginHistory.setPlatform(platform);
        loginHistory.setParentId(parentId);
        loginHistory.setUserId(userId);
        loginHistory.setLoginAt(new Date());


        loginHistoryRepository.save(loginHistory);
    }
}
