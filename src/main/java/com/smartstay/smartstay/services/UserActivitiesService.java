package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.UserActivities;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.repositories.UserActivitiesRepositories;
import com.smartstay.smartstay.util.ActivityLogUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserActivitiesService {
    @Autowired
    private UserActivitiesRepositories userActivitiesRepositories;
    @Autowired
    private Authentication authentication;

    private UsersService usersService;

    @Autowired
    public void setUsersService(@Lazy UsersService usersService) {
        this.usersService = usersService;
    }

    public void createUser( String source, String operation,  Users users) {
        Date loggedAt = new Date();

        UserActivities userActivities = new UserActivities();
        userActivities.setDescription(ActivityLogUtils.getActivityDescription(source, operation));
        userActivities.setUserId(users.getUserId());
        userActivities.setLoggedAt(loggedAt);
        userActivities.setCreatedAt(new Date());
        userActivities.setParentId(users.getParentId());
        userActivities.setSource(source);
        userActivities.setSourceId(users.getUserId());
        userActivities.setActivityType(operation);
        userActivities.setHostelId(null);

        userActivitiesRepositories.save(userActivities);

    }

    public void addLogBasedOnProfile( String source, String operation,  Users users) {
        Date loggedAt = new Date();

        UserActivities userActivities = new UserActivities();
        userActivities.setDescription(ActivityLogUtils.getActivityDescription(source, operation));
        userActivities.setUserId(users.getUserId());
        userActivities.setLoggedAt(loggedAt);
        userActivities.setCreatedAt(new Date());
        userActivities.setParentId(users.getParentId());
        userActivities.setSource(source);
        userActivities.setSourceId(users.getUserId());
        userActivities.setActivityType(operation);
        userActivities.setHostelId(null);
        userActivities.setPlatform(authentication.getSource());

        userActivitiesRepositories.save(userActivities);

    }

    public void addLoginLog(String hostelId, String date, String source, String operation, String sourceId, Users users) {
        Date loggedAt = new Date();
        if (date != null && !date.trim().equalsIgnoreCase("")) {
            loggedAt = Utils.stringToDate(date.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }

        UserActivities userActivities = new UserActivities();
        userActivities.setDescription(ActivityLogUtils.getActivityDescription(source, operation));
        userActivities.setUserId(users.getUserId());
        userActivities.setLoggedAt(loggedAt);
        userActivities.setCreatedAt(new Date());
        userActivities.setParentId(users.getParentId());
        userActivities.setSource(source);
        userActivities.setSourceId(sourceId);
        userActivities.setActivityType(operation);
        userActivities.setHostelId(hostelId);
        userActivities.setPlatform(authentication.getSource());

        userActivitiesRepositories.save(userActivities);

    }

    public void addLoginLogWithType(String hostelId, String date, String source, String operation, String sourceId, Users users, String type) {
        Date loggedAt = new Date();
        if (date != null && !date.trim().equalsIgnoreCase("")) {
            loggedAt = Utils.stringToDate(date.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }

        UserActivities userActivities = new UserActivities();
        userActivities.setDescription(ActivityLogUtils.getActivityDescriptionWithType(source, operation, type));
        userActivities.setUserId(users.getUserId());
        userActivities.setLoggedAt(loggedAt);
        userActivities.setCreatedAt(new Date());
        userActivities.setParentId(users.getParentId());
        userActivities.setSource(source);
        userActivities.setSourceId(sourceId);
        userActivities.setActivityType(operation);
        userActivities.setHostelId(hostelId);
        userActivities.setPlatform(authentication.getSource());

        userActivitiesRepositories.save(userActivities);

    }

    public void addLoginLog(String hostelId, String date, String source, String operation, String sourceId, Users users, List<String> customerIds) {
        Date loggedAt = new Date();
        if (date != null && !date.trim().equalsIgnoreCase("")) {
            loggedAt = Utils.stringToDate(date.replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        }

        UserActivities userActivities = new UserActivities();
        userActivities.setDescription(ActivityLogUtils.getActivityDescription(source, operation));
        userActivities.setUserId(users.getUserId());
        userActivities.setLoggedAt(loggedAt);
        userActivities.setCreatedAt(new Date());
        userActivities.setParentId(users.getParentId());
        userActivities.setSource(source);
        userActivities.setSourceId(sourceId);
        userActivities.setActivityType(operation);
        userActivities.setHostelId(hostelId);
        userActivities.setPlatform(authentication.getSource());

        if (customerIds != null && !customerIds.isEmpty()) {
            userActivities.setTenantIds(customerIds);
        }

        userActivitiesRepositories.save(userActivities);
    }
}
