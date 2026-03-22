package com.scm.services;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    // Upload image and return URL
    String uploadImage(MultipartFile image, String filename);

    // Get image URL from Cloudinary public ID
    String getUrlFromPublicId(String publicId);

    // Delete image from Cloudinary by public ID
    void deleteImage(String publicId);
}