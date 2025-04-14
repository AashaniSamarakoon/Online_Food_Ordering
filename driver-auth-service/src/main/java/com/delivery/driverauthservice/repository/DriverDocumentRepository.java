package com.delivery.driverauthservice.repository;

import com.delivery.driverauthservice.model.DocumentType;
import com.delivery.driverauthservice.model.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverDocumentRepository extends JpaRepository<DriverDocument, Long> {
    List<DriverDocument> findByDriverDriverId(Long driverId);
    Optional<DriverDocument> findTopByDriverDriverIdAndDocumentTypeOrderByUploadedAtDesc(Long driverId, DocumentType documentType);
    @Modifying
    @Query(value = "UPDATE driver_documents SET verified = true, verified_at = NOW() WHERE id = :documentId", nativeQuery = true)
    int markDocumentAsVerified(@Param("documentId") Long documentId);
}