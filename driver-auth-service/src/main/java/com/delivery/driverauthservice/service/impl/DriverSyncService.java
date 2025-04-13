package com.delivery.driverauthservice.service.impl;

import com.delivery.driverauthservice.config.RabbitMQConfig;
import com.delivery.driverauthservice.messaging.DriverEventPublisher;
import com.delivery.driverauthservice.messaging.DriverRegistrationEvent;
import com.delivery.driverauthservice.messaging.DriverSyncResultEvent;
import com.delivery.driverauthservice.model.DocumentType;
import com.delivery.driverauthservice.model.DriverCredential;
import com.delivery.driverauthservice.model.DriverDocument;
import com.delivery.driverauthservice.model.RegistrationStatus;
import com.delivery.driverauthservice.repository.DriverCredentialRepository;
import com.delivery.driverauthservice.repository.DriverDocumentRepository;
import com.delivery.driverauthservice.repository.VehicleRepository;
import com.delivery.driverauthservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class DriverSyncService {

    private final DriverCredentialRepository driverCredentialRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final VehicleRepository vehicleRepository;
    private final DocumentService documentService;
    private final DriverEventPublisher driverEventPublisher;

    @RabbitListener(queues = RabbitMQConfig.DRIVER_SYNC_RESULT_QUEUE)
    @Transactional
    public void handleDriverSyncResult(DriverSyncResultEvent event) {
        log.info("Received driver sync result for tempId: {}, success: {}",
                event.getTempDriverId(), event.isSuccess());

        try {
            driverCredentialRepository.findByDriverId(event.getTempDriverId())
                    .ifPresentOrElse(driver -> {
                        if (event.isSuccess()) {
                            Long oldDriverId = driver.getDriverId();
                            Long newDriverId = event.getActualDriverId();

                            // Update driver ID
                            driver.setDriverId(newDriverId);
                            driver.setRegistrationStatus(RegistrationStatus.COMPLETED);
                            driverCredentialRepository.save(driver);

                            // Update document driver references
                            List<DriverDocument> documents = driverDocumentRepository.findByDriverDriverId(oldDriverId);
                            for (DriverDocument doc : documents) {
                                // Update document's driver reference
                                doc.setDriver(driver);
                                driverDocumentRepository.save(doc);
                            }

                            // Update vehicle driver reference
                            vehicleRepository.findByDriverId(oldDriverId).ifPresent(vehicle -> {
                                vehicle.setDriver(driver);
                                vehicleRepository.save(vehicle);
                            });

                            log.info("Driver sync completed for user: {}, new ID: {}",
                                    driver.getUsername(), newDriverId);
                        } else {
                            driver.setRegistrationStatus(RegistrationStatus.FAILED);
                            driverCredentialRepository.save(driver);

                            log.error("Driver sync failed for user: {}, reason: {}",
                                    driver.getUsername(), event.getErrorMessage());
                        }
                    }, () -> log.error("Could not find driver with tempId: {}", event.getTempDriverId()));
        } catch (Exception e) {
            log.error("Error processing driver sync result: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    @Transactional
    public void retryFailedRegistrations() {
        List<DriverCredential> failedDrivers =
                driverCredentialRepository.findByRegistrationStatus(RegistrationStatus.FAILED);

        if (!failedDrivers.isEmpty()) {
            log.info("Found {} failed driver registrations to retry", failedDrivers.size());
        }

        for (DriverCredential driver : failedDrivers) {
            try {
                log.info("Retrying failed registration for driver: {}", driver.getUsername());

                // Get all documents for this driver
                List<DriverDocument> documents = driverDocumentRepository.findByDriverDriverId(driver.getDriverId());
                Map<DocumentType, Long> documentIds = documents.stream()
                        .collect(Collectors.toMap(DriverDocument::getDocumentType, DriverDocument::getId));

                // Get vehicle info
                vehicleRepository.findByDriverId(driver.getDriverId())
                        .ifPresent(vehicle -> {
                            // Create registration event
                            DriverRegistrationEvent event = DriverRegistrationEvent.builder()
                                    .tempDriverId(driver.getDriverId())
                                    .username(driver.getUsername())
                                    .email(driver.getEmail())
                                    .phoneNumber(driver.getPhoneNumber())
                                    .firstName(driver.getFirstName())
                                    .lastName(driver.getLastName())
                                    .licenseNumber(vehicle.getLicensePlate()) // Using license plate as license number if no dedicated field
                                    .vehicleType(vehicle.getVehicleType())
                                    .vehicleBrand(vehicle.getBrand())
                                    .vehicleModel(vehicle.getModel())
                                    .vehicleYear(vehicle.getYear())
                                    .licensePlate(vehicle.getLicensePlate())
                                    .vehicleColor(vehicle.getColor())
                                    .documentIds(documentIds)
                                    .retryTimestamp(LocalDateTime.now())
                                    .build();

                            // Update driver status to PENDING for retry
                            driver.setRegistrationStatus(RegistrationStatus.PENDING);
                            driverCredentialRepository.save(driver);

                            // Send event to driver management service
                            driverEventPublisher.publishDriverRegistration(event);

                            log.info("Sent retry event for driver: {}", driver.getUsername());
                        });
            } catch (Exception e) {
                log.error("Failed to retry registration for driver {}: {}",
                        driver.getUsername(), e.getMessage(), e);
            }
        }
    }

    // Note: The following methods are commented out as they are currently unused.
    // They may be needed in the future for integration with other services.

    /*
    /**
     * Creates and publishes a driver status update event
     */
    /*
    public void publishDriverStatusUpdate(Long driverId, String status) {
        try {
            driverCredentialRepository.findByDriverId(driverId).ifPresent(driver -> {
                Map<String, Object> event = new HashMap<>();
                event.put("driverId", driverId);
                event.put("status", status);
                event.put("timestamp", LocalDateTime.now());

                driverEventPublisher.publishDriverStatusUpdate(event);

                log.info("Published status update for driver {}: {}", driverId, status);
            });
        } catch (Exception e) {
            log.error("Failed to publish status update for driver {}: {}", driverId, e.getMessage());
        }
    }
    */

    /*
    /**
     * Creates and publishes a driver verification event
     */
    /*
    public void publishDriverVerification(Long driverId, boolean verified) {
        try {
            driverCredentialRepository.findByDriverId(driverId).ifPresent(driver -> {
                Map<String, Object> event = new HashMap<>();
                event.put("driverId", driverId);
                event.put("phoneVerified", verified);
                event.put("timestamp", LocalDateTime.now());

                driverEventPublisher.publishDriverVerification(event);

                log.info("Published verification update for driver {}: {}", driverId, verified);
            });
        } catch (Exception e) {
            log.error("Failed to publish verification update for driver {}: {}", driverId, e.getMessage());
        }
    }
    */
}