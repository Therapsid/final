package com.example.backend.common.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folderName, String fileName) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folderName,
                        "public_id", fileName,
                        "overwrite", true,
                        "resource_type", "image"
                )
        );
        return (String) uploadResult.get("secure_url");
    }

    public String uploadFile(MultipartFile file, String folderName, String fileName) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folderName,
                        "public_id", fileName,
                        "overwrite", true,
                        "resource_type", "auto"
                )
        );
        return (String) uploadResult.get("secure_url");
    }
}
