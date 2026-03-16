package com.smartstay.smartstay.config;

import io.jsonwebtoken.Claims;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;

@Configuration
public class Authentication {

    public boolean isAuthenticated() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.isAuthenticated();
    }


    public String getName() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    public Claims getDetails() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            return (Claims) authentication.getDetails();
        }

        return null;
    }

    public String getSource() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            authentication.getAuthorities();

            Claims claims = (Claims) authentication.getDetails();
            return claims.get("source", String.class);
        }


        return null;
    }
}
