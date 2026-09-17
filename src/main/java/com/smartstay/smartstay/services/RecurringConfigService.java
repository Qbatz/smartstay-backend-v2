package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.RecurringConfiguration;
import com.smartstay.smartstay.repositories.RecurringConfigRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecurringConfigService {
    @Autowired
    private RecurringConfigRepositories configRepositories;

    public RecurringConfiguration getRecurringConfig(String hostelId) {
        return configRepositories.findByHostelId(hostelId);
    }

    public List<RecurringConfiguration> getRecurringConfig(List<String> hostelIds) {
        return configRepositories.findByHostelIds(hostelIds);
    }
}
