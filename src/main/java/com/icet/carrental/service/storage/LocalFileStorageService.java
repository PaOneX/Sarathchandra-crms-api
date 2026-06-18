package com.icet.carrental.service.storage;

import com.icet.carrental.exception.StorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalFileStorageService implements StorageService {

    private static final String PUBLIC_PREFIX = "/api/uploads/";

    private final Path uploadRoot = Paths.get("uploads");

    @Override
    public String upload(String bucket, String path, byte[] content, String contentType) {
        try {
            Path target = uploadRoot.resolve(bucket).resolve(path);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
            return buildPublicUrl(bucket, path);
        } catch (IOException ex) {
            throw new StorageException("Failed to save file locally: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void delete(String bucket, String path) {
        try {
            Files.deleteIfExists(uploadRoot.resolve(bucket).resolve(path));
        } catch (IOException ex) {
            throw new StorageException("Failed to delete local file: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String buildPublicUrl(String bucket, String path) {
        return PUBLIC_PREFIX + bucket + "/" + path.replace("\\", "/");
    }
}
