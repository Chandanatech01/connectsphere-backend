package com.scm.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class Helper {

    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    @Value("${server.baseUrl}")
    private String baseUrl;

    public static String getEmailOfLoggedInUser(Authentication authentication) {

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {

            String clientId = oauthToken.getAuthorizedClientRegistrationId();
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

            if (clientId.equalsIgnoreCase("google")) {
                logger.debug("Fetching email from Google OAuth2");
                return oauth2User.getAttribute("email").toString();

            } else if (clientId.equalsIgnoreCase("github")) {
                logger.debug("Fetching email from GitHub OAuth2");
                return oauth2User.getAttribute("email") != null
                        ? oauth2User.getAttribute("email").toString()
                        : oauth2User.getAttribute("login").toString() + "@github.com";
            }

            return "";

        } else {
            logger.debug("Fetching email from local database");
            return authentication.getName();
        }
    }

    public String getLinkForEmailVerification(String emailToken) {
        return this.baseUrl + "/auth/verify-email?token=" + emailToken;
    }
}