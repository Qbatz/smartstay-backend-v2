package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.HostelActivityLog;
import com.smartstay.smartstay.repositories.HostelActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class HostelActivityLogService {


    @Autowired
    public Authentication authentication;

    @Autowired
    private HostelActivityLogRepository activityLogRepository;

    public HostelActivityLog saveActivityLog(Date loggedAt,String hostelId, String parentId, String sourceId, String source, String eventType) {
        HostelActivityLog hostelActivityLog = new HostelActivityLog();
        hostelActivityLog.setHostelId(hostelId);
        hostelActivityLog.setParentId(parentId);
        hostelActivityLog.setUserId(authentication.getName());
        hostelActivityLog.setSourceId(sourceId);
        hostelActivityLog.setSource(source);
        hostelActivityLog.setEventType(eventType);
        hostelActivityLog.setLoggedAt(loggedAt);
        hostelActivityLog.setCreatedAt(new Date());
        return activityLogRepository.save(hostelActivityLog);
    }
}
