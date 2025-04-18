package com.delivery.driverauthservice.service.impl;

import com.delivery.driverauthservice.client.DriverServiceClient;
import com.delivery.driverauthservice.config.RabbitMQConfig;
import com.delivery.driverauthservice.dto.DriverRegistrationDTO;
import com.delivery.driverauthservice.messaging.DriverRegistrationEvent;
import com.delivery.driverauthservice.messaging.DriverSyncResultEvent;
import com.delivery.driverauthservice.model.*;
import com.delivery.driverauthservice.repository.DriverCredentialRepository;
import com.delivery.driverauthservice.repository.DriverDocumentRepository;
import com.delivery.driverauthservice.repository.VehicleRepository;
import com.delivery.driverauthservice.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverSyncService {

    private final DriverServiceClient driverServiceClient;
    private final DriverCredentialRepository driverCredentialRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final VehicleRepository vehicleRepository;
    private final DocumentService documentService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.DRIVER_REGISTRATION_QUEUE)
    @Transactional
    public void processDriverRegistration(DriverRegistrationEvent event) {
        log.info("Processing driver registration for tempId: {}", event.getTempDriverId());

        DriverSyncResultEvent resultEvent = DriverSyncResultEvent.builder()
                .tempDriverId(event.getTempDriverId())
                .success(false)
                .build();

        try {
            // Get document URLs for this driver to pass to driver service
            Map<String, String> documentUrls = new HashMap<>();
            if (event.getDocumentIds() != null && !event.getDocumentIds().isEmpty()) {
                for (Map.Entry<DocumentType, Long> entry : event.getDocumentIds().entrySet()) {
                    DocumentType type = entry.getKey();
                    Long docId = entry.getValue();

                    // Fetch document from repo
                    driverDocumentRepository.findById(docId).ifPresent(doc -> {
                        documentUrls.put(type.name(), doc.getFileUrl());
                    });
                }
            }

            // Create registration DTO
            DriverRegistrationDTO dto = DriverRegistrationDTO.builder()
                    .driverId(event.getTempDriverId()) // Use .id instead of .driverId
                    .username(event.getUsername())
                    .firstName(event.getFirstName())
                    .lastName(event.getLastName())
                    .phoneNumber(event.getPhoneNumber())
                    .email(event.getEmail())
                    .licenseNumber(event.getLicenseNumber())
                    .vehicleType(event.getVehicleType())
                    .vehicleBrand(event.getVehicleBrand())
                    .vehicleModel(event.getVehicleModel())
                    .vehicleYear(event.getVehicleYear())
                    .licensePlate(event.getLicensePlate())
                    .vehicleColor(event.getVehicleColor())
                    .latitude(0.0)  // Default until driver updates
                    .longitude(0.0) // Default until driver updates
                    .build();

            // Try to register with driver service
            var driverDetails = driverServiceClient.registerDriver(dto);

            // Success! Update result event
            resultEvent.setSuccess(true);
            resultEvent.setActualDriverId(driverDetails.getId());

            log.info("Successfully registered driver with tempId: {} to actualId: {}",
                    event.getTempDriverId(), driverDetails.getId());
        } catch (Exception e) {
            log.error("Failed to register driver with tempId: {}, error: {}",
                    event.getTempDriverId(), e.getMessage());
            resultEvent.setErrorMessage(e.getMessage());
        }

        // Send result back
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DRIVER_SYNC_RESULT_EXCHANGE,
                RabbitMQConfig.DRIVER_SYNC_RESULT_ROUTING_KEY,
                resultEvent
        );
    }

    @RabbitListener(queues = RabbitMQConfig.DRIVER_SYNC_RESULT_QUEUE)
    @Transactional
    public void handleDriverSyncResult(DriverSyncResultEvent event) {
        log.info("Received driver sync result for tempId: {}, success: {}",
                event.getTempDriverId(), event.isSuccess());

        driverCredentialRepository.findByDriverId(event.getTempDriverId())
                .ifPresent(driver -> {
                    if (event.isSuccess()) {
                        Long oldDriverId = driver.getDriverId();
                        Long newDriverId = event.getActualDriverId();

                        // Update driver ID
                        driver.setDriverId(newDriverId);
                        driver.setRegistrationStatus(RegistrationStatus.COMPLETED);
                        driverCredentialRepository.save(driver);

                        // Update document driver IDs
                        List<DriverDocument> documents = driverDocumentRepository.findByDriverDriverId(oldDriverId);
                        for (DriverDocument doc : documents) {
                            // Update document's driver reference
                            doc.setDriver(driver);
                            driverDocumentRepository.save(doc);
                        }

                        // Update vehicle driver references
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
                });
    }

    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    @Transactional
    public void retryFailedRegistrations() {
        List<DriverCredential> failedDrivers =
                driverCredentialRepository.findByRegistrationStatus(RegistrationStatus.FAILED);

        for (DriverCredential driver : failedDrivers) {
            log.info("Retrying failed registration for driver: {}", driver.getUsername());

            // Get vehicle info
            vehicleRepository.findByDriverId(driver.getDriverId()).ifPresent(vehicle -> {
                try {
                    // Create and submit registration DTO directly to driver service
                    DriverRegistrationDTO dto = DriverRegistrationDTO.builder()
                            .driverId(driver.getDriverId()) // Use .id instead of .driverId
                            .username(driver.getUsername())
                            .firstName(driver.getFirstName())
                            .lastName(driver.getLastName())
                            .phoneNumber(driver.getPhoneNumber())
                            .email(driver.getEmail())
                            .licenseNumber(vehicle.getLicensePlate()) // Using license plate as a fallback
                            .vehicleType(vehicle.getVehicleType())
                            .vehicleBrand(vehicle.getBrand())
                            .vehicleModel(vehicle.getModel())
                            .vehicleYear(vehicle.getYear())
                            .licensePlate(vehicle.getLicensePlate())
                            .vehicleColor(vehicle.getColor())
                            .latitude(0.0)
                            .longitude(0.0)
                            .build();

                    // Update driver status to RETRYING
                    driver.setRegistrationStatus(RegistrationStatus.RETRYING);
                    driverCredentialRepository.save(driver);

                    // Direct API call to driver service
                    var driverDetails = driverServiceClient.registerDriver(dto);

                    // On success, update driver status
                    driver.setRegistrationStatus(RegistrationStatus.COMPLETED);
                    driverCredentialRepository.save(driver);

                    log.info("Successfully retried registration for driver: {}", driver.getUsername());
                } catch (Exception e) {
                    log.error("Failed to retry registration for driver: {}, error: {}",
                            driver.getUsername(), e.getMessage());

                    // As fallback, also try via messaging
                    // Get all documents for this driver
                    List<DriverDocument> documents = driverDocumentRepository.findByDriverDriverId(driver.getDriverId());
                    Map<DocumentType, Long> documentIds = documents.stream()
                            .collect(Collectors.toMap(DriverDocument::getDocumentType, DriverDocument::getId));

                    DriverRegistrationEvent event = DriverRegistrationEvent.builder()
                            .tempDriverId(driver.getDriverId())
                            .username(driver.getUsername())
                            .email(driver.getEmail())
                            .phoneNumber(driver.getPhoneNumber())
                            .firstName(driver.getFirstName())
                            .lastName(driver.getLastName())
                            .licenseNumber(vehicle.getLicensePlate())
                            .vehicleType(vehicle.getVehicleType())
                            .vehicleBrand(vehicle.getBrand())
                            .vehicleModel(vehicle.getModel())
                            .vehicleYear(vehicle.getYear())
                            .licensePlate(vehicle.getLicensePlate())
                            .vehicleColor(vehicle.getColor())
                            .documentIds(documentIds)
                            .build();

                    rabbitTemplate.convertAndSend(
                            RabbitMQConfig.DRIVER_REGISTRATION_EXCHANGE,
                            RabbitMQConfig.DRIVER_REGISTRATION_ROUTING_KEY,
                            event
                    );
                }
            });
        }
    }
}