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
import java.util.Map;

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

    public Map<String, Object> getRequestStatusSummary(String hostelId, Date startDate, Date endDate) {
         return amenityRequestRepository.getRequestStatusSummary(hostelId, startDate, endDate);
    }

    public List<AmenityRequest> findTopRequests(String hostelId, org.springframework.data.domain.Pageable pageable) {
         return amenityRequestRepository.findTopRequests(hostelId, pageable);
    }

    public List<AmenityRequest> findTopRequestsByDate(String hostelId, Date startDate, Date endDate, org.springframework.data.domain.Pageable pageable) {
         return amenityRequestRepository.findTopRequestsByDate(hostelId, startDate, endDate, pageable);
    }


    public int countActiveByHostelIdAndDateRange(String hostelId, List<String> statuses, Date startDate, Date endDate) {
        return amenityRequestRepository.countActiveByHostelIdAndDateRange(hostelId, statuses, startDate, endDate);
    }
}
