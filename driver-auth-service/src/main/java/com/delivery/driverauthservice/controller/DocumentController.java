package com.delivery.driverauthservice.controller;

import com.delivery.driverauthservice.dto.DocumentDTO;
import com.delivery.driverauthservice.dto.DocumentUploadRequest;
import com.delivery.driverauthservice.model.DocumentType;
import com.delivery.driverauthservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
//    public ResponseEntity<DocumentDTO> uploadDocument(
//            @RequestParam("driverId") Long driverId,
//            @RequestParam("documentType") DocumentType documentType,
//            @RequestParam("file") MultipartFile file) {
//
//        DocumentDTO document = documentService.uploadDocument(driverId, documentType, file);
//        return ResponseEntity.ok(document);
//    }

    @PostMapping("/upload-base64")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<DocumentDTO> uploadDocumentBase64(@RequestBody DocumentUploadRequest request) {
        DocumentDTO document = documentService.uploadDocumentBase64(request);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<List<DocumentDTO>> getDriverDocuments(@PathVariable Long driverId) {
        List<DocumentDTO> documents = documentService.getDriverDocuments(driverId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/driver/{driverId}/type/{documentType}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<DocumentDTO> getLatestDocument(
            @PathVariable Long driverId,
            @PathVariable DocumentType documentType) {

        DocumentDTO document = documentService.getLatestDocument(driverId, documentType);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(document);
    }

    @PutMapping("/{documentId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> verifyDocument(
            @PathVariable Long documentId,
            @RequestParam boolean verified,
            @RequestParam(required = false) String notes) {

        boolean result = documentService.verifyDocument(documentId, verified, notes);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('DRIVER') or hasRole('ADMIN')")
    public ResponseEntity<Boolean> deleteDocument(@PathVariable Long documentId) {
        boolean result = documentService.deleteDocument(documentId);
        return ResponseEntity.ok(result);
    }
}