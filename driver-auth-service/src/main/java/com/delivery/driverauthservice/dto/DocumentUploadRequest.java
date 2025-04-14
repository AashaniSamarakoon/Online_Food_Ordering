package com.delivery.driverauthservice.dto;

import com.delivery.driverauthservice.model.DocumentType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentUploadRequest {
    private Long driverId;
    private DocumentType documentType;
    private String base64Image;
    private String fileName;
    private String contentType;
    private LocalDateTime expiryDate;
    private boolean verified;
}