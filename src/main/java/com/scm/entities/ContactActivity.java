package com.scm.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactActivity {

    @Id
    private String id;

    private String userId;
    private String contactId;
    private String contactName;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    private LocalDateTime createdAt;

    public enum ActivityType {
        ADDED, UPDATED, DELETED, EMAILED
    }
}