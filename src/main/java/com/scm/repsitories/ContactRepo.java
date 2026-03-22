package com.scm.repsitories;

import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.scm.entities.Contact;
import com.scm.entities.User;

@Repository
public interface ContactRepo extends JpaRepository<Contact, String> {

    Page<Contact> findByUser(User user, Pageable pageable);

    @Query("SELECT c FROM Contact c WHERE c.user.userId = :userId")
    List<Contact> findByUserId(@Param("userId") String userId);

    List<Contact> findByUserAndFavoriteTrue(User user);

    long countByUser(User user);

    Page<Contact> findByUserAndNameContainingIgnoreCase(User user, String name, Pageable pageable);
    Page<Contact> findByUserAndEmailContainingIgnoreCase(User user, String email, Pageable pageable);
    Page<Contact> findByUserAndPhoneNumberContaining(User user, String phone, Pageable pageable);
}