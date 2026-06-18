package com.icet.carrental.util;

import com.icet.carrental.exception.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ImageFileValidator {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png",  "png",
            "image/webp", "webp"
    );

    private ImageFileValidator() {
    }

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidFileException("File size must not exceed 5 MB");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!EXTENSIONS.containsKey(contentType)) {
            throw new InvalidFileException("Only JPEG, PNG, and WebP images are allowed");
        }
    }

    public static String resolveExtension(MultipartFile file) {
        String contentType = normalizeContentType(file.getContentType());
        return EXTENSIONS.getOrDefault(contentType, "bin");
    }

    public static String generateObjectName(String prefix, String extension) {
        return prefix + "/" + UUID.randomUUID() + "." + extension;
    }

    public static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new InvalidFileException("Failed to read uploaded file");
        }
    }

    public static String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.toLowerCase(Locale.ROOT).split(";")[0].trim();
    }

    public static Set<String> allowedContentTypes() {
        return EXTENSIONS.keySet();
    }
}
