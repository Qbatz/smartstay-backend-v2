package com.smartstay.smartstay.Wrappers.retainer;

import com.smartstay.smartstay.dao.CustomerAdditionalContacts;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.responses.customer.AdditionalContacts;
import com.smartstay.smartstay.responses.retainer.CustomersList;
import com.smartstay.smartstay.responses.retainer.Guardians;
import com.smartstay.smartstay.util.CustomerUtils;
import com.smartstay.smartstay.util.NameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CustomersListMapper implements Function<Customers, CustomersList>  {

    private List<CustomerAdditionalContacts> additionalContacts = null;

    public CustomersListMapper(List<CustomerAdditionalContacts> additionalContacts) {
        this.additionalContacts = additionalContacts;
    }

    @Override
    public CustomersList apply(Customers customers) {
        List<Guardians> guardiansList = new ArrayList<>();
        if (additionalContacts != null) {
            List<CustomerAdditionalContacts> listAdditionalContacts = additionalContacts
                    .stream()
                    .filter(i -> i.getCustomerId().equalsIgnoreCase(customers.getCustomerId()))
                    .toList();
            if (listAdditionalContacts != null) {
                guardiansList = listAdditionalContacts
                        .stream()
                        .map(i -> new AdditionalContactsGuardianMapper().apply(i))
                        .toList();
            }
        }
        return new CustomersList(customers.getCustomerId(),
                NameUtils.getFullName(customers.getFirstName(), customers.getLastName()),
                customers.getFirstName(),
                customers.getLastName(),
                CustomerUtils.getProfilePic(customers),
                NameUtils.getInitials(customers.getFirstName(), customers.getLastName()),
                customers.getMobile(),
                "91",
                0.0,
                0.0,
                0.0,
                guardiansList);
    }
}
