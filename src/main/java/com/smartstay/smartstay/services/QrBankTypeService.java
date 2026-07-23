package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.banking.QrBankTypeMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.QrBankType;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.QrType;
import com.smartstay.smartstay.repositories.QrBankTypeRepository;
import com.smartstay.smartstay.responses.banking.QrBankTypeResponse;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QrBankTypeService {

    private static final String S3_FOLDER = "QrBankType";

    @Autowired
    private Authentication authentication;

    @Autowired
    private UsersService usersService;

    @Autowired
    private RolesService rolesService;

    @Autowired
    private QrBankTypeRepository qrBankTypeRepository;

    @Autowired
    private UploadFileToS3 uploadToS3;

    public ResponseEntity<?> create(String type, String name, MultipartFile image) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
//        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_WRITE)) {
//            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
//        }

        QrType qrType = QrType.fromValue(type);
        if (qrType == null) {
            return new ResponseEntity<>(Utils.QR_TYPE_INVALID, HttpStatus.BAD_REQUEST);
        }
        String trimmedName = trimToNull(name);
        if (trimmedName == null) {
            return new ResponseEntity<>(Utils.QR_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (!isValidImage(image)) {
            return new ResponseEntity<>(Utils.QR_IMAGE_INVALID, HttpStatus.BAD_REQUEST);
        }
        if (qrBankTypeRepository.existsByTypeAndNameIgnoreCase(qrType, trimmedName)) {
            return new ResponseEntity<>(Utils.QR_TYPE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        Date now = new Date();
        QrBankType entity = new QrBankType();
        entity.setType(qrType);
        entity.setName(trimmedName);
        entity.setImage(uploadIfPresent(image));
        entity.setCreatedBy(user.getUserId());
        entity.setUpdatedBy(user.getUserId());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        QrBankType saved = qrBankTypeRepository.save(entity);
        return new ResponseEntity<>(new QrBankTypeMapper().apply(saved), HttpStatus.CREATED);
    }

    public ResponseEntity<?> getAll(String type) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
//        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_READ)) {
//            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
//        }

        List<QrBankType> records;
        if (type != null && !type.trim().isEmpty()) {
            QrType qrType = QrType.fromValue(type);
            if (qrType == null) {
                return new ResponseEntity<>(Utils.QR_TYPE_INVALID, HttpStatus.BAD_REQUEST);
            }
            records = qrBankTypeRepository.findAllByTypeOrderByNameAsc(qrType);
        } else {
            records = qrBankTypeRepository.findAllByOrderByTypeAscNameAsc();
        }

        QrBankTypeMapper mapper = new QrBankTypeMapper();
        List<QrBankTypeResponse> response = records.stream().map(mapper).collect(Collectors.toList());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getById(Integer id) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
//        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_READ)) {
//            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
//        }

        Optional<QrBankType> record = qrBankTypeRepository.findById(id);
        if (record.isEmpty()) {
            return new ResponseEntity<>(Utils.QR_TYPE_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new QrBankTypeMapper().apply(record.get()), HttpStatus.OK);
    }

    public ResponseEntity<?> update(Integer id, String type, String name, MultipartFile image) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
//        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_WRITE)) {
//            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
//        }

        Optional<QrBankType> existingOpt = qrBankTypeRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return new ResponseEntity<>(Utils.QR_TYPE_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        QrBankType existing = existingOpt.get();

        QrType qrType = QrType.fromValue(type);
        if (qrType == null) {
            return new ResponseEntity<>(Utils.QR_TYPE_INVALID, HttpStatus.BAD_REQUEST);
        }
        String trimmedName = trimToNull(name);
        if (trimmedName == null) {
            return new ResponseEntity<>(Utils.QR_NAME_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (!isValidImage(image)) {
            return new ResponseEntity<>(Utils.QR_IMAGE_INVALID, HttpStatus.BAD_REQUEST);
        }
        if (qrBankTypeRepository.existsByTypeAndNameIgnoreCaseAndIdNot(qrType, trimmedName, id)) {
            return new ResponseEntity<>(Utils.QR_TYPE_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
        }

        existing.setType(qrType);
        existing.setName(trimmedName);

        // Replace the image only when a new one is supplied; delete the old object to avoid orphans.
        if (image != null && !image.isEmpty()) {
            String oldImage = existing.getImage();
            existing.setImage(uploadIfPresent(image));
            if (oldImage != null && !oldImage.isEmpty()) {
                uploadToS3.deleteFileFromS3(oldImage);
            }
        }

        existing.setUpdatedBy(user.getUserId());
        existing.setUpdatedAt(new Date());

        QrBankType saved = qrBankTypeRepository.save(existing);
        return new ResponseEntity<>(new QrBankTypeMapper().apply(saved), HttpStatus.OK);
    }

    public ResponseEntity<?> delete(Integer id) {
        Users user = currentUser();
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
//        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_BANKING, Utils.PERMISSION_DELETE)) {
//            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
//        }

        Optional<QrBankType> existingOpt = qrBankTypeRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return new ResponseEntity<>(Utils.QR_TYPE_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        QrBankType existing = existingOpt.get();
        String image = existing.getImage();

        qrBankTypeRepository.delete(existing);

        if (image != null && !image.isEmpty()) {
            uploadToS3.deleteFileFromS3(image);
        }
        return new ResponseEntity<>(Utils.QR_TYPE_DELETED, HttpStatus.OK);
    }

    private Users currentUser() {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        return usersService.findUserByUserId(authentication.getName());
    }

    private String trimToNull(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }

    /** Optional field: absent is valid; when present it must be an image. */
    private boolean isValidImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return true;
        }
        String contentType = image.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("image/");
    }

    private String uploadIfPresent(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }
        return uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(image), S3_FOLDER);
    }
}
