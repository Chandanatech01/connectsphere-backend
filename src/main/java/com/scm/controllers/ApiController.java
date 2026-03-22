package com.scm.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.scm.entities.Contact;
import com.scm.helpers.Helper;
import com.scm.services.ContactService;
import com.scm.services.UserService;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private UserService userService;

    @GetMapping("/contacts/{contactId}")
    public ResponseEntity<?> getContact(
            @PathVariable String contactId,
            Authentication authentication) {

        // Security: only return contact if it belongs to logged-in user
        var user = userService.getUserByEmail(Helper.getEmailOfLoggedInUser(authentication));
        Contact contact = contactService.getById(contactId);

        if (contact == null) {
            return ResponseEntity.notFound().build();
        }

        if (!contact.getUser().getUserId().equals(user.getUserId())) {
            return ResponseEntity.status(403).body("Access denied.");
        }

        return ResponseEntity.ok(contact);
    }
}