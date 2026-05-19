package com.example.smartstore.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${smartstore.events.exchange}")
    private String userActionsExchange;

    @Value("${smartstore.events.queue}")
    private String userActionsQueue;

    @Value("${smartstore.events.routing-key}")
    private String userActionsRoutingKey;

    @Bean
    public TopicExchange userActionsExchange() {
        return new TopicExchange(userActionsExchange, true, false);
    }

    @Bean
    public Queue userActionsQueue() {
        return new Queue(userActionsQueue, true);
    }

    @Bean
    public Binding userActionsBinding() {
        return BindingBuilder
                .bind(userActionsQueue())
                .to(userActionsExchange())
                .with(userActionsRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
