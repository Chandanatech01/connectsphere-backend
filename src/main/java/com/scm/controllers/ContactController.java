package com.scm.controllers;

import com.scm.entities.Contact;
import com.scm.entities.ContactActivity;
import com.scm.entities.ContactActivity.ActivityType;
import com.scm.entities.User;
import com.scm.repsitories.ContactActivityRepo;
import com.scm.repsitories.ContactRepo;
import com.scm.repsitories.UserRepo;
import com.scm.services.EmailService;
import com.scm.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired private ContactRepo contactRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private ImageService imageService;
    @Autowired private EmailService emailService;
    @Autowired private ContactActivityRepo activityRepo;

    private User getUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName()).orElseThrow();
    }

    private void logActivity(String userId, String contactId, String contactName, ActivityType type) {
        ContactActivity activity = ContactActivity.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .contactId(contactId)
                .contactName(contactName)
                .activityType(type)
                .createdAt(LocalDateTime.now())
                .build();
        activityRepo.save(activity);
    }

    // ── Get all contacts ──────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getContacts(Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = getUser(auth);
        Page<Contact> contacts = contactRepo.findByUser(user, PageRequest.of(page, size, Sort.by("name").ascending()));
        return ResponseEntity.ok(contacts);
    }

    // ── Get single contact ────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getContact(@PathVariable String id, Authentication auth) {
        User user = getUser(auth);
        Contact contact = contactRepo.findById(id).orElse(null);
        if (contact == null) return ResponseEntity.notFound().build();
        if (!contact.getUser().getUserId().equals(user.getUserId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(contact);
    }

    // ── Create contact ────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createContact(Authentication auth,
            @RequestParam String name, @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String websiteLink,
            @RequestParam(required = false) String linkedInLink,
            @RequestParam(defaultValue = "false") boolean favorite,
            @RequestParam(required = false) MultipartFile contactImage) {

        User user = getUser(auth);
        Contact contact = new Contact();
        contact.setId(UUID.randomUUID().toString());
        contact.setName(name); contact.setEmail(email);
        contact.setPhoneNumber(phoneNumber); contact.setAddress(address);
        contact.setDescription(description); contact.setWebsiteLink(websiteLink);
        contact.setLinkedInLink(linkedInLink); contact.setFavorite(favorite);
        contact.setUser(user);

        if (contactImage != null && !contactImage.isEmpty()) {
            contact.setPicture(imageService.uploadImage(contactImage, UUID.randomUUID().toString()));
        } else {
            contact.setPicture("https://ui-avatars.com/api/?name=" + name + "&background=e0e7ff&color=4f46e5");
        }

        contactRepo.save(contact);
        logActivity(user.getUserId(), contact.getId(), name, ActivityType.ADDED);
        return ResponseEntity.status(HttpStatus.CREATED).body(contact);
    }

    // ── Update contact ────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(@PathVariable String id, Authentication auth,
            @RequestParam String name, @RequestParam String email,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String websiteLink,
            @RequestParam(required = false) String linkedInLink,
            @RequestParam(defaultValue = "false") boolean favorite,
            @RequestParam(required = false) MultipartFile contactImage) {

        User user = getUser(auth);
        Contact contact = contactRepo.findById(id).orElse(null);
        if (contact == null) return ResponseEntity.notFound().build();
        if (!contact.getUser().getUserId().equals(user.getUserId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        contact.setName(name); contact.setEmail(email);
        contact.setPhoneNumber(phoneNumber); contact.setAddress(address);
        contact.setDescription(description); contact.setWebsiteLink(websiteLink);
        contact.setLinkedInLink(linkedInLink); contact.setFavorite(favorite);

        if (contactImage != null && !contactImage.isEmpty())
            contact.setPicture(imageService.uploadImage(contactImage, UUID.randomUUID().toString()));

        contactRepo.save(contact);
        logActivity(user.getUserId(), contact.getId(), name, ActivityType.UPDATED);
        return ResponseEntity.ok(contact);
    }

    // ── Delete contact ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable String id, Authentication auth) {
        User user = getUser(auth);
        Contact contact = contactRepo.findById(id).orElse(null);
        if (contact == null) return ResponseEntity.notFound().build();
        if (!contact.getUser().getUserId().equals(user.getUserId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        String name = contact.getName();
        contactRepo.delete(contact);
        logActivity(user.getUserId(), id, name, ActivityType.DELETED);
        return ResponseEntity.ok(Map.of("message", "Contact deleted successfully."));
    }

    // ── Search contacts ───────────────────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<?> searchContacts(Authentication auth,
            @RequestParam String field, @RequestParam String value,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = getUser(auth);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Contact> contacts = switch (field.toLowerCase()) {
            case "email" -> contactRepo.findByUserAndEmailContainingIgnoreCase(user, value, pageable);
            case "phone" -> contactRepo.findByUserAndPhoneNumberContaining(user, value, pageable);
            default -> contactRepo.findByUserAndNameContainingIgnoreCase(user, value, pageable);
        };
        return ResponseEntity.ok(contacts);
    }

    // ── Send Email to Contact ─────────────────────────────────────────
    @PostMapping("/{id}/send-email")
    public ResponseEntity<?> sendEmailToContact(@PathVariable String id,
            @RequestBody Map<String, String> body, Authentication auth) {

        User user = getUser(auth);
        Contact contact = contactRepo.findById(id).orElse(null);
        if (contact == null) return ResponseEntity.notFound().build();
        if (!contact.getUser().getUserId().equals(user.getUserId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        String subject = body.get("subject");
        String message = body.get("message");
        if (subject == null || message == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Subject and message required."));

        String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto'>"
                + "<div style='background:linear-gradient(135deg,#1e3a8a,#2563eb);padding:24px;border-radius:12px 12px 0 0'>"
                + "<h2 style='color:white;margin:0'>Message from " + user.getName() + "</h2></div>"
                + "<div style='background:#f8fafc;padding:24px;border-radius:0 0 12px 12px;border:1px solid #e2e8f0'>"
                + "<p style='color:#374151;line-height:1.6'>" + message.replace("\n", "<br/>") + "</p>"
                + "<hr style='border:none;border-top:1px solid #e2e8f0;margin:20px 0'/>"
                + "<p style='color:#9ca3af;font-size:12px'>Sent via ConnectSphere by "
                + user.getName() + " (" + user.getEmail() + ")</p></div></div>";

        try {
            emailService.sendEmail(contact.getEmail(), subject, html);
            logActivity(user.getUserId(), contact.getId(), contact.getName(), ActivityType.EMAILED);
            return ResponseEntity.ok(Map.of("message", "Email sent to " + contact.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to send email."));
        }
    }
}