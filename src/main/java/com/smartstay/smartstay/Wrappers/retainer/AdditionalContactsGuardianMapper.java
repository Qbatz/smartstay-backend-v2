package com.smartstay.smartstay.Wrappers.retainer;

import com.smartstay.smartstay.dao.CustomerAdditionalContacts;
import com.smartstay.smartstay.responses.retainer.Guardians;

import java.util.function.Function;

public class AdditionalContactsGuardianMapper implements Function<CustomerAdditionalContacts, Guardians> {
    @Override
    public Guardians apply(CustomerAdditionalContacts customerAdditionalContacts) {
        return new Guardians(customerAdditionalContacts.getContactId(),
                customerAdditionalContacts.getName(),
                "91",
                customerAdditionalContacts.getMobile(),
                customerAdditionalContacts.getRelationship());
    }
}
