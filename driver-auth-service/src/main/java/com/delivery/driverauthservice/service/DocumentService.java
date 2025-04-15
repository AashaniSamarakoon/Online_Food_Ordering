package com.delivery.driverauthservice.service;

import com.delivery.driverauthservice.dto.DocumentDTO;
import com.delivery.driverauthservice.dto.DocumentUploadRequest;
import com.delivery.driverauthservice.model.DocumentType;
import com.delivery.driverauthservice.model.DriverDocument;

import java.util.List;

public interface DocumentService {
    DocumentDTO uploadDocumentBase64(DocumentUploadRequest request);
    List<DocumentDTO> getDriverDocuments(Long driverId);
    DocumentDTO getLatestDocument(Long driverId, DocumentType documentType);
    boolean verifyDocument(Long documentId, boolean verified, String notes);
    boolean deleteDocument(Long documentId);
    DocumentDTO getDocument(Long documentId);

    // Add this new method to match what we need
    default List<DriverDocument> getDocumentsByDriverId(Long driverId) {
        return null; // Will be implemented in the DocumentServiceImpl
    }

    // Add this new method to check all documents verification status
    default boolean areAllDocumentsVerified(Long driverId) {
        return getDriverDocuments(driverId).stream()
                .allMatch(DocumentDTO::isVerified);
    }
}