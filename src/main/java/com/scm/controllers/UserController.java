package com.scm.controllers;

import com.scm.entities.ContactActivity;
import com.scm.entities.User;
import com.scm.repsitories.ContactActivityRepo;
import com.scm.repsitories.ContactRepo;
import com.scm.repsitories.UserRepo;
import com.scm.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserRepo userRepo;
    @Autowired private ContactRepo contactRepo;
    @Autowired private ContactActivityRepo activityRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;

    // ── Get Profile ───────────────────────────────────────────────────
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(buildUserMap(user));
    }

    // ── Update Profile ────────────────────────────────────────────────
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> body, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        if (body.containsKey("name") && !body.get("name").isBlank()) user.setName(body.get("name"));
        if (body.containsKey("phoneNumber")) user.setPhoneNumber(body.get("phoneNumber"));
        if (body.containsKey("about")) user.setAbout(body.get("about"));
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully.", "user", buildUserMap(user)));
    }

    // ── Change Password ───────────────────────────────────────────────
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Authentication auth) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        if (currentPassword == null || newPassword == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Both fields are required."));
        if (newPassword.length() < 8)
            return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 8 characters."));
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Current password is incorrect."));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully."));
    }

    // ── Forgot Password ───────────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));

        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty())
            return ResponseEntity.ok(Map.of("message", "If that email exists, a reset link has been sent."));

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        userRepo.save(user);

        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto'>"
                + "<div style='background:linear-gradient(135deg,#1e3a8a,#2563eb);padding:24px;border-radius:12px 12px 0 0'>"
                + "<h2 style='color:white;margin:0'>Reset Your Password</h2></div>"
                + "<div style='background:#f8fafc;padding:24px;border-radius:0 0 12px 12px;border:1px solid #e2e8f0'>"
                + "<p style='color:#374151'>Hi " + user.getName() + ",</p>"
                + "<p style='color:#374151'>Click below to reset your password:</p>"
                + "<a href='" + resetLink + "' style='display:inline-block;background:#2563eb;color:white;padding:12px 28px;border-radius:8px;text-decoration:none;font-weight:600;margin:16px 0'>Reset Password</a>"
                + "<p style='color:#9ca3af;font-size:12px'>If you didn't request this, ignore this email.</p>"
                + "</div></div>";
        try { emailService.sendEmail(email, "Reset your ConnectSphere password", html); } catch (Exception ignored) {}

        return ResponseEntity.ok(Map.of("message", "If that email exists, a reset link has been sent."));
    }

    // ── Reset Password ────────────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        if (token == null || newPassword == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Token and password required."));
        if (newPassword.length() < 8)
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 8 characters."));

        Optional<User> userOpt = userRepo.findByPasswordResetToken(token);
        if (userOpt.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired reset token."));

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully! You can now login."));
    }

    // ── Activity Feed ─────────────────────────────────────────────────
    @GetMapping("/activity")
    public ResponseEntity<?> getActivity(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        List<ContactActivity> activities = activityRepo.findTop10ByUserIdOrderByCreatedAtDesc(user.getUserId());
        return ResponseEntity.ok(activities);
    }

    // ── Stats ─────────────────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(Authentication auth) {
        User user = userRepo.findByEmail(auth.getName()).orElseThrow();
        long totalContacts = contactRepo.countByUser(user);
        long favorites = contactRepo.findByUserAndFavoriteTrue(user).size();
        return ResponseEntity.ok(Map.of(
            "totalContacts", totalContacts,
            "favorites", favorites,
            "emailVerified", user.isEmailVerified(),
            "provider", user.getProvider()
        ));
    }

    private Map<String, Object> buildUserMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("profilePic", user.getProfilePic());
        map.put("phoneNumber", user.getPhoneNumber());
        map.put("about", user.getAbout());
        map.put("emailVerified", user.isEmailVerified());
        map.put("phoneVerified", user.isPhoneVerified());
        map.put("provider", user.getProvider());
        map.put("roleList", user.getRoleList());
        return map;
    }
}