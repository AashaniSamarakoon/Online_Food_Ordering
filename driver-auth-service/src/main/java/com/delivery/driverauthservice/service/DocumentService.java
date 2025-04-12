package com.delivery.driverauthservice.service;

import com.delivery.driverauthservice.dto.DocumentDTO;
import com.delivery.driverauthservice.dto.DocumentUploadRequest;
import com.delivery.driverauthservice.model.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentDTO uploadDocument(Long driverId, DocumentType documentType, MultipartFile file);
    DocumentDTO uploadDocumentBase64(DocumentUploadRequest request);
    List<DocumentDTO> getDriverDocuments(Long driverId);
    DocumentDTO getLatestDocument(Long driverId, DocumentType documentType);
    boolean verifyDocument(Long documentId, boolean verified, String notes);
    boolean deleteDocument(Long documentId);
}