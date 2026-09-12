package com.smartstay.smartstay.payloads.customer;

import com.smartstay.smartstay.dto.customer.CustomerJob;

import java.util.List;

public record CustomerAdditionalData(JobDetails jobDetails,
                                     List<Guardian> guardians,
                                     List<CustomerJob> customerJobs) {
}
