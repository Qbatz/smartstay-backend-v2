package com.smartstay.smartstay.Wrappers.complaints;

import com.smartstay.smartstay.dao.ComplaintComments;
import com.smartstay.smartstay.dao.ComplaintUpdates;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.ComplaintStatus;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.util.Utils;
import org.apache.catalina.User;

import java.util.List;
import java.util.function.Function;

public class ComplaintUpdatesMapper implements Function<ComplaintUpdates, com.smartstay.smartstay.responses.complaint.ComplaintUpdates> {

    List<Customers> listCustomers = null;
    List<Users> listUsers = null;
    List<ComplaintComments> complaintComments;
    List<Users> assignedUsers = null;
    String description = null;
    String complaintType = null;

    public ComplaintUpdatesMapper(List<Customers> listCustomers, List<Users> listUsers, List<ComplaintComments> complaintComments, String title, List<Users> assignedUsers, String complaintType) {
        this.listCustomers = listCustomers;
        this.listUsers = listUsers;
        this.complaintComments = complaintComments;
        this.description = title;
        this.assignedUsers = assignedUsers;
        this.complaintType = complaintType;
    }

    @Override
    public com.smartstay.smartstay.responses.complaint.ComplaintUpdates apply(ComplaintUpdates complaintUpdates) {
        StringBuilder initials = new StringBuilder();
        StringBuilder fullName = new StringBuilder();
        String profilePic = null;
        String update = null;
        String updatedAt = null;
        String updatedTime = null;
        String complaintTitle = null;
        String assignee = null;

        if (complaintUpdates.getStatus().equalsIgnoreCase(ComplaintStatus.ASSIGNED.name())) {
            Users usr = assignedUsers
                    .stream()
                    .filter(i -> i.getUserId().equalsIgnoreCase(complaintUpdates.getAssignedTo()))
                    .findFirst()
                    .orElse(null);
            if (usr != null) {
                assignee = usr.getFirstName();
                if (usr.getLastName() != null) {
                    assignee = assignee + " " + usr.getLastName();
                }
            }
        }


        if (complaintUpdates.getUserType().equalsIgnoreCase(UserType.TENANT.name())) {
            if (listCustomers != null) {
                Customers customer = listCustomers
                        .stream()
                        .filter(i -> i.getCustomerId().equalsIgnoreCase(complaintUpdates.getUpdatedBy()))
                        .findFirst()
                        .orElse(null);

                if (customer != null) {
                    profilePic = customer.getProfilePic();
                    initials.append(customer.getFirstName().toUpperCase().charAt(0));
                    fullName.append(customer.getFirstName());
                    if (customer.getLastName() != null && !customer.getLastName().trim().equalsIgnoreCase("")) {
                        initials.append(customer.getLastName().toUpperCase().charAt(0));
                        fullName.append(" ");
                        fullName.append(customer.getLastName());
                    }
                    else {
                        if (customer.getFirstName() != null && customer.getFirstName().length() > 1) {
                            initials.append(customer.getFirstName().toUpperCase().charAt(1));
                        }
                    }
                }
            }

        }else {
            Users user = listUsers
                    .stream()
                    .filter(i -> i.getUserId().equalsIgnoreCase(complaintUpdates.getUpdatedBy()))
                    .findFirst()
                    .orElse(null);
            if (user != null) {
                profilePic = user.getProfileUrl();
                initials.append(user.getFirstName().toUpperCase().charAt(0));
                fullName.append(user.getFirstName());
                if (user.getLastName() != null && !user.getLastName().trim().equalsIgnoreCase("")) {
                    initials.append(user.getLastName().toUpperCase().charAt(0));
                    fullName.append(" ");
                    fullName.append(user.getLastName());
                }
                else {
                    if (user.getFirstName() != null && user.getFirstName().length() > 1) {
                        initials.append(user.getFirstName().toUpperCase().charAt(1));
                    }
                }
            }
        }

        if (complaintUpdates.getStatus().equalsIgnoreCase(ComplaintStatus.OPENED.name())) {
            update = fullName.toString() + " is raised - " + complaintType ;
        }
        else if (complaintUpdates.getStatus().equalsIgnoreCase(ComplaintStatus.PENDING.name())) {
            update = complaintUpdates.getComments();
            description = complaintUpdates.getComments();
        }
        else if (complaintUpdates.getStatus().equalsIgnoreCase(ComplaintStatus.ASSIGNED.name())) {
            description = "Complaint is assigned to " + assignee;
            update = "Complaint is assigned";
        }
        else if (complaintUpdates.getStatus().equalsIgnoreCase(ComplaintStatus.RESOLVED.name())) {
            description = fullName + " is marked the complaint as Resolved";
            update = "Complaint is resolved";
        }

        updatedAt = Utils.dateToString(complaintUpdates.getCreatedAt());
        updatedTime = Utils.dateToTime(complaintUpdates.getCreatedAt());




        return new com.smartstay.smartstay.responses.complaint.ComplaintUpdates(update,
                description,
                fullName.toString(),
                initials.toString(),
                updatedAt,
                updatedTime,
                null);
    }
}
