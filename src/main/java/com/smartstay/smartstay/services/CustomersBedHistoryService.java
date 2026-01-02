package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Electricity.BedHistoryCustomerListMapper;
import com.smartstay.smartstay.Wrappers.customers.BedHistoryMapper;
import com.smartstay.smartstay.dao.BookingsV1;
import com.smartstay.smartstay.dao.CustomersBedHistory;
import com.smartstay.smartstay.dao.RentHistory;
import com.smartstay.smartstay.dto.electricity.BedHistoryByRoomId;
import com.smartstay.smartstay.dto.electricity.CustomerBedsList;
import com.smartstay.smartstay.ennum.CustomersBedType;
import com.smartstay.smartstay.repositories.CustomerBedHistoryRespository;
import com.smartstay.smartstay.responses.customer.BedHistory;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class CustomersBedHistoryService {

    @Autowired
    private CustomerBedHistoryRespository customerBedHistoryRepository;


    public CustomersBedHistory getCustomerBedByStartDate(String customerId, Date startDate, Date endDate) {
        CustomersBedHistory cbh = customerBedHistoryRepository.findByCustomerIdAndDate(customerId, startDate, endDate);
        return cbh;
    }

    public List<CustomersBedHistory> getCustomersBedHistory(String customerId, Date startDate, Date endDate) {
        return customerBedHistoryRepository.listBedsByCustomerIdAndDate(customerId, startDate, endDate);
    }

    public List<CustomerBedsList> getAllCustomerFromBedsHistory(String hostelId, Date billStartDate, Date billEndDate) {
        return customerBedHistoryRepository.findByHostelIdAndStartAndEndDate(hostelId, billStartDate, billEndDate)
                .stream()
                .filter(item -> Utils.compareWithTwoDates(item.getStartDate(), billEndDate) <= 0)
                .map(item -> new BedHistoryCustomerListMapper().apply(item))
                .toList();
    }

    public CustomersBedHistory getLatestCustomerBed(String customerId) {
        return customerBedHistoryRepository.findTopByCustomerIdOrderByCreatedAtDesc(customerId).orElse(null);
    }

    public List<BedHistory> getCustomersBedHistory(String customerId) {
        return customerBedHistoryRepository.findByCustomerId(customerId)
                .stream()
                .map(i -> new BedHistoryMapper().apply(i))
                .toList();
    }

    public CustomersBedHistory getCustomerBookedBed(String customerId) {
        return customerBedHistoryRepository.findByCustomerIdAndTypeBooking(customerId);
    }

    public void checkoutCustomer(String customerId) {
        CustomersBedHistory bedHistory = customerBedHistoryRepository.findTopByCustomerIdOrderByCreatedAtDesc(customerId).orElse(null);
        if (bedHistory != null) {
            bedHistory.setEndDate(new Date());
            customerBedHistoryRepository.save(bedHistory);
        }

    }

    public void updateBedEndDate(CustomersBedHistory currentBed) {
        if (currentBed != null) {
            customerBedHistoryRepository.save(currentBed);
        }
    }

    public void updateJoiningDate(String customerId, Date joinigDate) {
        CustomersBedHistory cbh = customerBedHistoryRepository.findByCustomerIdAndTypeRent(customerId);
        cbh.setStartDate(joinigDate);

        customerBedHistoryRepository.save(cbh);
    }


    public List<CustomersBedHistory> getCheckedInReassignedHistory(String customerId) {
        return customerBedHistoryRepository.findAllBedsAfterJoining(customerId);
    }

    public void updateRentAmount(Double newRentAmount, String customerId) {
        CustomersBedHistory cbh = customerBedHistoryRepository.findByCustomerIdAndTypeRent(customerId);
        cbh.setRentAmount(newRentAmount);

        customerBedHistoryRepository.save(cbh);
    }

    public List<CustomersBedHistory> getByCustomerIdAndStartAndEndDate(String customerId, Date startDate, Date endDate) {
        return customerBedHistoryRepository.findByCustomerIdAndStartAndEndDate(customerId, startDate, endDate);
    }

    public void updateNewRentAmount(List<RentHistory> rentHistoryList) {
        List<String> customerIds = rentHistoryList
                .stream()
                .map(RentHistory::getCustomerId)
                .toList();

        Calendar getEndDate = Calendar.getInstance();
        getEndDate.add(Calendar.DAY_OF_MONTH, -1);

        List<CustomersBedHistory> listCustomerBedHistory = customerBedHistoryRepository.findCurrentBed(customerIds);
        List<CustomersBedHistory> updateBedHistoryWithNewRent = listCustomerBedHistory
                .stream()
                .map(i -> {
                    double rentAmount = rentHistoryList.stream()
                            .filter(item -> item.getCustomerId().equalsIgnoreCase(i.getCustomerId()))
                            .mapToDouble(RentHistory::getRent)
                            .sum();
                   CustomersBedHistory cbh = new CustomersBedHistory();
                   cbh.setCustomerId(i.getCustomerId());
                   cbh.setBedId(i.getBedId());
                   cbh.setRoomId(i.getRoomId());
                   cbh.setFloorId(i.getFloorId());
                   cbh.setType(CustomersBedType.RENT_REVISION.name());
                   cbh.setHostelId(i.getHostelId());
                   cbh.setStartDate(new Date());
                   cbh.setEndDate(null);
                   cbh.setChangedBy(i.getChangedBy());
                   cbh.setReason("Rent Revision");
                   cbh.setActive(true);
                   cbh.setCreatedAt(new Date());
                   cbh.setRentAmount(rentAmount);
                   return cbh;
                })
                .toList();

        List<CustomersBedHistory> listUpdatedBedHistoryWithEndDate = listCustomerBedHistory
                .stream()
                        .map(i -> {
                            i.setEndDate(getEndDate.getTime());
                            return i;
                        })
                                .toList();

        customerBedHistoryRepository.saveAll(updateBedHistoryWithNewRent);
        customerBedHistoryRepository.saveAll(listUpdatedBedHistoryWithEndDate);
    }

    public void saveCheckInHistory(CustomersBedHistory cbh) {
        customerBedHistoryRepository.save(cbh);
    }

    public List<CustomersBedHistory> getCurrentBedHistoryByCustomerIds(List<String> customerIds) {
        return customerBedHistoryRepository.findLatestBedHistoryForCustomers(customerIds);
    }

    public HashMap<Integer, Integer> findByStartAndEndDateAndRoomIds(List<BedHistoryByRoomId> listBeds) {
        HashMap<Integer, Integer> occupantsCountsByRoomId = new HashMap<>();
        if (!listBeds.isEmpty()) {
            listBeds
                    .forEach(i -> {
                       List<CustomersBedHistory> customersBedHistoryList = customerBedHistoryRepository.findByRoomIdStartAndEndDate(i.roomId(), i.startDate(), i.endDate());
                       if (customersBedHistoryList != null) {
                           occupantsCountsByRoomId.put(i.roomId(), customersBedHistoryList.size());
                       }
                    });
        }

        return occupantsCountsByRoomId;
    }

    public List<CustomersBedHistory> getCustomersByBedIdAndDates(Integer roomId, Date startDate, Date endDate) {
        return customerBedHistoryRepository.findByRoomIdStartAndEndDate(roomId, startDate, endDate);
    }
}
