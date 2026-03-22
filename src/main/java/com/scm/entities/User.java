package com.scm.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity(name = "user")
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Getter(AccessLevel.NONE)
    private String password;

    @Column(length = 1000)
    private String about;

    @Column(length = 1000)
    private String profilePic;

    private String phoneNumber;

    @Getter(value = AccessLevel.NONE)
    private boolean enabled = false;

    private boolean emailVerified = false;
    private boolean phoneVerified = false;

    // NEW: admin can block users
    private boolean blocked = false;

    // NEW: password reset token
    private String passwordResetToken;

    @Enumerated(value = EnumType.STRING)
    private Providers provider = Providers.SELF;

    private String providerUserId;
    private String emailToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roleList = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleList.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() { return this.email; }

    @Override
    public String getPassword() { return this.password; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return !this.blocked; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return this.enabled; }
}