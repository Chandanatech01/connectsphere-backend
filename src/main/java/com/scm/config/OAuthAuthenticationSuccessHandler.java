package com.scm.config;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.scm.entities.Providers;
import com.scm.entities.User;
import com.scm.helpers.AppConstants;
import com.scm.repsitories.UserRepo;
import com.scm.security.JwtHelper;
import com.scm.services.impl.SecurityCustomUserDetailService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuthAuthenticationSuccessHandler.class);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private SecurityCustomUserDetailService userDetailsService;

    // Reads from application.properties
    // Locally = http://localhost:5173
    // On Vercel = https://your-app.vercel.app
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        logger.info("OAuth2 Authentication Success");

        var oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId();
        var oauthUser = (DefaultOAuth2User) authentication.getPrincipal();

        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setRoleList(List.of(AppConstants.ROLE_USER));
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setPassword("oauth_dummy_" + UUID.randomUUID());

        if (provider.equalsIgnoreCase("google")) {
            user.setEmail(oauthUser.getAttribute("email").toString());
            user.setProfilePic(oauthUser.getAttribute("picture").toString());
            user.setName(oauthUser.getAttribute("name").toString());
            user.setProviderUserId(oauthUser.getName());
            user.setProvider(Providers.GOOGLE);
            user.setAbout("This account was created using Google.");

        } else if (provider.equalsIgnoreCase("github")) {
            String email = oauthUser.getAttribute("email") != null
                    ? oauthUser.getAttribute("email").toString()
                    : oauthUser.getAttribute("login").toString() + "@github.com";
            user.setEmail(email);
            String avatar = oauthUser.getAttribute("avatar_url") != null
                    ? oauthUser.getAttribute("avatar_url").toString() : "";
            user.setProfilePic(avatar);
            user.setName(oauthUser.getAttribute("login").toString());
            user.setProviderUserId(oauthUser.getName());
            user.setProvider(Providers.GITHUB);
            user.setAbout("This account was created using GitHub.");
        }

        // Save or update user in DB
        userRepo.findByEmail(user.getEmail()).ifPresentOrElse(
                existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setProfilePic(user.getProfilePic());
                    userRepo.save(existingUser);
                    logger.info("Existing OAuth user updated: {}", existingUser.getEmail());
                },
                () -> {
                    userRepo.save(user);
                    logger.info("New OAuth user saved: {}", user.getEmail());
                });

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtHelper.generateToken(userDetails);

        // Get full saved user
        User savedUser = userRepo.findByEmail(user.getEmail()).orElseThrow();

        // Encode URL params
        String userName = java.net.URLEncoder.encode(savedUser.getName(), "UTF-8");
        String userEmail = java.net.URLEncoder.encode(savedUser.getEmail(), "UTF-8");
        String profilePic = savedUser.getProfilePic() != null
                ? java.net.URLEncoder.encode(savedUser.getProfilePic(), "UTF-8") : "";
        String providerStr = savedUser.getProvider().toString();
        String userId = savedUser.getUserId();

        // Redirect to React frontend — works both locally and after deployment!
        String redirectUrl = frontendUrl + "/#/oauth-success"
                + "?token=" + token
                + "&userId=" + userId
                + "&name=" + userName
                + "&email=" + userEmail
                + "&profilePic=" + profilePic
                + "&provider=" + providerStr
                + "&emailVerified=true";

        new DefaultRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}