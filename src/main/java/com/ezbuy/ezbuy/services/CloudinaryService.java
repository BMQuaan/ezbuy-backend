package com.ezbuy.ezbuy.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folderName) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folderName
            ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
    }

    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image", e);
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        Pattern pattern = Pattern.compile("/upload/(?:v\\d+/)?([^\\.]+)(\\.\\w+)?");
        Matcher matcher = pattern.matcher(imageUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}