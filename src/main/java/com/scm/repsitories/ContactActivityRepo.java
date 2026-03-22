package com.scm.repsitories;

import com.scm.entities.ContactActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContactActivityRepo extends JpaRepository<ContactActivity, String> {
    List<ContactActivity> findByUserIdOrderByCreatedAtDesc(String userId);
    List<ContactActivity> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
}