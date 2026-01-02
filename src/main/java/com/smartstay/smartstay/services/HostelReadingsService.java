package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ElectricityConfig;
import com.smartstay.smartstay.dao.HostelReadings;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.ElectricityBillStatus;
import com.smartstay.smartstay.payloads.electricity.AddReading;
import com.smartstay.smartstay.payloads.electricity.UpdateElectricity;
import com.smartstay.smartstay.repositories.HostelEBReadingsRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class HostelReadingsService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private HostelEBReadingsRepository hostelEBReadingsRepository;
    @Autowired
    private HostelService hostelService;

    public ResponseEntity<?> addEbReadings(String hostelId, AddReading readings, Date billStartDate, Date billEndDate, ElectricityConfig electricityConfig) {
        HostelReadings lastReadings = hostelEBReadingsRepository.lastReading(hostelId);
        double consumption = readings.reading();
        boolean isFirstReading = true;
        if (lastReadings != null) {
            consumption = readings.reading() - lastReadings.getCurrentReading() ;
            Date sDate = Utils.addDaysToDate(lastReadings.getEntryDate(), 1);
            billStartDate = sDate;
        }
        Date readingDate = null;
        if (readings.readingDate() != null) {
            readingDate = Utils.stringToDate(readings.readingDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        } else {
            readingDate = new Date();
        }

        if (lastReadings != null) {
            isFirstReading = false;
            if (Utils.compareWithTwoDates(readingDate, lastReadings.getEntryDate()) <= 0) {
                return new ResponseEntity<>(Utils.ALREADY_READING_TAKEN_THIS_DATE, HttpStatus.BAD_REQUEST);
            }
            if (readings.reading() != null) {
                if (readings.reading() <= lastReadings.getCurrentReading()) {
                    return new ResponseEntity<>(Utils.PREVIOUS_CURRENT_READING_NOT_MATCHING, HttpStatus.BAD_REQUEST);
                }
            }
        }

        HostelReadings hr = new HostelReadings();
        hr.setHostelId(hostelId);
        hr.setCurrentReading(readings.reading());
        hr.setPreviousReading(0.0);
        hr.setCurrentUnitPrice(electricityConfig.getCharge());
        hr.setBillStatus(ElectricityBillStatus.INVOICE_NOT_GENERATED.name());
        hr.setBillStartDate(billStartDate);
        hr.setBillEndDate(billEndDate);
        hr.setEntryDate(readingDate);
        hr.setConsumption(consumption);
        hr.setFirstEntry(isFirstReading);
        hr.setMissedEntry(false);
        hr.setCreatedAt(new Date());
        hr.setCreatedBy(authentication.getName());

        hostelEBReadingsRepository.save(hr);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);

    }

    public List<com.smartstay.smartstay.dto.electricity.HostelReadings> getLastReading(String hostelId) {
        List<com.smartstay.smartstay.dto.electricity.HostelReadings> listHr = new ArrayList<>();
        HostelReadings hr = hostelEBReadingsRepository.lastReading(hostelId);
        if (hr != null) {
            com.smartstay.smartstay.dto.electricity.HostelReadings hr1 = new com.smartstay.smartstay.dto.electricity.HostelReadings(
                    hr.getId(),
                    Utils.dateToString(hr.getBillStartDate()),
                    Utils.dateToString(hr.getBillEndDate()),
                    Utils.dateToString(hr.getEntryDate()),
                    hr.getCurrentReading(),
                    hr.getConsumption()
            );

            listHr.add(hr1);
        }



        return listHr;
    }

    public ResponseEntity<?> updateEbReading(String hostelId, String readingId, UpdateElectricity updateElectricity) {
        Long id = Long.parseLong(readingId);
        HostelReadings currentReading = hostelEBReadingsRepository.findById(id).orElse(null);
        if (currentReading == null) {
            return new ResponseEntity<>(Utils.EB_ENTRY_NOT_FOUND, HttpStatus.BAD_REQUEST);
        }
        if (currentReading.getBillStatus().equalsIgnoreCase(ElectricityBillStatus.INVOICE_GENERATED.name())) {
            return new ResponseEntity<>(Utils.EB_ENTRY_CANNOT_CHANGE_INVOICE_GENERATED, HttpStatus.BAD_REQUEST);
        }
        HostelReadings previousReading = hostelEBReadingsRepository.previousEntry(hostelId, id);

        if (previousReading != null) {
            if (updateElectricity.reading() != null) {
                if (updateElectricity.reading() <= previousReading.getCurrentReading()) {
                    return new ResponseEntity<>(Utils.PREVIOUS_CURRENT_READING_NOT_MATCHING, HttpStatus.BAD_REQUEST);
                }
                currentReading.setCurrentReading(updateElectricity.reading());
                double consumption = updateElectricity.reading() - previousReading.getCurrentReading();
                currentReading.setConsumption(consumption);
                currentReading.setUpdatedAt(new Date());
                currentReading.setUpdatedBy(authentication.getName());
            }
            if (updateElectricity.entryDate() != null) {
                Date entryDate = Utils.stringToDate(updateElectricity.entryDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
                if (Utils.compareWithTwoDates(entryDate, previousReading.getEntryDate()) <= 0) {
                    return new ResponseEntity<>(Utils.ALREADY_READING_TAKEN_THIS_DATE, HttpStatus.BAD_REQUEST);
                }
                currentReading.setEntryDate(entryDate);
                currentReading.setBillEndDate(entryDate);
            }

            hostelEBReadingsRepository.save(currentReading);
        }
        else {
            if (updateElectricity.reading() != null) {
                currentReading.setCurrentReading(updateElectricity.reading());
                currentReading.setConsumption(updateElectricity.reading());
            }
            if (updateElectricity.entryDate() != null) {
                Date entryDate = Utils.stringToDate(updateElectricity.entryDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
                BillingDates billingDates = hostelService.getBillingRuleOnDate(hostelId, entryDate);
                if (billingDates != null) {
                    currentReading.setBillStartDate(billingDates.currentBillStartDate());
                }

                currentReading.setEntryDate(entryDate);
                currentReading.setBillEndDate(entryDate);
            }

            currentReading.setUpdatedBy(authentication.getName());
            currentReading.setUpdatedAt(new Date());

            hostelEBReadingsRepository.save(currentReading);
        }

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteLatestEntry(String hostelId, String readingId) {
        Long id = Long.parseLong(readingId);

        HostelReadings hr = hostelEBReadingsRepository.findById(id).orElse(null);
        if (hr == null) {
            return new ResponseEntity<>(Utils.INVALID_READING_ID, HttpStatus.BAD_REQUEST);
        }
        HostelReadings latesteReadings = hostelEBReadingsRepository.lastReading(hostelId);
        if (!hr.getId().equals(latesteReadings.getId())) {
            return new ResponseEntity<>(Utils.DELETE_AVAILABLE_ONLY_FOR_LAST_ENTRY, HttpStatus.BAD_REQUEST);
        }
        if (hr.getBillStatus().equalsIgnoreCase(ElectricityBillStatus.INVOICE_GENERATED.name())) {
            return new ResponseEntity<>(Utils.EB_ENTRY_CANNOT_DELETE_INVOICE_GENERATED, HttpStatus.BAD_REQUEST);
        }

        hostelEBReadingsRepository.delete(hr);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
