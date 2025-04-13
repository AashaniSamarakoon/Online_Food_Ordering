package com.delivery.driverauthservice.service.impl;

import com.delivery.driverauthservice.dto.DocumentDTO;
import com.delivery.driverauthservice.dto.DocumentUploadRequest;
import com.delivery.driverauthservice.model.DocumentType;
import com.delivery.driverauthservice.model.DriverCredential;
import com.delivery.driverauthservice.model.DriverDocument;
import com.delivery.driverauthservice.repository.DriverCredentialRepository;
import com.delivery.driverauthservice.repository.DriverDocumentRepository;
import com.delivery.driverauthservice.service.DocumentService;
import com.delivery.driverauthservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DriverDocumentRepository documentRepository;
    private final DriverCredentialRepository driverRepository;
    private final StorageService storageService;  // Restored StorageService

//    @Override
//    @Transactional
//    public DocumentDTO uploadDocument(Long driverId, DocumentType documentType, MultipartFile file) {
//        try {
//            DriverCredential driver = driverRepository.findById(driverId)
//                    .orElseThrow(() -> new RuntimeException("Driver not found"));
//
//            String fileUrl = storageService.uploadFile(file, "driver-" + driverId + "/" + documentType.name().toLowerCase());
//
//            DriverDocument document = DriverDocument.builder()
//                    .driver(driver)
//                    .documentType(documentType)
//                    .fileName(file.getOriginalFilename())
//                    .fileUrl(fileUrl)
//                    .fileContentType(file.getContentType())
//                    .fileSize(file.getSize())
//                    .verified(false)
//                    .build();
//
//            document = documentRepository.save(document);
//
//            return mapToDTO(document);
//        } catch (IOException e) {
//            log.error("Error uploading document", e);
//            throw new RuntimeException("Failed to upload document: " + e.getMessage());
//        }
//    }

    @Override
    @Transactional
    public DocumentDTO uploadDocumentBase64(DocumentUploadRequest request) {
        try {
            // Process the base64 string to handle data URL format
            String base64Data = request.getBase64Image();
            String contentType = request.getContentType();

            // Handle data URL format (e.g., "data:image/jpeg;base64,...")
            if (base64Data.contains(",")) {
                String[] parts = base64Data.split(",");
                if (parts.length > 1) {
                    // If there's content type info in the data URL, extract it
                    if (parts[0].contains("data:") && parts[0].contains(";base64")) {
                        String dataTypePart = parts[0].substring(5, parts[0].indexOf(";base64"));
                        if (dataTypePart.length() > 0) {
                            contentType = dataTypePart; // Use content type from data URL if available
                        }
                    }
                    base64Data = parts[1];
                }
            }

            DriverCredential driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new RuntimeException("Driver not found with ID: " + request.getDriverId()));

            // Decode base64 string to byte[]
            byte[] imageBytes;
            try {
                imageBytes = Base64.getDecoder().decode(base64Data);
            } catch (IllegalArgumentException e) {
                log.error("Invalid Base64 string: {}", e.getMessage());
                throw new RuntimeException("Invalid Base64 data: " + e.getMessage());
            }

            String fileUrl = storageService.uploadBytes(
                    imageBytes,
                    request.getFileName(),
                    contentType,
                    "driver-" + request.getDriverId() + "/" + request.getDocumentType().name().toLowerCase()
            );

            DriverDocument document = DriverDocument.builder()
                    .driver(driver)
                    .documentType(request.getDocumentType())
                    .fileName(request.getFileName())
                    .fileUrl(fileUrl)
                    .fileContentType(contentType)
                    .fileSize((long) imageBytes.length)
                    .verified(false)
                    .expiryDate(request.getExpiryDate())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            document = documentRepository.save(document);

            return mapToDTO(document);
        } catch (IOException e) {
            log.error("Error uploading document", e);
            throw new RuntimeException("Failed to upload document: " + e.getMessage());
        }
    }

    @Override
    public List<DocumentDTO> getDriverDocuments(Long driverId) {
        return documentRepository.findByDriverDriverId(driverId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentDTO getLatestDocument(Long driverId, DocumentType documentType) {
        return documentRepository.findTopByDriverDriverIdAndDocumentTypeOrderByUploadedAtDesc(driverId, documentType)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean verifyDocument(Long documentId, boolean verified, String notes) {
        return documentRepository.findById(documentId)
                .map(document -> {
                    document.setVerified(verified);
                    document.setVerificationNotes(notes);
                    document.setVerifiedAt(LocalDateTime.now());
                    documentRepository.save(document);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean deleteDocument(Long documentId) {
        return documentRepository.findById(documentId)
                .map(document -> {
                    try {
                        storageService.deleteFile(document.getFileUrl());
                        documentRepository.delete(document);
                        return true;
                    } catch (IOException e) {
                        log.error("Error deleting document file", e);
                        return false;
                    }
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public DocumentDTO getDocument(Long documentId) {
        DriverDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        return mapToDTO(document);
    }

    private DocumentDTO mapToDTO(DriverDocument document) {
        return DocumentDTO.builder()
                .id(document.getId())
                .driverId(document.getDriver().getDriverId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .fileContentType(document.getFileContentType())
                .fileSize(document.getFileSize())
                .verified(document.isVerified())
                .verificationNotes(document.getVerificationNotes())
                .verifiedAt(document.getVerifiedAt())
                .uploadedAt(document.getUploadedAt())
                .expiryDate(document.getExpiryDate())
                .build();
    }
}