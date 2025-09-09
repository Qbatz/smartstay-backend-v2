package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BillTemplateType;
import com.smartstay.smartstay.dao.BillTemplates;
import com.smartstay.smartstay.ennum.BillConfigTypes;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.repositories.BillTemplatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TemplatesService {

    @Autowired
    private Authentication authentication;

    @Autowired
    private BillTemplatesRepository templateRepository;


    public int initialTemplateSetup(com.smartstay.smartstay.payloads.templates.BillTemplates tmpl) {

        if (!authentication.isAuthenticated()) {
            return 0;
        }
        String hostelName = tmpl.hostelName();
        StringBuilder findPrefix = new StringBuilder();
        StringBuilder findPrefixRent = new StringBuilder();
        findPrefix.append("#INV-AD");
        findPrefixRent.append("#INV");
        if (hostelName.length() > 3) {
            findPrefixRent.append(hostelName.replaceAll(" ", "").substring(0, 4).toUpperCase());
            findPrefix.append(hostelName.replaceAll(" ", "").substring(0, 4).toUpperCase());
        }

        BillTemplates templates = new BillTemplates();
        templates.setMobile(tmpl.mobile());
        templates.setEmailId(tmpl.emailId());
        templates.setHostelId(tmpl.hostelId());
        templates.setTemplateUpdated(false);
        templates.setCreatedAt(new Date());
        templates.setCreatedBy(authentication.getName());



        List<BillTemplateType> listTemplateType = new ArrayList<>();

        BillTemplateType templateType = new BillTemplateType();
        templateType.setInvoiceType(BillConfigTypes.ADVANCE.name());
        templateType.setInvoicePrefix(findPrefixRent.toString());
        templateType.setInvoiceSuffix("001");
        templateType.setGstPercentage(0.0);
        templateType.setCgst(0.0);
        templateType.setSgst(0.0);
        templateType.setInvoiceNotes("Welcome to " + tmpl.hostelName() + ". Wishing you happy stay");
        templateType.setReceiptNotes("Thanks for choosing " + tmpl.hostelName());
        templateType.setInvoiceTemplateColor("rgb(30, 69, 225)");
        templateType.setReceiptTemplateColor("rgb(30, 69, 225)");
        templateType.setTemplates(templates);

        listTemplateType.add(templateType);

        BillTemplateType templateType1 = new BillTemplateType();
        templateType1.setInvoiceType(BillConfigTypes.RENTAL.name());
        templateType1.setInvoicePrefix(findPrefix.toString());
        templateType1.setInvoiceSuffix("001");
        templateType1.setGstPercentage(0.0);
        templateType1.setCgst(0.0);
        templateType1.setSgst(0.0);
        templateType1.setInvoiceNotes("Welcome to " + tmpl.hostelName() + ". Wishing you happy stay");
        templateType1.setReceiptNotes("Thanks for choosing " + tmpl.hostelName());
        templateType1.setInvoiceTemplateColor("rgb(30, 69, 225)");
        templateType1.setReceiptTemplateColor("rgb(30, 69, 225)");
        templateType1.setTemplates(templates);

        listTemplateType.add(templateType1);

        templates.setTemplateTypes(listTemplateType);


        templateRepository.save(templates);




        return 1;
    }
}
