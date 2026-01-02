package com.grievance.grievance_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file, Long grievanceId);

    byte[] loadFile(String filePath);

    void deleteFile(String filePath);
}