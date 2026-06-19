package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dao.VendorComments;
import com.smartstay.smartstay.dao.VendorV1;
import com.smartstay.smartstay.payloads.vendor.AddVendorComment;
import com.smartstay.smartstay.payloads.vendor.UpdateVendorComment;
import com.smartstay.smartstay.repositories.VendorCommentsRepository;
import com.smartstay.smartstay.repositories.VendorRepository;
import com.smartstay.smartstay.responses.vendor.VendorCommentResponse;
import com.smartstay.smartstay.responses.vendor.VendorCommentsResponse;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class VendorCommentService {

    @Autowired
    private VendorCommentsRepository vendorCommentsRepository;
    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private RolesService rolesService;

    @Transactional
    public ResponseEntity<?> addComment(AddVendorComment payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        VendorV1 vendor = vendorRepository.findByVendorId(payloads.vendorId());
        if (vendor == null) {
            return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.BAD_REQUEST);
        }

        Date now = new Date();
        VendorComments vendorComment = new VendorComments();
        vendorComment.setVendorId(payloads.vendorId());
        vendorComment.setComment(payloads.comment().trim());
        vendorComment.setActive(true);
        vendorComment.setCreatedAt(now);
        vendorComment.setCreatedBy(userId);
        vendorComment.setUpdatedAt(now);
        vendorComment.setUpdatedBy(userId);
        vendorCommentsRepository.save(vendorComment);

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.CREATED);
    }

    public ResponseEntity<?> getComments(int vendorId, Integer page, Integer size) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        VendorV1 vendor = vendorRepository.findByVendorId(vendorId);
        if (vendor == null) {
            return new ResponseEntity<>(Utils.INVALID_VENDOR, HttpStatus.BAD_REQUEST);
        }

        int pageNumber = (page == null || page < 1) ? 1 : page;
        int pageSize = (size == null || size < 1) ? 10 : size;
        // Newest comments first, always.
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<VendorComments> commentPage = vendorCommentsRepository.findByVendorIdAndIsActiveTrue(vendorId, pageable);

        // Resolve creator names for the page in one bulk lookup (no N+1).
        List<String> creatorIds = commentPage.getContent().stream()
                .map(VendorComments::getCreatedBy)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<String, String> creatorNamesById = new HashMap<>();
        if (!creatorIds.isEmpty()) {
            usersService.findAllUsersFromUserId(creatorIds).forEach(u ->
                    creatorNamesById.put(u.getUserId(), NameUtils.getFullName(u.getFirstName(), u.getLastName())));
        }

        List<VendorCommentResponse> comments = commentPage.getContent().stream()
                .map(comment -> toResponse(comment, creatorNamesById))
                .toList();

        VendorCommentsResponse response = new VendorCommentsResponse(commentPage.getTotalElements(),
                pageNumber, commentPage.getTotalPages(), pageSize, comments);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> updateComment(Long commentId, UpdateVendorComment payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        VendorComments vendorComment = vendorCommentsRepository.findByIdAndIsActiveTrue(commentId);
        if (vendorComment == null) {
            return new ResponseEntity<>(Utils.INVALID_COMMENT_ID, HttpStatus.BAD_REQUEST);
        }

        // Only the comment text and the update-audit fields change; vendorId/createdAt/createdBy stay.
        vendorComment.setComment(payloads.comment().trim());
        vendorComment.setUpdatedAt(new Date());
        vendorComment.setUpdatedBy(userId);
        vendorCommentsRepository.save(vendorComment);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteComment(Long commentId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        Users user = usersService.findUserByUserId(userId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(user.getRoleId(), Utils.MODULE_ID_VENDOR, Utils.PERMISSION_DELETE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        VendorComments vendorComment = vendorCommentsRepository.findByIdAndIsActiveTrue(commentId);
        if (vendorComment == null) {
            return new ResponseEntity<>(Utils.INVALID_COMMENT_ID, HttpStatus.BAD_REQUEST);
        }

        // Soft delete, consistent with the other vendor entities.
        vendorComment.setActive(false);
        vendorComment.setUpdatedAt(new Date());
        vendorComment.setUpdatedBy(userId);
        vendorCommentsRepository.save(vendorComment);

        return new ResponseEntity<>(Utils.DELETED, HttpStatus.OK);
    }

    private VendorCommentResponse toResponse(VendorComments comment, Map<String, String> creatorNamesById) {
        String createdByName = comment.getCreatedBy() != null
                ? creatorNamesById.getOrDefault(comment.getCreatedBy(), comment.getCreatedBy())
                : null;
        return new VendorCommentResponse(
                comment.getId(),
                comment.getComment(),
                createdByName,
                Utils.dateToDateTime(comment.getCreatedAt()));
    }
}
