package com.delivery.driverauthservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadMetadata {
    private String base64Image;
    private String fileName;
    private String contentType;
    private LocalDateTime expiryDate;
}