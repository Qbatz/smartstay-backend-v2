package com.smartstay.smartstay.aspects;

import com.smartstay.smartstay.Exceptions.SubscriptionExpiredException;
import com.smartstay.smartstay.annotations.RequiresActiveSubscription;
import com.smartstay.smartstay.dto.subscription.SubscriptionDto;
import com.smartstay.smartstay.services.SubscriptionService;
import com.smartstay.smartstay.util.Utils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Aspect
@Component
public class SubscriptionAspect {

    @Autowired
    private SubscriptionService subscriptionService;

    @Before("@annotation(requiresActiveSubscription)")
    public void validateSubscription(JoinPoint joinPoint, RequiresActiveSubscription requiresActiveSubscription) {
        String hostelId = getHostelId(joinPoint, requiresActiveSubscription.hostelIdParam());

        if (hostelId != null) {
            SubscriptionDto subscriptionDto = subscriptionService.getCurrentSubscriptionDetails(hostelId);
            if (subscriptionDto == null || !subscriptionDto.isValid()) {
                throw new SubscriptionExpiredException(Utils.SUBSCRIPTION_EXPIRED);
            }
        }
    }

    private String getHostelId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals(paramName)) {
                    return args[i] != null ? args[i].toString() : null;
                }
            }
        }

        // 2. Check inside request body objects (if hostelId not found in params)
        for (Object arg : args) {
            if (arg != null) {
                String value = getValueFromObject(arg, paramName);
                if (value != null) {
                    return value;
                }
            }
        }

        return null;
    }

    private String getValueFromObject(Object obj, String fieldName) {
        try {
            // Check if it's a record (using accessor method)
            if (obj.getClass().isRecord()) {
                Method method = obj.getClass().getMethod(fieldName);
                Object value = method.invoke(obj);
                return value != null ? value.toString() : null;
            }

            // Check fields for normal classes
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            // Field not found in this object, ignore
        }
        return null;
    }
}
