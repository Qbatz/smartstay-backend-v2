package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.UserHostel;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.repositories.UserHostelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserHostelService {

    @Autowired
    private UserHostelRepository userHostelRepo;

    public void mapUserHostel(String userId, String hostelId, String parentId) {
        List<UserHostel> listHostels = userHostelRepo.findAllByHostelId(hostelId);
        UserHostel uh = new UserHostel();
        uh.setUserId(userId);
        uh.setHostelId(hostelId);
        uh.setParentId(parentId);
        listHostels.add(uh);

        userHostelRepo.saveAll(listHostels);
    }

    public List<UserHostel> listAllUserHostel(String parentId) {
        return userHostelRepo.findAllByParentId(parentId);
    }

    public List<UserHostel> listAllUsers(String parentId) {
        return userHostelRepo.findAllUserFromParentId(parentId);
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
        List<UserHostel> addHostelToExistingUsers = new ArrayList<>();

        if (!listUserHostel.isEmpty()) {
            listUserHostel.forEach(item -> {
                UserHostel uh = new UserHostel();
                uh.setParentId(parentId);
                uh.setUserId(item.getUserId());
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

    public void addUserToExistingHostel(String parentId, String userId) {
        List<UserHostel> listUserHostel = listAllUserHostel(parentId);

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
