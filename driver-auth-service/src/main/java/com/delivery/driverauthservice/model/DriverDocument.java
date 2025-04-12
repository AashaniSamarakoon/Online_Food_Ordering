package com.delivery.driverauthservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverCredential driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl;

    private String fileContentType;

    private Long fileSize;

    @Column(nullable = false)
    private boolean verified;

    private String verificationNotes;

    private LocalDateTime verifiedAt;

    private LocalDateTime uploadedAt;

    private LocalDateTime expiryDate;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}