package com.delivery.driverauthservice.messaging;

import com.delivery.driverauthservice.config.RabbitMQConfig;
import com.delivery.driverauthservice.model.DriverCredential;
import com.delivery.driverauthservice.model.RegistrationStatus;
import com.delivery.driverauthservice.repository.DriverCredentialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverEventConsumer {

    private final DriverCredentialRepository driverCredentialRepository;

    @RabbitListener(queues = RabbitMQConfig.DRIVER_SYNC_RESULT_QUEUE)
    @Transactional
    public void handleDriverSyncResult(DriverSyncResultEvent event) {
        log.info("Received driver sync result for tempId: {}, success: {}",
                event.getTempDriverId(), event.isSuccess());

        Optional<DriverCredential> driverOpt =
                driverCredentialRepository.findByDriverId(event.getTempDriverId());

        if (driverOpt.isPresent()) {
            DriverCredential driver = driverOpt.get();

            if (event.isSuccess()) {
                // Update with actual driver ID from driver service
                driver.setDriverId(event.getActualDriverId());
                driver.setRegistrationStatus(RegistrationStatus.COMPLETED);
                log.info("Driver sync completed for user: {}", driver.getUsername());
            } else {
                driver.setRegistrationStatus(RegistrationStatus.FAILED);
                log.error("Driver sync failed for user: {}, reason: {}",
                        driver.getUsername(), event.getErrorMessage());
            }

            driverCredentialRepository.save(driver);
        } else {
            log.error("Could not find driver with tempId: {}", event.getTempDriverId());
        }
    }
}