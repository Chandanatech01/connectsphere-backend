package com.scm.services;

import java.util.List;
import org.springframework.data.domain.Page;
import com.scm.entities.Contact;
import com.scm.entities.User;

public interface ContactService {

    // Save new contact
    Contact save(Contact contact);

    // Update existing contact
    Contact update(Contact contact);

    // Get contact by ID
    Contact getById(String id);

    // Delete contact by ID
    void delete(String id);

    // Get all contacts by user (paginated) - primary method
    Page<Contact> getByUser(User user, int page, int size, String sortField, String sortDirection);

    // Get favorite contacts by user
    List<Contact> getFavoritesByUser(User user);

    // Count total contacts by user
    long countByUser(User user);

    // Search methods
    Page<Contact> searchByName(String nameKeyword, int size, int page, String sortBy, String order, User user);
    Page<Contact> searchByEmail(String emailKeyword, int size, int page, String sortBy, String order, User user);
    Page<Contact> searchByPhoneNumber(String phoneNumberKeyword, int size, int page, String sortBy, String order, User user);
}