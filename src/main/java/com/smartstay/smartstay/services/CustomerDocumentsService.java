package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.CustomerDocuments;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.documents.CustomerFiles;
import com.smartstay.smartstay.dto.documents.FileDetails;
import com.smartstay.smartstay.dto.documents.UploadFiles;
import com.smartstay.smartstay.ennum.*;
import com.smartstay.smartstay.payloads.documents.UploadDocuments;
import com.smartstay.smartstay.repositories.CustomerDocumentsRepositories;
import com.smartstay.smartstay.util.Utils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerDocumentsService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private UploadFileToS3 uploadFileToS3;
    @Autowired
    private CustomerDocumentsRepositories customerDocumentsRepositories;

    public ResponseEntity<?> addFiles(String hostelId, String customerId, List<MultipartFile> listFiles, UploadDocuments uploadDocuments) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Customers customers = customersService.getCustomerInformation(customerId);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!customers.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (listFiles == null) {
            return new ResponseEntity<>(Utils.FILES_ARE_REQUIRED_TO_UPLOAD, HttpStatus.BAD_REQUEST);
        }

        List<UploadFiles> uploadLists = listFiles
                .stream()
                .map(i -> uploadFileToS3.uploadCustomerFiles(FilesConfig.convertMultipartToFile(i), "customer/additional"))
                .toList();

        if (uploadLists != null && !uploadLists.isEmpty()) {

            List<CustomerDocuments> customerDocuments = uploadLists
                    .stream()
                    .map(i -> {
                        CustomerDocuments cd = new CustomerDocuments();
                        cd.setCustomerId(customerId);
                        cd.setHostelId(hostelId);
                        cd.setDocumentUrl(i.fileName());
                        cd.setIsDeleted(false);
                        cd.setIsActive(true);
                        cd.setCreatedBy(authentication.getName());
                        cd.setCreatedAt(new Date());
                        cd.setCreatedByUserType(UserType.ADMIN.name());
                        if (i.fileFormat().equalsIgnoreCase("image/png")
                                || i.fileFormat().equalsIgnoreCase("image/jpeg")
                                || i.fileFormat().equalsIgnoreCase("image/jpg")) {
                            cd.setDocumentFileType(FileFormat.IMAGE.name());
                        }
                        else if (i.fileFormat().equalsIgnoreCase("application/pdf")) {
                            cd.setDocumentFileType(FileFormat.PDF.name());
                        }
                        else if (i.fileFormat().equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                            cd.setDocumentFileType(FileFormat.DOC.name());
                        }
                        if (uploadDocuments.type().equalsIgnoreCase(DocumentType.KYC.name())) {
                            cd.setDocumentType(DocumentType.KYC.name());
                        }
                        else if (uploadDocuments.type().equalsIgnoreCase(DocumentType.CHECKIN.name())) {
                            cd.setDocumentType(DocumentType.CHECKIN.name());
                        }
                        else {
                            cd.setDocumentType(DocumentType.OTHER.name());
                        }


                        return cd;
                    })
                    .toList();

            customerDocumentsRepositories.saveAll(customerDocuments);

        }


        usersService.addUserLog(hostelId, customerId, ActivitySource.CUSTOMERS, ActivitySourceType.FILES_UPLOAD, users);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);

    }

    public CustomerFiles getCustomerFiles(String customerId) {
        List<CustomerDocuments> listDocuments = customerDocumentsRepositories.findByCustomerIdAndIsDeletedFalse(customerId);

        List<FileDetails> kycDocs = listDocuments
                .stream()
                .filter(i -> i.getDocumentType().equalsIgnoreCase(DocumentType.KYC.name()))
                .map(i -> {
                    String type = null;
                    if (i.getDocumentFileType().equalsIgnoreCase(FileFormat.PDF.name())) {
                        type = FileFormat.PDF.name();
                    }
                    else if (i.getDocumentFileType().equalsIgnoreCase(FileFormat.DOC.name())) {
                        type = FileFormat.DOC.name();
                    }
                    else if (i.getDocumentFileType().equalsIgnoreCase(FileFormat.IMAGE.name())) {
                        type = FileFormat.IMAGE.name();
                    }
                    return new FileDetails(i.getDocumentId(), i.getDocumentUrl(), type);
                })
                .toList();

        List<FileDetails> checkIn = listDocuments
                .stream()
                .filter(i -> i.getDocumentType().equalsIgnoreCase(DocumentType.CHECKIN.name()))
                .map(i -> {
                    String type = null;
                    if (i.getDocumentFileType().equalsIgnoreCase(FileFormat.PDF.name())) {
                        type = FileFormat.PDF.name();
                    }
                    else if (i.getDocumentFileType().equalsIgnoreCase(FileFormat.DOC.name())) {
                        type = FileFormat.DOC.name();
                    }
                    else if (i.getDocumentFileType().equalsIgnoreCase(FileFormat.IMAGE.name())) {
                        type = FileFormat.IMAGE.name();
                    }
                    return new FileDetails(i.getDocumentId(), i.getDocumentUrl(), type);
                })
                .toList();

        List<FileDetails> other = listDocuments
                .stream()
                .filter(i -> i.getDocumentType().equalsIgnoreCase(DocumentType.OTHER.name()))
                .map(i -> {
                    String type = null;
                    if (i.getDocumentFileType().equalsIgnoreCase(FileFormat.PDF.name())) {
                        type = FileFormat.PDF.name();
                    }
                    else if (i.getDocumentFileType().equalsIgnoreCase(FileFormat.DOC.name())) {
                        type = FileFormat.DOC.name();
                    }
                    else if (i.getDocumentFileType().equalsIgnoreCase(FileFormat.IMAGE.name())) {
                        type = FileFormat.IMAGE.name();
                    }
                    return new FileDetails(i.getDocumentId(), i.getDocumentUrl(), type);
                })
                .toList();

        return new CustomerFiles(kycDocs, checkIn, other);
    }

    public ResponseEntity<?> deleteDocument(String hostelId, String customerId, String documentId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Customers customers = customersService.getCustomerInformation(customerId);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }

        if (!hostelId.equalsIgnoreCase(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (documentId == null) {
            return new ResponseEntity<>(Utils.INVALID_DOCUMENT_ID, HttpStatus.BAD_REQUEST);
        }

        Long docId = 0l;
        try {
            docId = Long.valueOf(documentId);
        }
        catch (Exception e) {
            return  new ResponseEntity<>(Utils.INVALID_DOCUMENT_ID, HttpStatus.BAD_REQUEST);
        }

        CustomerDocuments customerDocuments = customerDocumentsRepositories.findByDocumentIdAndCustomerId(docId, customerId);
        if (customerDocuments == null) {
            return new ResponseEntity<>(Utils.INVALID_DOCUMENT_ID, HttpStatus.BAD_REQUEST);
        }
        if (customerDocuments.getIsDeleted() != null && customerDocuments.getIsDeleted()) {
            return new ResponseEntity<>(Utils.DOCUMENT_ALREADY_DELETED, HttpStatus.BAD_REQUEST);
        }

        customerDocuments.setIsDeleted(true);
        customerDocuments.setUpdatedAt(new Date());
        customerDocuments.setUpdatedBy(authentication.getName());

        customerDocumentsRepositories.save(customerDocuments);
        usersService.addUserLog(hostelId, documentId, ActivitySource.CUSTOMERS, ActivitySourceType.FILE_DELETE, users);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
