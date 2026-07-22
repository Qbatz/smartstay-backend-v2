package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.CustomerJobDetails;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.payloads.customer.JobDetails;
import com.smartstay.smartstay.payloads.customer.UpdateCustomerJob;
import com.smartstay.smartstay.repositories.CustomerJobDetailsRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CustomerJobDetailsService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private CustomerJobDetailsRepository jobDetailsRepository;

    public void addJobDetails(String hostelId, String customerId, JobDetails jobDetails) {
        boolean canUpdate = false;
        CustomerJobDetails customerJobDetails = jobDetailsRepository.findByCustomerIdAndHostelId(customerId, hostelId);
        if (customerJobDetails == null) {
            customerJobDetails = new CustomerJobDetails();
        }
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

    public com.smartstay.smartstay.dto.customer.JobDetails getCustomerJobDetails(String hostelId, String customerId) {
        CustomerJobDetails customerJobDetails = jobDetailsRepository.findByCustomerIdAndHostelId(customerId, hostelId);
        if (customerJobDetails == null) {
            return null;
        }
        return new com.smartstay.smartstay.dto.customer.JobDetails(customerJobDetails.getEmploymentStatus(),
                customerJobDetails.getOrganizationName(),
                customerJobDetails.getRole(),
                customerJobDetails.getWorkLocation(),
                customerJobDetails.getShiftType(),
                customerJobDetails.getShiftStartTime() + ":" + customerJobDetails.getShiftEndTime());
    }

    public ResponseEntity<?> updateJobInformation(String hostelId, String customerId, UpdateCustomerJob updateCustomerJob) {
        CustomerJobDetails cjd =  jobDetailsRepository.findByCustomerIdAndHostelId(customerId, hostelId);
        if (cjd == null) {
            cjd = new CustomerJobDetails();
            cjd.setHostelId(hostelId);
            cjd.setCustomerId(customerId);
            cjd.setCreatedAt(new Date());
            cjd.setUpdatedAt(new Date());
            cjd.setUpdatedBy(authentication.getName());
            cjd.setCreatedBy(authentication.getName());
            cjd.setCreatedByUserType(UserType.ADMIN.name());
            cjd.setUpdatedByUserType(UserType.ADMIN.name());
        }
        if (updateCustomerJob.employmentStatus() != null && !updateCustomerJob.employmentStatus().trim().equalsIgnoreCase("")) {
            cjd.setEmploymentStatus(updateCustomerJob.employmentStatus());
        }
        else {
            if (cjd.getEmploymentStatus() != null) {
                cjd.setEmploymentStatus(null);
            }
        }

        if (updateCustomerJob.organizationName() != null && !updateCustomerJob.organizationName().trim().equalsIgnoreCase("")) {
            cjd.setOrganizationName(updateCustomerJob.organizationName());
        }
        else {
            if (cjd.getOrganizationName() != null) {
                cjd.setOrganizationName(null);
            }
        }

        if (updateCustomerJob.role() != null && !updateCustomerJob.role().trim().equalsIgnoreCase("")) {
            cjd.setRole(updateCustomerJob.role());
        }
        else {
            if (cjd.getRole() != null) {
                cjd.setRole(null);
            }
        }
        if (updateCustomerJob.workLocation() != null && !updateCustomerJob.workLocation().trim().equalsIgnoreCase("")) {
            cjd.setWorkLocation(updateCustomerJob.workLocation());
        }
        else {
            if (cjd.getWorkLocation() != null) {
                cjd.setWorkLocation(null);
            }
        }
        if (updateCustomerJob.shiftType() != null && !updateCustomerJob.shiftType().trim().equalsIgnoreCase("")) {
            cjd.setShiftType(updateCustomerJob.shiftType());
        }
        else {
            if (cjd.getShiftType() != null) {
                cjd.setShiftType(null);
            }
        }

        if (updateCustomerJob.shiftStartsFrom() != null && !updateCustomerJob.shiftStartsFrom().trim().equalsIgnoreCase("")) {
            cjd.setShiftStartTime(updateCustomerJob.shiftStartsFrom());
        }
        else {
            if (cjd.getShiftStartTime() != null) {
                cjd.setShiftStartTime(null);
            }
        }
        if (updateCustomerJob.shiftEndsAt() != null && !updateCustomerJob.shiftEndsAt().trim().equalsIgnoreCase("")) {
            cjd.setShiftEndTime(updateCustomerJob.shiftEndsAt());
        }
        else {
            if (cjd.getShiftEndTime() != null) {
                cjd.setShiftEndTime(null);
            }
        }
        cjd.setUpdatedAt(new Date());
        cjd.setUpdatedBy(authentication.getName());
        cjd.setUpdatedByUserType(UserType.ADMIN.name());

        jobDetailsRepository.save(cjd);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }
}
