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
public class DocumentDTO {
    private Long id;
    private Long driverId;
    private DocumentType documentType;
    private String fileName;
    private String fileUrl;
    private String fileContentType;
    private Long fileSize;
    private boolean verified;
    private String verificationNotes;
    private LocalDateTime verifiedAt;
    private LocalDateTime uploadedAt;
    private LocalDateTime expiryDate;
}