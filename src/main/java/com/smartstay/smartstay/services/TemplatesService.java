package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Bills.BillingRulesMapper;
import com.smartstay.smartstay.Wrappers.Bills.TemplateMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.BillConfigTypes;
import com.smartstay.smartstay.ennum.InvoiceType;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.payloads.billTemplate.UpdateBillTemplate;
import com.smartstay.smartstay.payloads.billTemplate.UpdateBillingRule;
import com.smartstay.smartstay.repositories.BillTemplatesRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.smartstay.smartstay.util.Utils.isNotBlank;

@Service
public class TemplatesService {

    @Autowired
    private Authentication authentication;

    @Autowired
    private UsersService userService;

    @Autowired
    private HostelConfigService hostelConfigService;

    @Autowired
    private BankingService bankingService;

    @Autowired
    private UploadFileToS3 uploadToS3;

    @Autowired
    UserHostelService userHostelService;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private BillTemplatesRepository templateRepository;


    public int initialTemplateSetup(com.smartstay.smartstay.payloads.templates.BillTemplates tmpl) {

        if (!authentication.isAuthenticated()) {
            return 0;
        }
        String hostelName = tmpl.hostelName();
        StringBuilder findPrefix = new StringBuilder();
        StringBuilder findPrefixRent = new StringBuilder();
        findPrefix.append("ADV");
        findPrefixRent.append("INV");
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

    public ResponseEntity<?> updateTemplate(String hostelId,
                                            String mobile,
                                            String email,
                                            Boolean isMobileCustomized,
                                            Boolean isEmailCustomized,
                                            Boolean isLogoCustomized,
                                            Boolean isSignatureCustomized,
                                            MultipartFile hostelLogo,
                                            MultipartFile billSignature,
                                            MultipartFile invoiceLogo,
                                            MultipartFile invSignature,
                                            MultipartFile qrCode,
                                            MultipartFile receiptLogo,
                                            MultipartFile receiptSignature,
                                            UpdateBillTemplate payloads) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.BILLS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(loginId, hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        BillTemplates templates = templateRepository.getByHostelId(hostelId);
        if (templates == null) {
            return new ResponseEntity<>(Utils.TEMPLATE_NOT_AVAILABLE, HttpStatus.NO_CONTENT);
        }

        if (payloads != null && isNotBlank(payloads.bankId())) {
            boolean bankingExist = bankingService.findBankingRecordByHostelIdAndBankId(payloads.bankId(), hostelId);
            if (!bankingExist) {
                return new ResponseEntity<>(Utils.INVALID_BANKING_DETAILS, HttpStatus.BAD_REQUEST);
            }
        }

        updateBasicFields(templates, mobile, email, isMobileCustomized, isEmailCustomized, isLogoCustomized, isSignatureCustomized);
        updateLogosAndSignatures(templates, hostelLogo, billSignature);
        if (payloads != null) {
            Optional<BillTemplateType> templateTypeOpt = templates.getTemplateTypes().stream()
                    .filter(item -> Objects.equals(item.getTemplateTypeId(), payloads.templateTypeId()))
                    .findFirst();

            if (templateTypeOpt.isEmpty()) {
                return new ResponseEntity<>(Utils.TEMPLATE_TYPE_NOT_FOUND,
                        HttpStatus.BAD_REQUEST);
            }

            BillTemplateType templateType = templateTypeOpt.get();
            updateTemplateTypeFields(templateType,
                    payloads,
                    invoiceLogo,
                    invSignature,
                    receiptLogo,
                    receiptSignature,
                    qrCode,
                    templates.isLogoCustomized(),
                    templates.isSignatureCustomized(),
                    templates.isMobileCustomized(),
                    templates.isEmailCustomized()
            );
        }

        templates.setUpdatedAt(new Date());
        templates.setUpdatedBy(loginId);
        templateRepository.save(templates);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public ResponseEntity<?> updateBillingRule(String hostelId, Integer billingRuleId,
                                               UpdateBillingRule payloads) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.BILLS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(loginId, hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        Optional<BillingRules> optionalBillingRule =
                hostelConfigService.getBillingRuleByIdAndHostelId(billingRuleId, hostelId);

        if (optionalBillingRule.isEmpty()) {
            return new ResponseEntity<>(Utils.BILLING_RULE_NOT_AVAILABLE, HttpStatus.NO_CONTENT);
        }

        BillingRules billingRule = optionalBillingRule.get();
        if (payloads.billingStartDate() != null) {
            billingRule.setBillingStartDate(payloads.billingStartDate());
        }
        if (payloads.billingDueDate() != null) {
            billingRule.setBillingDueDate(payloads.billingDueDate());
        }
        if (payloads.noticePeriod() != null) {
            billingRule.setNoticePeriod(payloads.noticePeriod());
        }

        hostelConfigService.saveBillingRule(billingRule);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }


    public ResponseEntity<?> getBillingRule(String hostelId) {

        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.BILLS.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(loginId, hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        Optional<BillingRules> optionalBillingRule =
                hostelConfigService.getBillingRuleByHostelId(hostelId);

        if (optionalBillingRule.isEmpty()) {
            return new ResponseEntity<>(Utils.BILLING_RULE_NOT_AVAILABLE, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(BillingRulesMapper.toDto(optionalBillingRule.get()), HttpStatus.OK);
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


    private void updateBasicFields(BillTemplates templates,
                                   String mobile, String email,
                                   Boolean isMobileCustomized, Boolean isEmailCustomized,
                                   Boolean isLogoCustomized, Boolean isSignatureCustomized) {
        if (isNotBlank(mobile)) templates.setMobile(mobile);
        if (isNotBlank(email)) templates.setEmailId(email);
        if (isMobileCustomized != null) templates.setMobileCustomized(isMobileCustomized);
        if (isEmailCustomized != null) templates.setEmailCustomized(isEmailCustomized);
        if (isLogoCustomized != null) templates.setLogoCustomized(isLogoCustomized);
        if (isSignatureCustomized != null) templates.setSignatureCustomized(isSignatureCustomized);
    }

    private void updateLogosAndSignatures(BillTemplates templates,
                                          MultipartFile hostelLogo, MultipartFile billSignature) {
        if (hostelLogo != null) {
            templates.setHostelLogo(uploadIfPresent(hostelLogo));
        }
        if (billSignature != null) {
            templates.setDigitalSignature(uploadIfPresent(billSignature));
        }
    }

    private void updateTemplateTypeFields(BillTemplateType templateType,
                                          UpdateBillTemplate payloads,
                                          MultipartFile invoiceLogo, MultipartFile invSignature,
                                          MultipartFile receiptLogo, MultipartFile receiptSignature,
                                          MultipartFile qrCode,
                                          Boolean isLogoCustomized, Boolean isSignatureCustomized,
                                          Boolean isMobileCustomized, Boolean isEmailCustomized) {

        if (isNotBlank(payloads.gstPercentile())) {
            try {
                Double gst = Double.parseDouble(payloads.gstPercentile());
                templateType.setGstPercentage(Math.max(gst, 0.0));
                templateType.setCgst(Math.max(gst, 0.0) / 2);
                templateType.setSgst(Math.max(gst, 0.0) / 2);
            } catch (NumberFormatException e) {

            }
        }

        if (isNotBlank(payloads.bankId())) templateType.setBankAccountId(payloads.bankId());
        if (isNotBlank(payloads.invoiceTermsAndCondition())) templateType.setInvoiceTermsAndCondition(payloads.invoiceTermsAndCondition());

        if (Boolean.TRUE.equals(isLogoCustomized)) {

            if (invoiceLogo != null){
                String invoiceImage = uploadIfPresent(invoiceLogo);
                templateType.setInvoiceLogoUrl(invoiceImage);
            }
            if (receiptLogo != null){
                String receiptImage = uploadIfPresent(receiptLogo);
                templateType.setReceiptLogoUrl(receiptImage);
            }
        }else {
            templateType.setInvoiceLogoUrl(null);
            templateType.setReceiptLogoUrl(null);
        }

        if (Boolean.TRUE.equals(isSignatureCustomized)) {

            if (invSignature != null) {
                String invSignatureImage = uploadIfPresent(invSignature);
                templateType.setInvoiceSignatureUrl(invSignatureImage);
            }
            if (receiptSignature != null) {
                String receiptSignatureImage = uploadIfPresent(receiptSignature);
                templateType.setReceiptSignatureUrl(receiptSignatureImage);
            }
        }else {
            templateType.setInvoiceSignatureUrl(null);
            templateType.setReceiptSignatureUrl(null);
        }

        if (Boolean.TRUE.equals(isMobileCustomized)) {

            if (isNotBlank(payloads.invoicePhoneNumber())){
                templateType.setInvoicePhoneNumber(payloads.invoicePhoneNumber());
            }

            if (isNotBlank(payloads.receiptPhoneNumber())){
                templateType.setReceiptPhoneNumber(payloads.receiptPhoneNumber());
            }

        }else {
            templateType.setInvoicePhoneNumber(null);
            templateType.setReceiptPhoneNumber(null);
        }

        if (Boolean.TRUE.equals(isEmailCustomized)) {

            if (isNotBlank(payloads.invoiceMailId())){
                templateType.setInvoiceMailId(payloads.invoiceMailId());
            }

            if (isNotBlank(payloads.receiptMailId())){
                templateType.setReceiptMailId(payloads.receiptMailId());
            }

        }else {
            templateType.setInvoiceMailId(null);
            templateType.setReceiptMailId(null);
        }

        if (isNotBlank(payloads.invoiceTemplateColor())) templateType.setInvoiceTemplateColor(payloads.invoiceTemplateColor());
        if (isNotBlank(payloads.receiptTemplateColor())) templateType.setReceiptTemplateColor(payloads.receiptTemplateColor());
        if (qrCode != null) templateType.setQrCode(uploadIfPresent(qrCode));
        if (isNotBlank(payloads.invoiceNotes())) templateType.setInvoiceNotes(payloads.invoiceNotes());
        if (isNotBlank(payloads.receiptNotes())) templateType.setReceiptNotes(payloads.receiptNotes());
        if (isNotBlank(payloads.prefix())) templateType.setInvoicePrefix(payloads.prefix());
        if (isNotBlank(payloads.suffix())) templateType.setInvoiceSuffix(payloads.suffix());

    }

    private String uploadIfPresent(MultipartFile file) {
        return uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(file), "users/Bills");
    }
}
