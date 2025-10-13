package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Electricity.RoomElectricityMapper;
import com.smartstay.smartstay.dao.CustomersEbHistory;
import com.smartstay.smartstay.dto.electricity.ElectricityCustomersList;
import com.smartstay.smartstay.dto.electricity.ElectricityHistoryBySingleCustomer;
import com.smartstay.smartstay.repositories.CustomerEBHistoryRepository;
import com.smartstay.smartstay.responses.electricity.RoomElectricityCustomersList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CustomerEbHistoryService {

    @Autowired
    private CustomerEBHistoryRepository customerEbRepository;

    private List<CustomersEbHistory> listCustomerEbHistory() {
        return null;
    }

    public void addEbForCustomer(List<CustomersEbHistory> listCustomers) {
        customerEbRepository.saveAll(listCustomers);
    }

    public List<RoomElectricityCustomersList> getCustomerEbListForRoom(Integer roomId, Date startDate, Date endDate) {
        return customerEbRepository.getAllCustomersByRoomId(roomId, startDate, endDate)
                .stream()
                .map(item -> new RoomElectricityMapper().apply(item))
                .toList();
    }

    public List<ElectricityCustomersList> getCustomerListFromRooms(List<Integer> roomIds) {
        return customerEbRepository.fetchAllCustomersList(roomIds);
    }

    public List<ElectricityHistoryBySingleCustomer> getAllReadingByCustomerId(String customerId, Date startDate, Date endDate) {
        return customerEbRepository.getSingleCustomerEbHistory(customerId, startDate, endDate);
    }
}
