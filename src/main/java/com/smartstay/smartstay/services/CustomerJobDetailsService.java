package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.CustomerJobDetails;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.payloads.customer.JobDetails;
import com.smartstay.smartstay.repositories.CustomerJobDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}
