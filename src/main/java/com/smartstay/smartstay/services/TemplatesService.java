package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Bills.TemplateMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.BillTemplateType;
import com.smartstay.smartstay.dao.BillTemplates;
import com.smartstay.smartstay.ennum.BillConfigTypes;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.repositories.BillTemplatesRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
        findPrefix.append("#ADV");
        findPrefixRent.append("#INV");
//        if (hostelName.length() > 3) {
//            String hostelNameShort = hostelName.replaceAll(" ", "").substring(0, 4).toUpperCase();
//            findPrefixRent.append(hostelNameShort);
//            findPrefix.append(hostelNameShort);
//        }
//        else {
//            findPrefixRent.append(hostelName.replaceAll(" ", "").toUpperCase());
//            findPrefix.append(hostelName.replaceAll(" ", "").toUpperCase());
//        }

        BillTemplates templates = new BillTemplates();
        templates.setMobile(tmpl.mobile());
        templates.setEmailId(tmpl.emailId());
        templates.setHostelId(tmpl.hostelId());
        templates.setTemplateUpdated(false);
        templates.setCreatedAt(new Date());
        templates.setCreatedBy(authentication.getName());
        templates.setSignatureCustomized(false);
        templates.setMobileCustomized(false);
        templates.setLogoCustomized(false);
        templates.setEmailCustomized(false);



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

    public int initialTemplateSetup(com.smartstay.smartstay.payloads.templates.BillTemplates tmpl, String createdBy) {

        String hostelName = tmpl.hostelName();
        StringBuilder findPrefix = new StringBuilder();
        StringBuilder findPrefixRent = new StringBuilder();
        findPrefix.append("#ADV");
        findPrefixRent.append("#INV");
        if (hostelName.length() > 3) {
            String hostelNameShort = hostelName.replaceAll(" ", "").substring(0, 4).toUpperCase();
            findPrefixRent.append(hostelNameShort);
            findPrefix.append(hostelNameShort);
        }
        else {
            findPrefixRent.append(hostelName.replaceAll(" ", "").toUpperCase());
            findPrefix.append(hostelName.replaceAll(" ", "").toUpperCase());
        }

        BillTemplates templates = new BillTemplates();
        templates.setMobile(tmpl.mobile());
        templates.setEmailId(tmpl.emailId());
        templates.setHostelId(tmpl.hostelId());
        templates.setTemplateUpdated(false);
        templates.setCreatedAt(new Date());
        templates.setCreatedBy(createdBy);
        templates.setSignatureCustomized(false);
        templates.setMobileCustomized(false);
        templates.setLogoCustomized(false);
        templates.setEmailCustomized(false);



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

    public ResponseEntity<?> getTemplates(String hostelId) {
        BillTemplates templates = templateRepository.getByHostelId(hostelId);
        if (templates == null) {
            return new ResponseEntity<>(Utils.TEMPLATE_NOT_AVAILABLE, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(new TemplateMapper().apply(templates), HttpStatus.OK);
    }

    public String[] getBillTemplate(String hostelId, String type) {
        String[] templates = new String[2];
        BillTemplates tmp = templateRepository.getByHostelId(hostelId);
        if (tmp == null) {
            return null;
        }
        List<BillTemplateType> templateType = tmp.getTemplateTypes()
                .stream()
                .filter(item -> item.getInvoiceType().equalsIgnoreCase(type))
                .toList();
        if (!templateType.isEmpty()) {
            templates[0] = templateType.get(0).getInvoicePrefix();
            templates[1] = templateType.get(0).getInvoiceSuffix();
        }

        return templates;
    }
}
