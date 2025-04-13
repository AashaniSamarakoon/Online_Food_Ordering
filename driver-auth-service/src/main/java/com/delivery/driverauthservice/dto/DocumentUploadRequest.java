package com.delivery.driverauthservice.dto;

import com.delivery.driverauthservice.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    private Long driverId;
    private DocumentType documentType;
    private String base64Image;
    private String fileName;
    private String contentType;
    private LocalDateTime expiryDate;
}