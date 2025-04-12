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
    private final StorageService storageService;

    @Override
    @Transactional
    public DocumentDTO uploadDocument(Long driverId, DocumentType documentType, MultipartFile file) {
        try {
            DriverCredential driver = driverRepository.findById(driverId)
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            String fileUrl = storageService.uploadFile(file, "driver-" + driverId + "/" + documentType.name().toLowerCase());

            DriverDocument document = DriverDocument.builder()
                    .driver(driver)
                    .documentType(documentType)
                    .fileName(file.getOriginalFilename())
                    .fileUrl(fileUrl)
                    .fileContentType(file.getContentType())
                    .fileSize(file.getSize())
                    .verified(false)
                    .build();

            document = documentRepository.save(document);

            return mapToDTO(document);
        } catch (IOException e) {
            log.error("Error uploading document", e);
            throw new RuntimeException("Failed to upload document: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public DocumentDTO uploadDocumentBase64(DocumentUploadRequest request) {
        try {
            DriverCredential driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new RuntimeException("Driver not found"));

            // Decode base64 string to byte[]
            byte[] imageBytes = Base64.getDecoder().decode(request.getBase64Image());

            String fileUrl = storageService.uploadBytes(
                    imageBytes,
                    request.getFileName(),
                    request.getContentType(),
                    "driver-" + request.getDriverId() + "/" + request.getDocumentType().name().toLowerCase()
            );

            DriverDocument document = DriverDocument.builder()
                    .driver(driver)
                    .documentType(request.getDocumentType())
                    .fileName(request.getFileName())
                    .fileUrl(fileUrl)
                    .fileContentType(request.getContentType())
                    .fileSize((long) imageBytes.length)
                    .verified(false)
                    .expiryDate(request.getExpiryDate())
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

    private DocumentDTO mapToDTO(DriverDocument document) {
        return DocumentDTO.builder()
                .id(document.getId())
                .driverId(document.getDriver().getDriverId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .fileUrl(document.getFileUrl())
                .verified(document.isVerified())
                .uploadedAt(document.getUploadedAt())
                .expiryDate(document.getExpiryDate())
                .build();
    }
}