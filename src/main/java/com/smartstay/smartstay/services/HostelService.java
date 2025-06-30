package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.HostelsMapper;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.payloads.AddHostelPayloads;
import com.smartstay.smartstay.payloads.ZohoSubscriptionRequest;
import com.smartstay.smartstay.repositories.HostelV1Repository;
import com.smartstay.smartstay.repositories.UserHostelRepository;
import com.smartstay.smartstay.repositories.UserRepository;
import com.smartstay.smartstay.responses.Hostels;
import com.smartstay.smartstay.responses.ZohoSubscription;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HostelService {

    @Autowired
    private UploadFileToS3 uploadToS3;

    @Autowired
    private HostelV1Repository hostelV1Repository;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserHostelRepository userHostelRepo;

    public ResponseEntity<?> addHostel(MultipartFile mainImage, List<MultipartFile> additionalImages, AddHostelPayloads payloads) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>("Invalid user.", HttpStatus.UNAUTHORIZED);
        }
        String userId = authentication.getName();
        String emailId = payloads.emailId();
        Users users = userRepository.findUserByUserId(userId);

        if (payloads.emailId() == null || payloads.emailId().equalsIgnoreCase("")) {
            emailId = users.getEmailId();
        }
        else if (!Utils.verifyEmail(payloads.emailId())) {
            emailId = userRepository.findUserByUserId(userId).getEmailId();
        }

        ZohoSubscriptionRequest request = formSubscription(payloads, emailId);

        HostelV1 hostelV1 = new HostelV1();
        hostelV1.setHostelId(hostelIdGenerator());
        hostelV1.setCreatedBy(userId);
        hostelV1.setHostelType(1);
        hostelV1.setHostelName(payloads.hostelName());
        hostelV1.setCity(payloads.city());
        hostelV1.setCountry(1);
        hostelV1.setCreatedAt(Calendar.getInstance().getTime());
        hostelV1.setUpdatedAt(Calendar.getInstance().getTime());
        hostelV1.setLandmark(payloads.landmark());
        hostelV1.setMobile(payloads.mobile());
        hostelV1.setEmailId(payloads.emailId());
        hostelV1.setHouseNo(payloads.houseNo());
        hostelV1.setPincode(payloads.pincode());
        hostelV1.setStreet(payloads.street());
        hostelV1.setState(payloads.state());

        if (mainImage != null) {
            String mainImageUrl = uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(mainImage), "Hostel-Images");
            hostelV1.setMainImage(mainImageUrl);
        }


        List<String> listImageUrls = new ArrayList<>();
        if (additionalImages != null && !additionalImages.isEmpty()) {
            listImageUrls = additionalImages.stream().map(multipartFile -> uploadToS3.uploadFileToS3(FilesConfig.convertMultipartToFile(multipartFile), "Hostel-Images")).collect(Collectors.toList());
        }

        if (!listImageUrls.isEmpty()) {
            List<HostelImages> listHostelImages = listImageUrls.stream().map(item -> {
                HostelImages hostelImg = new HostelImages();
                hostelImg.setCreatedBy(userId);
                hostelImg.setImageUrl(item);
                hostelImg.setHostel(hostelV1);
                return hostelImg;
            }).toList();

            hostelV1.setAdditionalImages(listHostelImages);
        }

        Subscription subscription = subscriptionService.addSubscription(request, 1);

        if (subscription != null) {
            subscription.setHostel(hostelV1);
            hostelV1.setSubscription(subscription);
            hostelV1Repository.save(hostelV1);
            mapUserHostel(userId, hostelV1.getHostelId());

            return new ResponseEntity<>("Created successfully", HttpStatus.CREATED);
        }
        else {
            return new ResponseEntity<>("Failed to subscribe in zoho", HttpStatus.BAD_REQUEST);
        }
    }


    public String hostelIdGenerator() {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        HostelV1 v1 = hostelV1Repository.findById(uuidString).orElse(null);
        if (v1 == null) {
            return uuidString;
        }
        return hostelIdGenerator();
    }

    protected ZohoSubscriptionRequest formSubscription(AddHostelPayloads payloads, String emailId) {

        Calendar cal = Calendar.getInstance();

        ZohoSubscriptionRequest.Plan plan = new ZohoSubscriptionRequest.Plan();
        plan.setPlan_code("one_day");

        ZohoSubscriptionRequest.Customer customer = new ZohoSubscriptionRequest.Customer();
        customer.setDisplay_name(payloads.hostelName());
        customer.setFirst_name(payloads.hostelName());
        customer.setEmail(emailId);
        customer.setMobile(payloads.mobile());

        ZohoSubscriptionRequest request = new ZohoSubscriptionRequest();
        request.setPlan(plan);
        request.setCustomer(customer);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        request.setStarts_at(sdf.format(cal.getTime()));
        request.setNotes("New Plan");
        return request;
    }

    public ResponseEntity<?> getAllHostels() {
        List<HostelV1> listHotels = hostelV1Repository.findAll();

        List<Hostels> list = listHotels.stream().map(hostelV1 -> new HostelsMapper().apply(hostelV1)).toList();

        return new ResponseEntity<>(list, HttpStatus.OK);
    }


    public List<UserHostel> fetchAllHostels(String userId) {
        return userHostelRepo.findByUserId(userId);
    }

    public void mapUserHostel(String userId, String hostelId) {
        UserHostel userHostel = new UserHostel();
        userHostel.setHostleId(hostelId);
        userHostel.setUserId(userId);

        userHostelRepo.save(userHostel);
    }
}

