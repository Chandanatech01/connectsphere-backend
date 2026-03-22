package com.scm.services;

import java.util.List;
import java.util.Optional;
import com.scm.entities.User;

public interface UserService {

    // Save new user
    User saveUser(User user);

    // Get user by ID
    Optional<User> getUserById(String id);

    // Update user details
    User updateUser(User user);

    // Delete user
    void deleteUser(String id);

    // Check if user exists by ID
    boolean isUserExist(String userId);

    // Check if user exists by email
    boolean isUserExistByEmail(String email);

    // Get all users
    List<User> getAllUsers();

    // Get user by email
    User getUserByEmail(String email);

    // Update profile picture
    User updateProfilePicture(String userId, String profilePicUrl);

    // Verify email token
    boolean verifyEmailToken(String token);
}