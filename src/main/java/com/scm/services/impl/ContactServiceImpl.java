package com.scm.services.impl;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.helpers.ResourceNotFoundException;
import com.scm.repsitories.ContactRepo;
import com.scm.services.ContactService;
import com.scm.services.ImageService;

@Service
public class ContactServiceImpl implements ContactService {

    private final Logger logger = LoggerFactory.getLogger(ContactServiceImpl.class);

    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private ImageService imageService;

    @Override
    public Contact save(Contact contact) {
        contact.setId(UUID.randomUUID().toString());
        return contactRepo.save(contact);
    }

    @Override
    public Contact update(Contact contact) {
        Contact existing = contactRepo.findById(contact.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + contact.getId()));
        existing.setName(contact.getName());
        existing.setEmail(contact.getEmail());
        existing.setPhoneNumber(contact.getPhoneNumber());
        existing.setAddress(contact.getAddress());
        existing.setDescription(contact.getDescription());
        existing.setWebsiteLink(contact.getWebsiteLink());
        existing.setLinkedInLink(contact.getLinkedInLink());
        existing.setFavorite(contact.isFavorite());
        existing.setPicture(contact.getPicture());
        existing.setCloudinaryImagePublicId(contact.getCloudinaryImagePublicId());
        return contactRepo.save(existing);
    }

    @Override
    public void delete(String id) {
        Contact contact = contactRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        contactRepo.delete(contact);
    }

    @Override
    public Contact getById(String id) {
        return contactRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    }

    @Override
    public Page<Contact> getByUser(User user, int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUser(user, pageable);
    }

    @Override
    public Page<Contact> searchByName(String name, int page, int size, String sortBy, String direction, User user) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndNameContainingIgnoreCase(user, name, pageable);
    }

    @Override
    public Page<Contact> searchByEmail(String email, int page, int size, String sortBy, String direction, User user) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndEmailContainingIgnoreCase(user, email, pageable);
    }

    @Override
    public Page<Contact> searchByPhoneNumber(String phone, int page, int size, String sortBy, String direction, User user) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return contactRepo.findByUserAndPhoneNumberContaining(user, phone, pageable);
    }

    @Override
    public List<Contact> getFavoritesByUser(User user) {
        return contactRepo.findByUserAndFavoriteTrue(user);
    }

    @Override
    public long countByUser(User user) {
        return contactRepo.countByUser(user);
    }
}