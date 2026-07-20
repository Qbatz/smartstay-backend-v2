package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Draft;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.customer.Deductions;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.ennum.DraftType;
import com.smartstay.smartstay.ennum.StayType;
import com.smartstay.smartstay.payloads.drafts.UpdateDrafts;
import com.smartstay.smartstay.repositories.DraftsRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CustomerDraftService {
    @Autowired
    private FloorsService floorsService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private BedsService bedsService;
    @Autowired
    private DraftsRepository draftsRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;

    public ResponseEntity<?> updateDraftInfo(String customerId, UpdateDrafts updateDrafts) {
        Draft draft = draftsRepository.findById(customerId).orElse(null);
        if (draft == null) {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }
        Double bookingAmount = 0.0;
        if (updateDrafts.operationType() != null) {
            if (updateDrafts.operationType().equalsIgnoreCase("BOOKING")) {
                draft.setType(DraftType.BOOKING.name());

                if (updateDrafts.rent() != null) {
                    draft.setRentalAmount(updateDrafts.rent());
                }

                if (updateDrafts.bookingDate() != null) {
                    Date bookingDate = Utils.stringToDate(updateDrafts.bookingDate().replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
                    draft.setBookingDate(bookingDate);
                }
                if (updateDrafts.bookingAmount() != null) {
                    bookingAmount = updateDrafts.bookingAmount();
                    draft.setBookingAmount(bookingAmount);
                }
            }
            else if (updateDrafts.operationType().equalsIgnoreCase("CHECK_IN")) {
                draft.setType(DraftType.CHECK_IN.name());
                if (updateDrafts.stayType() != null) {
                    if (updateDrafts.stayType().equalsIgnoreCase(StayType.LONG.name())) {
                        draft.setStayType(StayType.LONG.name());
                    }
                    else if (updateDrafts.stayType().equalsIgnoreCase(StayType.SHORT.name())) {
                        draft.setStayType(StayType.SHORT.name());
                    }
                }
                else {
                    draft.setStayType(StayType.LONG.name());
                }

                if (updateDrafts.refundableAdvance() == null ) {
                    draft.setHasRefundableAdvance(false);
                    draft.setAdvanceAmount(0.0);
                    draft.setTotalAdvanceAmount(0.0);
                }
                else {
                    if (updateDrafts.refundableAdvance() == 0) {
                        draft.setHasRefundableAdvance(false);
                        draft.setAdvanceAmount(0.0);
                        draft.setTotalAdvanceAmount(0.0);
                    }
                    else {
                        draft.setHasRefundableAdvance(true);
                        if (draft.getAdvanceAmount() == null) {
                            return new ResponseEntity<>(Utils.ADVANCE_AMOUNT_REQUIRED, HttpStatus.BAD_REQUEST);
                        }
                        else if (draft.getAdvanceAmount() == 0) {
                            return new ResponseEntity<>(Utils.ADVANCE_AMOUNT_REQUIRED, HttpStatus.BAD_REQUEST);
                        }
                        draft.setAdvanceAmount(draft.getAdvanceAmount());
                        draft.setTotalAdvanceAmount(draft.getTotalAdvanceAmount());
                    }
                }

                if (updateDrafts.advanceDeductions() != null) {
                    double deductionAmount = updateDrafts
                            .advanceDeductions()
                            .stream()
                            .mapToDouble(i -> {
                                if (i.amount() != null) {
                                    return i.amount();
                                }
                                return 0.0;
                            })
                            .sum();
                    List<Deductions> deductionsList = updateDrafts
                            .advanceDeductions()
                            .stream()
                            .map(i -> new Deductions(i.type(), i.amount(), 0.0))
                            .toList();
                    draft.setDeductions(deductionsList);
                    draft.setTotalAdvanceAmount(draft.getTotalAdvanceAmount() + deductionAmount);
                }

                if (updateDrafts.oneTimeDeductions() != null) {
                    double deductionAmount = updateDrafts
                            .oneTimeDeductions()
                            .stream()
                            .mapToDouble(i -> {
                                if (i.amount() != null) {
                                    return i.amount();
                                }
                                return 0.0;
                            })
                            .sum();
                    List<Deductions> deductionsList = updateDrafts
                            .oneTimeDeductions()
                            .stream()
                            .map(i -> new Deductions(i.type(), i.amount(), 0.0))
                            .toList();
                    draft.setOneTimeDeductions(deductionsList);
                }
                if (updateDrafts.rent() != null) {
                    draft.setRentalAmount(updateDrafts.rent());
                }
            }
        }


        if (updateDrafts.joiningDate() != null) {
            Date joiningDate = Utils.stringToDate(updateDrafts.joiningDate().replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            draft.setJoiningDate(joiningDate);
        }
        if (updateDrafts.floorId() != null) {
            if (!floorsService.checkFloorExistForHostel(updateDrafts.floorId(), draft.getHostelId())) {
                return new ResponseEntity<>(Utils.INVALID_FLOOR, HttpStatus.BAD_REQUEST);
            }
            draft.setFloorId(updateDrafts.floorId());

            if (updateDrafts.roomId() != null) {
                if (!roomsService.checkRoomExistForFloor(updateDrafts.floorId(), updateDrafts.roomId())) {
                    return new ResponseEntity<>(Utils.INVALID_ROOM_ID, HttpStatus.BAD_REQUEST);
                }
                draft.setRoomId(updateDrafts.roomId());

                if (updateDrafts.bedId() != null) {
                    if (!bedsService.checkBedExistForRoom(updateDrafts.bedId(), updateDrafts.roomId(), draft.getHostelId())) {
                        return new ResponseEntity<>(Utils.INVALID_BED_ID, HttpStatus.BAD_REQUEST);
                    }
                    draft.setBedId(updateDrafts.bedId());
                }
            }

            if (updateDrafts.bankId() != null) {
                draft.setBankId(updateDrafts.bankId());
            }
            if (updateDrafts.transactionId() != null) {
                draft.setUpdatedBy(authentication.getName());
            }

            draft.setUpdatedAt(new Date());
            draft.setUpdatedBy(authentication.getName());
        }

        draftsRepository.save(draft);

        Users users = usersService.findUserByUserId(authentication.getName());
        if (users != null) {
            usersService.addUserLog(draft.getHostelId(), customerId, ActivitySource.DRAFTS, ActivitySourceType.UPDATE, users);
        }

        return new ResponseEntity<>(Utils.CREATED, HttpStatus.OK);

    }

    public void deleteDraftedCUstomer(String customerId) {
        Draft draft = draftsRepository.findById(customerId).orElse(null);
        if (draft != null) {
            draftsRepository.delete(draft);
        }
    }

    public Draft getCustomerDrafts(String customerId, String hostelId) {
        return draftsRepository.findById(customerId).orElse(null);
    }

    public void findCustomerInDraftAndDelete(String hostelId, String customerId) {
        Draft customerDrafts = draftsRepository.findById(customerId).orElse(null);
        if (customerDrafts != null) {
            draftsRepository.delete(customerDrafts);
        }
    }
}
