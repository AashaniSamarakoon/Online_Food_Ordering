package com.delivery.driverauthservice.repository;

import com.delivery.driverauthservice.model.DocumentType;
import com.delivery.driverauthservice.model.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverDocumentRepository extends JpaRepository<DriverDocument, Long> {
    List<DriverDocument> findByDriverDriverId(Long driverId);
    List<DriverDocument> findByDriverDriverIdAndDocumentType(Long driverId, DocumentType documentType);
    Optional<DriverDocument> findTopByDriverDriverIdAndDocumentTypeOrderByUploadedAtDesc(Long driverId, DocumentType documentType);
}