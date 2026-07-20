package com.smartstay.smartstay.payloads.customer;

import java.util.List;

public record CustomerAdditionalData(JobDetails jobDetails,
                                     List<Guardian> guardians) {
}
