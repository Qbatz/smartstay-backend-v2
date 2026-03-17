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

    public List<RoomElectricityCustomersList> getCustomerEbListForRoom(Integer roomId) {
        return customerEbRepository.getAllCustomersByRoomId(roomId)
                .stream()
                .map(item -> new RoomElectricityMapper().apply(item))
                .toList();
    }

    public List<ElectricityCustomersList> getCustomerListFromRooms(List<Integer> roomIds) {
        return customerEbRepository.fetchAllCustomersList(roomIds);
    }

    public List<CustomersEbHistory> getAllByReadingId(List<Integer> readingIds) {
        return customerEbRepository.findByReadingIdIn(readingIds);
    }

    public List<ElectricityHistoryBySingleCustomer> getAllReadingByCustomerId(String customerId, Date startDate, Date endDate) {
        return customerEbRepository.getSingleCustomerEbHistory(customerId, startDate, endDate);
    }

    public void saveCustomerEb(List<CustomersEbHistory> customerEbHistory) {
        customerEbRepository.saveAll(customerEbHistory);
    }

    public List<CustomersEbHistory> getAllByCustomerIdAndReadingId(String customerId, List<Integer> ebReadingsId) {
        return customerEbRepository.findByCustomerIdAndReadingsId(customerId, ebReadingsId);
    }

    public boolean deleteEntriesByIds(List<Integer> readingIds) {
        List<CustomersEbHistory> listHistories = customerEbRepository.findByReadingIdIn(readingIds);
        customerEbRepository.deleteAll(listHistories);
        return true;
    }

    public List<CustomersEbHistory> findHistoryByCustomerIdAndReadingId(String customerId, List<Integer> listReadingIds) {
        return customerEbRepository.findByCustomerIdAndReadingsId(customerId, listReadingIds);
    }
}
