package com.scm;

import com.scm.entities.User;
import com.scm.helpers.AppConstants;
import com.scm.repsitories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        userRepo.findByEmail("admin@gmail.com").ifPresentOrElse(
                user -> {},
                () -> {
                    User admin = new User();
                    admin.setUserId(UUID.randomUUID().toString());
                    admin.setName("Admin");
                    admin.setEmail("admin@gmail.com");
                    admin.setPassword(passwordEncoder.encode("Admin@1234"));
                    admin.setRoleList(List.of(AppConstants.ROLE_ADMIN, AppConstants.ROLE_USER));
                    admin.setEmailVerified(true);
                    admin.setEnabled(true);
                    admin.setPhoneVerified(true);
                    admin.setAbout("Default admin account");
                    userRepo.save(admin);
                    System.out.println("Admin user created successfully.");
                });
    }
}