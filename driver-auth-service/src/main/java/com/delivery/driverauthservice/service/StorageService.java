package com.delivery.driverauthservice.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    String uploadFile(MultipartFile file, String path) throws IOException;
    String uploadBytes(byte[] data, String filename, String contentType, String path) throws IOException;
    byte[] downloadFile(String fileUrl) throws IOException;
    void deleteFile(String fileUrl) throws IOException;
}