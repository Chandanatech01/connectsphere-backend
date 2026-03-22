package com.scm.forms;

import org.springframework.web.multipart.MultipartFile;
import com.scm.validators.ValidFile;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactForm {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address (e.g. example@gmail.com)")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 200, message = "Address must be under 200 characters")
    private String address;

    @Size(max = 1000, message = "Description must be under 1000 characters")
    private String description;

    private boolean favorite;

    @Pattern(
        regexp = "^$|^(https?://).+",
        message = "Website link must be a valid URL starting with http:// or https://"
    )
    private String websiteLink;

    @Pattern(
        regexp = "^$|^(https?://(www\\.)?linkedin\\.com/).+",
        message = "Please enter a valid LinkedIn URL"
    )
    private String linkedInLink;

    @ValidFile(message = "Invalid file. Please upload a valid image.")
    private MultipartFile contactImage;

    private String picture;
}