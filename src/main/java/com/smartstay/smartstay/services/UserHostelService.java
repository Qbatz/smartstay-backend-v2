package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.UserHostel;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.repositories.UserHostelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserHostelService {

    @Autowired
    private UserHostelRepository userHostelRepo;
    private UsersService usersService;

    @Autowired
    public void setUsersService(@Lazy UsersService usersService) {
        this.usersService = usersService;
    }

    public void mapUserHostel(String userId, String parentId, String hostelId) {
        UserHostel uh = new UserHostel();
        uh.setUserId(userId);
        uh.setHostelId(hostelId);
        uh.setParentId(parentId);

        userHostelRepo.save(uh);
    }

    public List<UserHostel> listAllUserHostel(String parentId, String userId) {
        return userHostelRepo.findAllByParentIdAndUserId(parentId, userId);
    }

    public List<UserHostel> listAllUsers(String parentId) {
        return userHostelRepo.findAllUserFromParentId(parentId);
    }

    public List<String> listAllUsersFromParentId(String parentId) {
        return userHostelRepo.findAllUserFromParentId(parentId)
                .stream()
                .map(UserHostel::getUserId)
                .toList();
    }

    public List<String> listAllUsersFromHostelId(String hostelId) {
        return userHostelRepo.findAllByHostelId(hostelId)
                .stream()
                .map(UserHostel::getUserId)
                .toList();
    }


    public List<UserHostel> findAllByHostelId(String hostelId) {
        return userHostelRepo.findAllByHostelId(hostelId);
    }

    public void deleteAllHostels(String hostelId) {
        List<UserHostel> listUsers = findAllByHostelId(hostelId);
        userHostelRepo.deleteAll(listUsers);
    }

    /**
     * this will lik with hostel service
     * @param parentId
     * @param hostelId
     */

    public int addHostelToExistingUsers(String parentId, String hostelId) {
        List<UserHostel> listUserHostel = listAllUsers(parentId);
        List<String> adminIds = listUserHostel
                .stream()
                .map(UserHostel::getUserId)
                .toList();
        List<String> adminUsers = usersService.findAdminUsers(adminIds);
        List<UserHostel> addHostelToExistingUsers = new ArrayList<>();

        if (!listUserHostel.isEmpty()) {
            adminUsers.forEach(item -> {
                UserHostel uh = new UserHostel();
                uh.setParentId(parentId);
                uh.setUserId(item);
                uh.setHostelId(hostelId);

                addHostelToExistingUsers.add(uh);
            });

            userHostelRepo.saveAll(addHostelToExistingUsers);

            return 200;
        }
        else {
           return 100;
        }

    }

    /**
     * this will link with user service
     * @param parentId
     * @param userId
     */

    public void addUserToExistingHostel(String parentId, String userId, String loggedInUserId) {
        List<UserHostel> listUserHostel = listAllUserHostel(parentId, loggedInUserId);

        List<UserHostel> newListForNewUser = new ArrayList<>();

        if (!listUserHostel.isEmpty()) {
            listUserHostel.forEach(item -> {
                UserHostel userHostel = new UserHostel();
                userHostel.setHostelId(item.getHostelId());
                userHostel.setUserId(userId);
                userHostel.setParentId(parentId);

                newListForNewUser.add(userHostel);
            });
            if (!newListForNewUser.isEmpty()) {
                userHostelRepo.saveAll(newListForNewUser);
            }
        }
    }

    public void addHostelToExistingUsers(String parentId, List<Users> listUsers, String hostelId) {
        List<UserHostel> listUserHostel = new ArrayList<>();
        listUsers.forEach(item -> {
            UserHostel uh = new UserHostel();
            uh.setParentId(parentId);
            uh.setHostelId(hostelId);
            uh.setUserId(item.getUserId());

            listUserHostel.add(uh);
        });
        if (!listUserHostel.isEmpty()) {
            userHostelRepo.saveAll(listUserHostel);
        }
    }

    public List<UserHostel> findByUserId(String userId) {
        return userHostelRepo.findByUserId(userId);
    }

    public UserHostel findByUserIdAndHostelId(String userId, String hostelId) {
        return userHostelRepo.findByUserIdAndHostelId(userId, hostelId);
    }

    public void deleteUserFromHostel(String userId, String hostelId) {
        UserHostel userHostel = findByUserIdAndHostelId(userId, hostelId);
        userHostelRepo.delete(userHostel);
    }

    public boolean checkHostelAccess(String userId, String hostelId) {
        return userHostelRepo.findByUserIdAndHostelId(userId, hostelId) != null;
    }
}
