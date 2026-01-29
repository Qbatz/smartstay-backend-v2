package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.amenity.AmenityRequestMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.AmenitiesV1;
import com.smartstay.smartstay.dao.AmenityRequest;
import com.smartstay.smartstay.dto.amenity.AmenityRequestDTO;
import com.smartstay.smartstay.repositories.AmenityRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AmenityRequestService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private AmenitiesService amenitiesService;
    @Autowired
    private AmenityRequestRepository amenityRequestRepository;

    /**
     * this will be accessed through customer details page
     *
     * @param customerId
     * @return
     */
    public List<AmenityRequestDTO> getRequestedAmenities(String customerId, String hostelId) {
        if (!authentication.isAuthenticated()) {
            return null;
        }

        List<AmenityRequest> listRequests = amenityRequestRepository.findByCustomerIdAndHostelId(customerId, hostelId);
        List<String> requestedAmenityIds = listRequests
                .stream()
                .map(AmenityRequest::getAmenityId)
                .toList();

        List<AmenitiesV1> listAmenities = amenitiesService.findByAmenityIds(requestedAmenityIds);

        List<AmenityRequestDTO> amenityRequests = null;
        if (listRequests != null) {
            amenityRequests = listRequests
                    .stream()
                    .map(i -> new AmenityRequestMapper(listAmenities).apply(i))
                    .toList();
        }


        return amenityRequests;

    }

    public int countByHostelIdAndDateRange(String hostelId, Date startDate, Date endDate) {
        return amenityRequestRepository.countByHostelIdAndDateRange(hostelId, startDate, endDate);
    }

    public int countActiveByHostelIdAndDateRange(String hostelId, List<String> statuses, Date startDate, Date endDate) {
        return amenityRequestRepository.countActiveByHostelIdAndDateRange(hostelId, statuses, startDate, endDate);
    }
}
