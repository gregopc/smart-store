package com.example.smartstore.event;

import com.example.smartstore.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${smartstore.events.exchange}")
    private String exchange;

    @Value("${smartstore.events.routing-key}")
    private String routingKey;

    public UserActionEvent.UserActionEventBuilder newEvent(UserActionType type, User user, String route) {
        UserActionEvent.UserActionEventBuilder builder = UserActionEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(type)
                .route(route)
                .occurredAt(Instant.now().toString());

        if (user != null) {
            builder.userId(user.getId());
            builder.userEmail(user.getEmail());
        }

        return builder;
    }

    public void publish(UserActionEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
        } catch (Exception ex) {
            log.warn("Could not publish user action event {} to RabbitMQ", event.getEventType(), ex);
        }
    }
}
