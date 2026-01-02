package com.grievance.grievance_service.service;

import com.grievance.grievance_service.config.RabbitMQConfig;
import com.grievance.grievance_service.dto.GrievanceEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrievanceEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishGrievanceCreated(GrievanceEvent event) {
        event.setEventType("GRIEVANCE_SUBMITTED");
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_CREATED,
                event
        );
        log.info("Published grievance created event: {}", event.getGrievanceNumber());
    }

    public void publishStatusChanged(GrievanceEvent event) {
        event.setEventType("STATUS_CHANGED");
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY_STATUS_CHANGED,
                event
        );
        log.info("Published status changed event: {} -> {}", event.getOldStatus(), event.getNewStatus());
    }
}