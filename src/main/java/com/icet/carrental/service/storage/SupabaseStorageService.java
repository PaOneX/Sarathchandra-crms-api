package com.icet.carrental.service.storage;

import com.icet.carrental.config.SupabaseProperties;
import com.icet.carrental.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@RequiredArgsConstructor
public class SupabaseStorageService implements StorageService {

    private final SupabaseProperties properties;
    private final RestClient       restClient;

    @Override
    public String upload(String bucket, String path, byte[] content, String contentType) {
        String baseUrl = normalizeUrl(properties.getUrl());
        String url     = baseUrl + "/storage/v1/object/" + bucket + "/" + path;

        try {
            restClient.post()
                    .uri(url)
                    .header("apikey", properties.getServiceRoleKey())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getServiceRoleKey())
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header("x-upsert", "true")
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new StorageException("Failed to upload file to storage: " + ex.getMessage(), ex);
        }

        return buildPublicUrl(bucket, path);
    }

    @Override
    public void delete(String bucket, String path) {
        String baseUrl = normalizeUrl(properties.getUrl());
        String url     = baseUrl + "/storage/v1/object/" + bucket + "/" + path;

        try {
            restClient.delete()
                    .uri(url)
                    .header("apikey", properties.getServiceRoleKey())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getServiceRoleKey())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw new StorageException("Failed to delete file from storage: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String buildPublicUrl(String bucket, String path) {
        return normalizeUrl(properties.getUrl())
                + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
