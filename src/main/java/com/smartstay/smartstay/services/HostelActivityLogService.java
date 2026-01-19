package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.HostelActivityLog;
import com.smartstay.smartstay.dao.Users;

import com.smartstay.smartstay.repositories.HostelActivityLogRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class HostelActivityLogService {

    @Autowired
    public Authentication authentication;

    @Autowired
    private HostelActivityLogRepository activityLogRepository;

//    @Autowired
//    private UsersService userService;

//    @Autowired
//    private UserHostelService userHostelService;

//    @Autowired
//    private RolesService rolesService;

    public HostelActivityLog saveActivityLog(Date loggedAt, String hostelId, String parentId, String sourceId,
            String source, String eventType) {
        return saveActivityLog(loggedAt, hostelId, parentId, sourceId, source, eventType, null);
    }

    public HostelActivityLog saveActivityLog(Date loggedAt, String hostelId, String parentId, String sourceId,
            String source, String eventType, String description) {
        HostelActivityLog hostelActivityLog = new HostelActivityLog();
        hostelActivityLog.setHostelId(hostelId);
        hostelActivityLog.setParentId(parentId);
        hostelActivityLog.setUserId(authentication != null ? authentication.getName() : null);
        hostelActivityLog.setSourceId(sourceId);
        hostelActivityLog.setSource(source);
        hostelActivityLog.setEventType(eventType);
        hostelActivityLog.setLoggedAt(loggedAt);
        hostelActivityLog.setCreatedAt(new Date());
        return activityLogRepository.save(hostelActivityLog);
    }

    public ResponseEntity<?> getActivityLogs(String hostelId, String search, int page, int size) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
//        Users user = userService.findUserByUserId(userId);
//        if (user == null) {
//            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
//        }

//        if (!userHostelService.checkHostelAccess(userId, hostelId)) {
//            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
//        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<HostelActivityLog> logs = activityLogRepository.searchByHostelId(hostelId, pageable);

        return new ResponseEntity<>(logs, HttpStatus.OK);
    }
}
