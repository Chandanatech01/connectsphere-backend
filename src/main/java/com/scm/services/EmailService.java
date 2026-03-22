package com.scm.services;

public interface EmailService {

    // Send plain text email
    void sendEmail(String to, String subject, String body);

    // Send HTML formatted email
    void sendEmailWithHtml(String to, String subject, String htmlBody);

    // Send email with attachment
    void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath);

    // Send email verification link
    void sendVerificationEmail(String to, String verificationLink);

    // Send welcome email after registration
    void sendWelcomeEmail(String to, String name);
}