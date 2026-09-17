package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.CustomerJobDetails;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.payloads.customer.JobDetails;
import com.smartstay.smartstay.payloads.customer.UpdateCustomerJob;
import com.smartstay.smartstay.repositories.CustomerJobDetailsRepository;
import com.smartstay.smartstay.dto.customer.CustomerJob;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CustomerJobDetailsService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private CustomerJobDetailsRepository jobDetailsRepository;
    @Autowired
    private UsersService usersService;

    public void addJobDetails(String hostelId, String customerId, JobDetails jobDetails) {
        boolean canUpdate = false;
        CustomerJobDetails customerJobDetails = new CustomerJobDetails();;

        if (jobDetails.employmentStatus() != null) {
            canUpdate = true;
            customerJobDetails.setEmploymentStatus(jobDetails.employmentStatus());
        }
        if (jobDetails.companyName() != null) {
            canUpdate = true;
            customerJobDetails.setOrganizationName(jobDetails.companyName());
        }
        if (jobDetails.collegeName() != null) {
            canUpdate = true;
            customerJobDetails.setOrganizationName(jobDetails.collegeName());
        }
        if (jobDetails.jobRole() != null) {
            canUpdate = true;
            customerJobDetails.setRole(jobDetails.jobRole());
        }
        if (jobDetails.workLocation() != null) {
            canUpdate = true;
            customerJobDetails.setWorkLocation(jobDetails.workLocation());
        }
        if (jobDetails.shiftType() != null) {
            canUpdate = true;
            customerJobDetails.setShiftType(jobDetails.shiftType());
        }
        if (jobDetails.shiftFrom() != null) {
            canUpdate = true;
            customerJobDetails.setShiftStartTime(jobDetails.shiftFrom());
        }
        if (jobDetails.shiftTo() != null) {
            canUpdate = true;
            customerJobDetails.setShiftEndTime(jobDetails.shiftTo());
        }

        if (canUpdate) {
            customerJobDetails.setIsDeleted(false);
            customerJobDetails.setHostelId(hostelId);
            customerJobDetails.setCustomerId(customerId);
            customerJobDetails.setCreatedByUserType(UserType.ADMIN.name());
            customerJobDetails.setCreatedBy(authentication.getName());
            customerJobDetails.setCreatedAt(new Date());

            jobDetailsRepository.save(customerJobDetails);
        }
    }

    public List<com.smartstay.smartstay.dto.customer.JobDetails> getCustomerJobDetails(String hostelId, String customerId) {
        List<CustomerJobDetails> customerJobDetails = jobDetailsRepository.findByCustomerIdAndHostelId(customerId, hostelId);
        if (customerJobDetails == null) {
            return new ArrayList<>();
        }
        List<com.smartstay.smartstay.dto.customer.JobDetails> listJobs = customerJobDetails
                .stream()
                .map(i -> {
                    return new com.smartstay.smartstay.dto.customer.JobDetails(i.getEmploymentStatus(),
                            i.getOrganizationName(),
                            i.getRole(),
                            i.getWorkLocation(),
                            i.getShiftType(),
                            i.getShiftStartTime(),
                            i.getShiftEndTime());
                })
                .toList();

        return listJobs;

    }

    public ResponseEntity<?> updateJobInformation(String hostelId, String customerId, UpdateCustomerJob updateCustomerJob, Users users) {
//        CustomerJobDetails cjd =  jobDetailsRepository.findByCustomerIdAndHostelId(customerId, hostelId);
//        if (cjd == null) {
//            cjd = new CustomerJobDetails();
//            cjd.setHostelId(hostelId);
//            cjd.setCustomerId(customerId);
//            cjd.setCreatedAt(new Date());
//            cjd.setUpdatedAt(new Date());
//            cjd.setUpdatedBy(authentication.getName());
//            cjd.setCreatedBy(authentication.getName());
//            cjd.setCreatedByUserType(UserType.ADMIN.name());
//            cjd.setUpdatedByUserType(UserType.ADMIN.name());
//        }
//        if (updateCustomerJob.employmentStatus() != null && !updateCustomerJob.employmentStatus().trim().equalsIgnoreCase("")) {
//            cjd.setEmploymentStatus(updateCustomerJob.employmentStatus());
//        }
//        else {
//            if (cjd.getEmploymentStatus() != null) {
//                cjd.setEmploymentStatus(null);
//            }
//        }
//
//        if (updateCustomerJob.organizationName() != null && !updateCustomerJob.organizationName().trim().equalsIgnoreCase("")) {
//            cjd.setOrganizationName(updateCustomerJob.organizationName());
//        }
//        else {
//            if (cjd.getOrganizationName() != null) {
//                cjd.setOrganizationName(null);
//            }
//        }
//
//        if (updateCustomerJob.role() != null && !updateCustomerJob.role().trim().equalsIgnoreCase("")) {
//            cjd.setRole(updateCustomerJob.role());
//        }
//        else {
//            if (cjd.getRole() != null) {
//                cjd.setRole(null);
//            }
//        }
//        if (updateCustomerJob.workLocation() != null && !updateCustomerJob.workLocation().trim().equalsIgnoreCase("")) {
//            cjd.setWorkLocation(updateCustomerJob.workLocation());
//        }
//        else {
//            if (cjd.getWorkLocation() != null) {
//                cjd.setWorkLocation(null);
//            }
//        }
//        if (updateCustomerJob.shiftType() != null && !updateCustomerJob.shiftType().trim().equalsIgnoreCase("")) {
//            cjd.setShiftType(updateCustomerJob.shiftType());
//        }
//        else {
//            if (cjd.getShiftType() != null) {
//                cjd.setShiftType(null);
//            }
//        }
//
//        if (updateCustomerJob.shiftStartsFrom() != null && !updateCustomerJob.shiftStartsFrom().trim().equalsIgnoreCase("")) {
//            cjd.setShiftStartTime(updateCustomerJob.shiftStartsFrom());
//        }
//        else {
//            if (cjd.getShiftStartTime() != null) {
//                cjd.setShiftStartTime(null);
//            }
//        }
//        if (updateCustomerJob.shiftEndsAt() != null && !updateCustomerJob.shiftEndsAt().trim().equalsIgnoreCase("")) {
//            cjd.setShiftEndTime(updateCustomerJob.shiftEndsAt());
//        }
//        else {
//            if (cjd.getShiftEndTime() != null) {
//                cjd.setShiftEndTime(null);
//            }
//        }
//        cjd.setUpdatedAt(new Date());
//        cjd.setUpdatedBy(authentication.getName());
//        cjd.setUpdatedByUserType(UserType.ADMIN.name());
//
//        jobDetailsRepository.save(cjd);
//        usersService.addUserLog(hostelId, customerId, ActivitySource.CUSTOMERS, ActivitySourceType.ADD_JOB, users);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> replaceJobs(String hostelId, String customerId, List<CustomerJob> jobs, Users users) {
        if (!belongsToTenant(hostelId, customerId, jobs)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        saveJobs(hostelId, customerId, jobs);
        usersService.addUserLog(hostelId, customerId, ActivitySource.CUSTOMERS, ActivitySourceType.ADD_JOB, users);
        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public boolean belongsToTenant(String hostelId, String customerId, List<CustomerJob> jobs) {
        return jobs.stream().allMatch(job -> job == null
                || (!differs(job.hostelId(), hostelId) && !differs(job.customerId(), customerId)));
    }

    @Transactional
    public void saveJobs(String hostelId, String customerId, List<CustomerJob> jobs) {
        Date now = new Date();
        String userId = authentication.getName();

        List<CustomerJobDetails> current = jobDetailsRepository.findActiveJobs(customerId, hostelId);
        current.forEach(row -> {
            row.setIsDeleted(true);
            row.setUpdatedAt(now);
            row.setUpdatedBy(userId);
            row.setUpdatedByUserType(UserType.ADMIN.name());
        });
        jobDetailsRepository.saveAll(current);

        List<CustomerJobDetails> rows = jobs.stream()
                .filter(job -> job != null && job.hasJobFields())
                .map(job -> {
                    CustomerJobDetails row = new CustomerJobDetails();
                    row.setHostelId(hostelId);
                    row.setCustomerId(customerId);
                    row.setEmploymentStatus(blankToNull(job.employmentStatus()));
                    row.setOrganizationName(blankToNull(job.organizationName()));
                    row.setRole(blankToNull(job.role()));
                    row.setWorkLocation(blankToNull(job.workLocation()));
                    row.setShiftType(blankToNull(job.shiftType()));
                    row.setShiftStartTime(blankToNull(job.shiftStartsFrom()));
                    row.setShiftEndTime(blankToNull(job.shiftEndsAt()));
                    row.setIsDeleted(false);
                    row.setCreatedAt(now);
                    row.setCreatedBy(userId);
                    row.setCreatedByUserType(UserType.ADMIN.name());
                    row.setUpdatedAt(now);
                    row.setUpdatedBy(userId);
                    row.setUpdatedByUserType(UserType.ADMIN.name());
                    return row;
                })
                .toList();
        jobDetailsRepository.saveAll(rows);
    }

    public List<CustomerJob> getCustomerJobs(String hostelId, String customerId) {
        return jobDetailsRepository.findActiveJobs(customerId, hostelId).stream()
                .map(row -> new CustomerJob(row.getHostelId(), row.getCustomerId(), row.getEmploymentStatus(),
                        row.getOrganizationName(), row.getRole(), row.getWorkLocation(), row.getShiftType(),
                        row.getShiftStartTime(), row.getShiftEndTime()))
                .toList();
    }

    private static boolean differs(String value, String expected) {
        return value != null && !value.isBlank() && !value.trim().equalsIgnoreCase(expected);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
