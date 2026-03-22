package com.scm.controllers;

import com.scm.entities.Providers;
import com.scm.entities.User;
import com.scm.helpers.AppConstants;
import com.scm.repsitories.UserRepo;
import com.scm.security.JwtHelper;
import com.scm.services.EmailService;
import com.scm.services.impl.SecurityCustomUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private SecurityCustomUserDetailService userDetailsService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${server.baseUrl}")
    private String baseUrl;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("username");
        String password = body.get("password");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Account not verified. Please check your email."));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password."));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtHelper.generateToken(userDetails);
        User user = userRepo.findByEmail(email).orElseThrow();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", user.getUserId());
        userMap.put("name", user.getName());
        userMap.put("email", user.getEmail());
        userMap.put("profilePic", user.getProfilePic());
        userMap.put("phoneNumber", user.getPhoneNumber());
        userMap.put("about", user.getAbout());
        userMap.put("emailVerified", user.isEmailVerified());
        userMap.put("provider", user.getProvider());
        userMap.put("roleList", user.getRoleList());

        return ResponseEntity.ok(Map.of("token", token, "user", userMap));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        if (userRepo.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already registered."));
        }

        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setName(body.get("name"));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(body.get("password")));
        user.setPhoneNumber(body.get("phoneNumber"));
        user.setAbout(body.getOrDefault("about", ""));
        user.setRoleList(List.of(AppConstants.ROLE_USER));
        user.setEmailVerified(false);
        user.setEnabled(false);
        user.setProvider(Providers.SELF);

        String emailToken = UUID.randomUUID().toString();
        user.setEmailToken(emailToken);
        userRepo.save(user);

        String verifyLink = baseUrl + "/api/auth/verify-email?token=" + emailToken;
        try {
            emailService.sendEmail(email,
                "Verify your ConnectSphere account",
                "<h2>Welcome to ConnectSphere!</h2>" +
                "<p>Click below to verify your email:</p>" +
                "<a href='" + verifyLink + "' style='background:#2563eb;color:white;padding:12px 24px;border-radius:8px;text-decoration:none;'>Verify Email</a>");
        } catch (Exception e) {
            // Email failed but user saved
        }

        return ResponseEntity.ok(Map.of("message",
                "Registration successful! Please check your email to verify your account."));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<User> userOpt = userRepo.findByEmailToken(token);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid or expired token."));
        }

        User user = userOpt.get();
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setEmailToken(null);
        userRepo.save(user);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "http://localhost:5173/login?verified=true")
                .build();
    }
}