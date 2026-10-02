package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.plans.PlanListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.RestTemplateLoggingInterceptor;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dto.subscription.PaymentSession;
import com.smartstay.smartstay.dto.subscription.SubscriptionDto;
import com.smartstay.smartstay.ennum.ActivitySource;
import com.smartstay.smartstay.ennum.ActivitySourceType;
import com.smartstay.smartstay.ennum.PaymentStatus;
import com.smartstay.smartstay.ennum.PlanType;
import com.smartstay.smartstay.payloads.subscription.PaymentLinks;
import com.smartstay.smartstay.repositories.SubscriptionRepository;
import com.smartstay.smartstay.responses.plans.PlansList;
import com.smartstay.smartstay.responses.subscriptions.PaymentSessionResponse;
import com.smartstay.smartstay.sockets.ClientConnect;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class SubscriptionService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private PlansService plansService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserHostelService userHostelService;
    private HostelService hostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private ClientConnect clientConnect;
    @Autowired
    private OrderHistoryService orderHistoryService;
    @Autowired
    private PaymentSessionService paymentSessionService;
    @Value("${PAYMENT_URL}")
    private String paymentUrl;
    @Value("${PAYMENT_API_KEY}")
    private String paymentApiKey;
    @Value("${PAYMENT_ENVIRONMENT}")
    private String paymentEnvironment;
    private final RestTemplate restTemplate;
    @Value("${REPORTS_URL}")
    private String reportsUrl;

    @Value("${IOS_SUBSCRIPTION_ALLOWED_EMAIL:smartstay@gmail.com}")
    private String iosSubscriptionAllowedEmail;

    private static final String IOS_RENEWAL_SUCCESS_STATUS = "success";

    @Autowired
    public void setHostelService(@Lazy HostelService hostelService) {
        this.hostelService = hostelService;
    }

    public SubscriptionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    public void addHostel(String hostelId, Date joiningDate) {
        if (!authentication.isAuthenticated()) {
            return;
        }

        Plans plans = plansService.getTrialPlan();
        Date endingDate = null;
        if (plans != null) {
            endingDate = Utils.addDaysToDate(joiningDate, plans.getDuration().intValue());
        }
        Subscription history = new Subscription();
        history.setHostelId(hostelId);
        history.setPlanAmount(plans.getPrice());
        history.setPaidAmount(0.0);
        history.setPlanCode(plans.getPlanCode());
        history.setPlanName(plans.getPlanName());
        history.setPlanStartsAt(joiningDate);
        history.setPlanEndsAt(endingDate);
        history.setActivatedAt(new Date());
        history.setCreatedAt(new Date());

        subscriptionRepository.save(history);

    }

    public ResponseEntity<?> getCurrentPlan(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

        List<Subscription> subscription = subscriptionRepository.findByHostelId(hostelId);
        if (subscription == null) {
            return new ResponseEntity<>(Utils.INVALID, HttpStatus.BAD_REQUEST);
        }

        List<com.smartstay.smartstay.responses.subscriptions.Subscription> listSubscriptionResponse = subscription
                .stream()
                .map(i -> new com.smartstay.smartstay.responses.subscriptions.Subscription(
                        i.getSubscriptionId(),
                        Utils.dateToString(i.getPlanStartsAt()),
                        Utils.dateToString(i.getPlanEndsAt()),
                        i.getSubscriptionNumber(),
                        i.getPlanName(),
                        i.getPlanCode(),
                        i.getInvoiceUrl()))
                .toList();

        return new ResponseEntity<>(listSubscriptionResponse, HttpStatus.OK);

    }

    public boolean isSubscriptionValidToday(String hostelId) {
        Subscription subscription = subscriptionRepository.checkSubscriptionForToday(hostelId, new Date());
        return subscription != null;
    }

    public SubscriptionDto getCurrentSubscriptionDetails(String hostelId) {
        Subscription subscriptionToday = subscriptionRepository.checkSubscriptionForToday(hostelId, new Date());
        SubscriptionDto subscriptionDto = null;
        if (subscriptionToday != null) {
            boolean isValid = false;
            int planEndsIn = 0;

            Date nextBillingDate = Utils.addDaysToDate(subscriptionToday.getPlanEndsAt(), 1);

            if (Utils.compareWithTwoDates(subscriptionToday.getPlanStartsAt(), new Date()) <= 0) {
                if (Utils.compareWithTwoDates(subscriptionToday.getPlanEndsAt(), new Date()) >= 0) {
                    isValid = true;
                }
            }
            if (isValid) {
                long numberOfDays = Utils.findNumberOfDays(new Date(), subscriptionToday.getPlanEndsAt());
                planEndsIn = (int) numberOfDays;
            }
            subscriptionDto = new SubscriptionDto(subscriptionToday.getPlanStartsAt(),
                    subscriptionToday.getPlanEndsAt(),
                    nextBillingDate,
                    isValid,
                    planEndsIn);
        }

        return subscriptionDto;
    }

    public boolean validateSubscription(String hostelId) {
        SubscriptionDto subscriptionDto = getCurrentSubscriptionDetails(hostelId);
        return subscriptionDto != null && subscriptionDto.isValid();
    }

    public ResponseEntity<?> subscribeSingleHostelOld(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_SUBSCRIPTION, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        Subscription latestSubscription = subscriptionRepository.findLatestSubscription(hostelId);
        if (latestSubscription == null) {
            return new ResponseEntity<>(Utils.INVALID_SUBSCRIPTION, HttpStatus.BAD_REQUEST);
        }
        Subscription newSubscription = new Subscription();
        newSubscription.setSubscriptionNumber(latestSubscription.getSubscriptionNumber());
        newSubscription.setHostelId(hostelId);
        newSubscription.setPlanCode(latestSubscription.getPlanCode());
        newSubscription.setPlanName(latestSubscription.getPlanName());
        newSubscription.setPlanStartsAt(new Date());
        newSubscription.setPaidAmount(0.0);
        newSubscription.setPlanAmount(0.0);
        newSubscription.setDiscount(0.0);
        newSubscription.setDiscountAmount(0.0);
        newSubscription.setCreatedAt(new Date());

        if (latestSubscription.getPlanEndsAt() != null) {
            if (Utils.compareWithTwoDates(latestSubscription.getPlanEndsAt(), new Date()) < 0) {
                newSubscription.setPlanStartsAt(new Date());
                Date endDate = Utils.addDaysToDate(new Date(), 30);
                newSubscription.setPlanEndsAt(endDate);
                newSubscription.setNextBillingAt(endDate);
                newSubscription.setActivatedAt(endDate);
            } else {
                Date startDate = Utils.addDaysToDate(latestSubscription.getPlanEndsAt(), 1);
                newSubscription.setPlanStartsAt(startDate);
                Date endDate = Utils.addDaysToDate(startDate, 30);
                newSubscription.setPlanEndsAt(endDate);
                newSubscription.setNextBillingAt(endDate);
                newSubscription.setActivatedAt(new Date());
            }
        }

        Subscription sub = subscriptionRepository.save(newSubscription);

        if (Utils.compareWithTwoDates(latestSubscription.getPlanEndsAt(), new Date()) <= 0) {
            HostelPlan hostelPlan = hostelV1.getHostelPlan();
            if (hostelPlan == null) {
                hostelPlan = new HostelPlan();
                hostelPlan.setCurrentPlanCode(latestSubscription.getPlanCode());
                hostelPlan.setCurrentPlanName(latestSubscription.getPlanName());
                hostelPlan.setHostel(hostelV1);
            }
            hostelPlan.setCurrentPlanStartsAt(newSubscription.getPlanStartsAt());
            hostelPlan.setCurrentPlanEndsAt(newSubscription.getPlanEndsAt());
            hostelPlan.setCurrentPlanPrice(0.0);
            hostelPlan.setPaidAmount(0.0);
            hostelPlan.setTrial(true);
            hostelPlan.setTrialEndingAt(newSubscription.getPlanEndsAt());

            hostelService.updateHostel(hostelV1);
        }

        usersService.addUserLog(hostelId,String.valueOf(sub.getSubscriptionId()), ActivitySource.SUBSCRIPTION, ActivitySourceType.CREATE, users);

        return new ResponseEntity<>(HttpStatus.OK);

    }

    public ResponseEntity<?> subscribeSingleHostel(String hostelId, com.smartstay.smartstay.payloads.subscription.Subscription subscription) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_SUBSCRIPTION, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (subscription.planCode() == null) {
            return new ResponseEntity<>(Utils.PLAN_CODE_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        Plans plans = plansService.findPlanByPlanCode(subscription.planCode());
        if (plans == null) {
            return new ResponseEntity<>(Utils.INVALID_PLAN_CODE, HttpStatus.BAD_REQUEST);
        }
        if (plans.getPlanType().equalsIgnoreCase(PlanType.TRIAL.name())) {
            return new ResponseEntity<>(Utils.CANNOT_MAKE_PAYMENT_FOR_TRIAL_PERIOD, HttpStatus.BAD_REQUEST);
        }

        Subscription latestSubscription = subscriptionRepository.findLatestSubscription(hostelId);
        if (latestSubscription == null) {
            return new ResponseEntity<>(Utils.INVALID_SUBSCRIPTION, HttpStatus.BAD_REQUEST);
        }

        double discountAmount = 0.0;
        double discountPercentage = 0.0;
        double totalAmount = plans.getFinalPrice();
//        if (subscription.discountAmount() != null) {
//            discountAmount = subscription.discountAmount();
//            discountPercentage = (discountAmount / totalAmount) * 100;
//        }
//        else if (subscription.discountPercentage() != null) {
//            discountPercentage = subscription.discountPercentage();
//            discountAmount = (discountPercentage/100) * totalAmount;
//        }
        double finalAmount = totalAmount - discountAmount;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", finalAmount);
        requestBody.put("currency", "INR");
        requestBody.put("description", "Plan renewal");

        String paymentLink = paymentUrl + "/v2/payments/generate/" + hostelId ;

//        String paymentUrl = "http://localhost:8083/v2/payments/generate/" + hostelId ;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody);

        ResponseEntity<PaymentLinks> responseEntity = restTemplate.exchange(paymentLink, HttpMethod.POST, entity, PaymentLinks.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            PaymentLinks details = responseEntity.getBody();
            orderHistoryService.createOrder(hostelId, details, finalAmount, plans.getPlanCode(), discountAmount, plans.getPrice());
            try {
                clientConnect.connect(details.paymentLinkId());
            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
            }
            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }

    }

    public Subscription findLatestSubscription(String hostelId) {
        return subscriptionRepository.findLatestSubscription(hostelId);
    }

    public void saveFromEvents(Subscription subscription) {
        subscriptionRepository.save(subscription);
    }

    public ResponseEntity<?> checkIosSubscription(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (com.smartstay.smartstay.ennum.Platform.IOS
                != com.smartstay.smartstay.ennum.Platform.fromValue(authentication.getSource())) {
            return new ResponseEntity<>(Utils.IOS_SUBSCRIPTION_ONLY, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_SUBSCRIPTION, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
//        if (!validateSubscription(hostelId)) {
//            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
//        }
        if (!isIosSubscriptionAllowed(users)) {
            return new ResponseEntity<>(Utils.IOS_SUBSCRIPTION_NOT_ALLOWED, HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(Utils.IOS_SUBSCRIPTION_ALLOWED, HttpStatus.OK);
    }

    private boolean isIosSubscriptionAllowed(Users users) {
        return users.getEmailId() != null
                && users.getEmailId().trim().equalsIgnoreCase(iosSubscriptionAllowedEmail);
    }

    @Transactional
    public ResponseEntity<?> subscribeIosHostels(String hostelId,
                                                 com.smartstay.smartstay.payloads.subscription.IosSubscription payload) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (com.smartstay.smartstay.ennum.Platform.IOS
                != com.smartstay.smartstay.ennum.Platform.fromValue(authentication.getSource())) {
            return new ResponseEntity<>(Utils.IOS_SUBSCRIPTION_ONLY, HttpStatus.BAD_REQUEST);
        }
        if (payload == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_SUBSCRIPTION, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (!isIosSubscriptionAllowed(users)) {
            return new ResponseEntity<>(Utils.IOS_SUBSCRIPTION_NOT_ALLOWED, HttpStatus.FORBIDDEN);
        }
        if (payload.status() == null || !payload.status().trim().equalsIgnoreCase(IOS_RENEWAL_SUCCESS_STATUS)) {
            return new ResponseEntity<>(Utils.IOS_RENEWAL_NOT_SUCCESSFUL, HttpStatus.BAD_REQUEST);
        }

        Date transactionDate = parseIosRenewalDate(payload.transactionDate());
        Date renewalDate = parseIosRenewalDate(payload.renewalDate());
        if (transactionDate == null || renewalDate == null || renewalDate.before(transactionDate)) {
            return new ResponseEntity<>(Utils.IOS_INVALID_RENEWAL_DATES, HttpStatus.BAD_REQUEST);
        }

        List<String> accountHostelIds = userHostelService.findByUserId(users.getUserId())
                .stream()
                .map(com.smartstay.smartstay.dao.UserHostel::getHostelId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (accountHostelIds.isEmpty()) {
            return new ResponseEntity<>(Utils.IOS_NO_HOSTELS_TO_SUBSCRIBE, HttpStatus.BAD_REQUEST);
        }

        int renewed = 0;
        int alreadyRenewed = 0;
        for (String accountHostelId : accountHostelIds) {
            HostelV1 hostel = hostelService.getHostelInfo(accountHostelId);
            if (hostel == null) {
                continue;
            }
            Subscription latest = subscriptionRepository.findLatestSubscription(accountHostelId);
            if (latest != null && latest.getPlanEndsAt() != null
                    && Utils.compareWithTwoDates(latest.getPlanEndsAt(), renewalDate) == 0) {
                alreadyRenewed++;
                continue;
            }
            Plans plan = resolveCurrentPlan(latest, hostel);
            if (plan == null) {
                continue;
            }
            applyIosRenewal(hostel, plan, users, transactionDate, renewalDate);
            renewed++;
        }

        if (renewed == 0 && alreadyRenewed == 0) {
            return new ResponseEntity<>(Utils.IOS_NO_HOSTELS_TO_SUBSCRIBE, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(Utils.IOS_HOSTELS_SUBSCRIBED, HttpStatus.OK);
    }

    private Plans resolveCurrentPlan(Subscription latest, HostelV1 hostel) {
        String planCode = latest != null ? latest.getPlanCode() : null;
        if (planCode == null && hostel.getHostelPlan() != null) {
            planCode = hostel.getHostelPlan().getCurrentPlanCode();
        }
        if (planCode == null || planCode.isBlank()) {
            return null;
        }
        return plansService.findPlanByPlanCode(planCode);
    }

    private void applyIosRenewal(HostelV1 hostel, Plans plan, Users users, Date startDate, Date endDate) {
        Subscription subscription = new Subscription();
        subscription.setHostelId(hostel.getHostelId());
        subscription.setPlanCode(plan.getPlanCode());
        subscription.setPlanName(plan.getPlanName());
        subscription.setPlanStartsAt(startDate);
        subscription.setPlanEndsAt(endDate);
        subscription.setActivatedAt(startDate);
        subscription.setNextBillingAt(endDate);
        subscription.setPlanAmount(plan.getFinalPrice());
        subscription.setPaidAmount(plan.getFinalPrice());
        subscription.setDiscount(0.0);
        subscription.setDiscountAmount(0.0);
        subscription.setCreatedAt(new Date());
        subscription.setCreatedBy(users.getUserId());
        subscription.setIsActive(true);
        subscriptionRepository.save(subscription);

        com.smartstay.smartstay.dao.HostelPlan hostelPlan = hostel.getHostelPlan();
        if (hostelPlan == null) {
            hostelPlan = new com.smartstay.smartstay.dao.HostelPlan();
            hostelPlan.setHostel(hostel);
        }
        hostelPlan.setCurrentPlanCode(plan.getPlanCode());
        hostelPlan.setCurrentPlanName(plan.getPlanName());
        hostelPlan.setCurrentPlanStartsAt(startDate);
        hostelPlan.setCurrentPlanEndsAt(endDate);
        hostelPlan.setCurrentPlanPrice(plan.getFinalPrice());
        hostelPlan.setPaidAmount(plan.getFinalPrice());
        hostelPlan.setTrial(false);
        hostelPlan.setTrialEndingAt(null);
        hostel.setHostelPlan(hostelPlan);
        hostelService.updateHostel(hostel);
    }

    private Date parseIosRenewalDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.matches("\\d{10,14}")) {
            return new Date(Long.parseLong(trimmed));
        }
        String[] patterns = {Utils.DATE_FORMAT_ZOHO, Utils.USER_INPUT_DATE_FORMAT, "yyyy-MM-dd'T'HH:mm:ss",
                Utils.INPUT_DATE_TIME_FORMAT};
        for (String pattern : patterns) {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(pattern);
            formatter.setLenient(false);
            java.text.ParsePosition position = new java.text.ParsePosition(0);
            Date parsed = formatter.parse(trimmed, position);
            if (parsed != null && position.getIndex() == trimmed.length()) {
                return parsed;
            }
        }
        return null;
    }

    public ResponseEntity<?> addSubscriptionMobile(String hostelId, com.smartstay.smartstay.payloads.subscription.Subscription subscription) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_SUBSCRIPTION, Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (subscription == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        if (authentication.getSource().equalsIgnoreCase("web")) {
            return new ResponseEntity<>(Utils.INVALID_PLATFORM, HttpStatus.BAD_REQUEST);
        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }
        if (subscription.planCode() == null) {
            return new ResponseEntity<>(Utils.PLAN_CODE_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        Plans plans = plansService.findPlanByPlanCode(subscription.planCode());
        if (plans == null) {
            return new ResponseEntity<>(Utils.INVALID_PLAN_CODE, HttpStatus.BAD_REQUEST);
        }
        if (plans.getPlanType().equalsIgnoreCase(PlanType.TRIAL.name())) {
            return new ResponseEntity<>(Utils.CANNOT_MAKE_PAYMENT_FOR_TRIAL_PERIOD, HttpStatus.BAD_REQUEST);
        }
        if (plans.getPlanType().equalsIgnoreCase(PlanType.ADVANCED.name())) {
            return new ResponseEntity<>(Utils.ADVANCE_PLAN_SUBSCRIPTION_NOT_AVAILABLE, HttpStatus.BAD_REQUEST);
        }

        Subscription latestSubscription = subscriptionRepository.findLatestSubscription(hostelId);
        if (latestSubscription == null) {
            return new ResponseEntity<>(Utils.INVALID_SUBSCRIPTION, HttpStatus.BAD_REQUEST);
        }

//        return new ResponseEntity<>("Please use web application for subscribing", HttpStatus.BAD_REQUEST);

        double discountAmount = 0.0;
        double discountPercentage = 0.0;
        double totalAmount = plans.getFinalPrice();
//        if (subscription.discountAmount() != null) {
//            discountAmount = subscription.discountAmount();
//            discountPercentage = (discountAmount / totalAmount) * 100;
//        }
//        else if (subscription.discountPercentage() != null) {
//            discountPercentage = subscription.discountPercentage();
//            discountAmount = (discountPercentage/100) * totalAmount;
//        }
        double finalAmount = totalAmount - discountAmount;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", finalAmount);
        requestBody.put("currency", "INR");
        requestBody.put("description", "Plan renewal");

        String paymentLink = paymentUrl +  "/v2/payments/session/" + hostelId ;

//        String paymentUrl = "http://localhost:8083/v2/payments/session/" + hostelId ;
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody);

        ResponseEntity<PaymentSession> responseEntity = restTemplate.exchange(paymentLink, HttpMethod.POST, entity, PaymentSession.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            PaymentSession session = responseEntity.getBody();
            if (session != null) {
                //values are hard coded. Front end SDK needs this is order to collect payments
                PaymentSessionResponse response = new PaymentSessionResponse(session.getSessionId(),
                        session.getAmount(),
                        paymentApiKey,
                        "60035196766",
                        paymentEnvironment);
                PaymentSessions paymentSessions = paymentSessionService.addPaymentSession(session.getSessionId(), session.getAmount(), hostelId, discountAmount, plans.getFinalPrice(), plans.getPlanCode());
                try {
                    clientConnect.connect(hostelId + "-" + session.getSessionId());
                } catch (ExecutionException e) {
//                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
                }

                usersService.addUserLog(hostelId, paymentSessions.getPaymentSessionId(), ActivitySource.PAYMENTS, ActivitySourceType.CREATE_SESSION, users);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }


        }

        return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> verifyPayment(String hostelId, String paymentId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_SUBSCRIPTION, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (authentication.getSource().equalsIgnoreCase("web")) {
            return new ResponseEntity<>(Utils.INVALID_PLATFORM, HttpStatus.BAD_REQUEST);
        }

        String paymentStatusUrl = paymentUrl + "/v2/payments/" + paymentId ;
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(paymentStatusUrl, String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return new ResponseEntity<>(Utils.PAYMENT_SUCCESS, HttpStatus.OK);
        }

        return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> downloadInvoice(String hostelId, String subscriptionId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_REPORTS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        Subscription subscription = subscriptionRepository.findBySubscriptionId(Long.valueOf(subscriptionId));
        if (subscription == null) {
            return new ResponseEntity<>(Utils.INVALID_SUBSCRIPTION_ID, HttpStatus.BAD_REQUEST);
        }
        if (!subscription.getHostelId().equalsIgnoreCase(hostelId)) {
            return new ResponseEntity<>(Utils.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }

//        if (subscription.getPaymentStatus().equalsIgnoreCase(com.smartstay.smartstay.ennum.PaymentStatus.PAID.name())) {
            if (subscription.getInvoiceUrl() != null) {
                return new ResponseEntity<>(subscription.getInvoiceUrl(), HttpStatus.OK);
            }
//        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String endpoint = reportsUrl + "/v2/reports/subscriptions/" + hostelId + "/" + subscriptionId;
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
//            if (subscription.getPaymentStatus().equalsIgnoreCase(PaymentStatus.PAID.name())) {
                subscription.setInvoiceUrl(response.getBody());
                subscriptionRepository.save(subscription);
//            }
            return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
        }

    }

    public List<Subscription> getSubscriptionList(String hostelId) {
        List<Subscription> listSubscriptions = subscriptionRepository.findByHostelId(hostelId);
        if (listSubscriptions == null) {
            listSubscriptions = new ArrayList<>();
        }
        return listSubscriptions;
    }

    public Subscription findCurrentSubscription(String hostelId) {
        return subscriptionRepository.checkSubscriptionForToday(hostelId, new Date());
    }

    public List<String> findActiveSubscriptionHostels(List<String> hostelIds) {
        List<Subscription> listSubscriptions = subscriptionRepository.findByHostelIdsAndDate(hostelIds, new Date());
        if (listSubscriptions != null) {
            List<String> activeHostelIds = listSubscriptions
                    .stream()
                    .map(Subscription::getHostelId)
                    .distinct()
                    .toList();
            List<String> hostelsWithoutSubscription = hostelIds.stream()
                    .filter(id -> !activeHostelIds.contains(id))
                    .toList();
            return hostelsWithoutSubscription;
        }

        return new ArrayList<>();
    }
}
