package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.payloads.customer.CustomerAdditionalContacts;
import com.smartstay.smartstay.repositories.CustomerAdditionalContactsRepositories;
import com.smartstay.smartstay.responses.customer.AdditionalContacts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AdditionalContactService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private CustomerAdditionalContactsRepositories customerAdditionalContactsRepositories;
    public ResponseEntity<?> addAdditionalContacts(String hostelId, String customerId, CustomerAdditionalContacts additionalContacts) {
        com.smartstay.smartstay.dao.CustomerAdditionalContacts ac = new com.smartstay.smartstay.dao.CustomerAdditionalContacts();
        ac.setName(additionalContacts.fullName());
        ac.setRelationship(additionalContacts.relationship());
        ac.setOccupation(additionalContacts.occupation());
        ac.setMobile(additionalContacts.mobile());
        ac.setCustomerId(customerId);
        ac.setHostelId(hostelId);
        ac.setCountryCode("91");
        ac.setDeleted(false);
        ac.setAddedByUserType(UserType.ADMIN.name());
        ac.setCreatedAt(new Date());
        ac.setCreatedBy(authentication.getName());

        customerAdditionalContactsRepositories.save(ac);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    public List<AdditionalContacts> getAdditionalContact(String hostelId, String customerId) {
        List<com.smartstay.smartstay.dao.CustomerAdditionalContacts> cac = customerAdditionalContactsRepositories.findByCustomerIdAndHostelId(hostelId, customerId);
        if (cac == null) {
            return null;
        }

        return cac
                .stream()
                .map(i -> new AdditionalContacts(i.getName(),
                        i.getMobile(),
                        i.getCountryCode(),
                        i.getRelationship(),
                        i.getOccupation(),
                        i.getContactId()))
                .toList();
    }
}
