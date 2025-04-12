package com.delivery.driverauthservice.dto;

import com.delivery.driverauthservice.model.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDTO {
    private Long id;
    private Long driverId;
    private DocumentType documentType;
    private String fileName;
    private String fileUrl;
    private boolean verified;
    private LocalDateTime uploadedAt;
    private LocalDateTime expiryDate;
}