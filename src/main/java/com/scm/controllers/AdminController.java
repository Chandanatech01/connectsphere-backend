package com.scm.controllers;

import com.scm.entities.User;
import com.scm.repsitories.ContactRepo;
import com.scm.repsitories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    @Autowired private UserRepo userRepo;
    @Autowired private ContactRepo contactRepo;

    // ── Dashboard Stats ───────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalUsers = userRepo.count();
        long totalContacts = contactRepo.count();
        long verifiedUsers = userRepo.findAll().stream().filter(User::isEmailVerified).count();
        long blockedUsers = userRepo.findAll().stream().filter(User::isBlocked).count();

        return ResponseEntity.ok(Map.of(
            "totalUsers", totalUsers,
            "totalContacts", totalContacts,
            "verifiedUsers", verifiedUsers,
            "blockedUsers", blockedUsers
        ));
    }

    // ── Get All Users ─────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<Map<String, Object>> users = userRepo.findAll().stream()
            .map(u -> {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", u.getUserId());
                map.put("name", u.getName());
                map.put("email", u.getEmail());
                map.put("profilePic", u.getProfilePic());
                map.put("emailVerified", u.isEmailVerified());
                map.put("enabled", u.isEnabled());
                map.put("blocked", u.isBlocked());
                map.put("provider", u.getProvider());
                map.put("roleList", u.getRoleList());
                map.put("totalContacts", contactRepo.countByUser(u));
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    // ── Get Single User ───────────────────────────────────────────────
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        Map<String, Object> map = new HashMap<>();
        map.put("userId", user.getUserId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("profilePic", user.getProfilePic());
        map.put("phoneNumber", user.getPhoneNumber());
        map.put("about", user.getAbout());
        map.put("emailVerified", user.isEmailVerified());
        map.put("enabled", user.isEnabled());
        map.put("blocked", user.isBlocked());
        map.put("provider", user.getProvider());
        map.put("roleList", user.getRoleList());
        map.put("totalContacts", contactRepo.countByUser(user));

        return ResponseEntity.ok(map);
    }

    // ── Block User ────────────────────────────────────────────────────
    @PutMapping("/users/{userId}/block")
    public ResponseEntity<?> blockUser(@PathVariable String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (user.getRoleList().contains("ROLE_ADMIN"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Cannot block an admin user."));

        user.setBlocked(true);
        user.setEnabled(false);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", user.getName() + " has been blocked."));
    }

    // ── Unblock User ──────────────────────────────────────────────────
    @PutMapping("/users/{userId}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        user.setBlocked(false);
        user.setEnabled(true);
        userRepo.save(user);
        return ResponseEntity.ok(Map.of("message", user.getName() + " has been unblocked."));
    }

    // ── Delete User ───────────────────────────────────────────────────
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (user.getRoleList().contains("ROLE_ADMIN"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Cannot delete an admin user."));

        userRepo.delete(user);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully."));
    }
}