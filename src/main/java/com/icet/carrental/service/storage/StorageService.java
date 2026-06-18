package com.icet.carrental.service.storage;

public interface StorageService {

    String upload(String bucket, String path, byte[] content, String contentType);

    void delete(String bucket, String path);

    String buildPublicUrl(String bucket, String path);
}
