package com.scm.entities;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

    @Id
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String picture;

    @Column(length = 1000)
    private String description;

    private boolean favorite = false;
    private String websiteLink;
    private String linkedInLink;
    private String cloudinaryImagePublicId;

    @ManyToOne
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<SocialLink> links = new ArrayList<>();
}