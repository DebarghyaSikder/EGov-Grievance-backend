package com.grievance.grievance_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "grievance.exchange";
    public static final String QUEUE_GRIEVANCE_CREATED = "grievance.created.queue";
    public static final String QUEUE_STATUS_CHANGED = "grievance.status.changed.queue";
    public static final String ROUTING_KEY_CREATED = "grievance.created";
    public static final String ROUTING_KEY_STATUS_CHANGED = "grievance.status.changed";
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue grievanceCreatedQueue() {
        return new Queue(QUEUE_GRIEVANCE_CREATED, true);
    }

    @Bean
    public Queue statusChangedQueue() {
        return new Queue(QUEUE_STATUS_CHANGED, true);
    }

    @Bean
    public Binding grievanceCreatedBinding(Queue grievanceCreatedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(grievanceCreatedQueue).to(exchange).with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding statusChangedBinding(Queue statusChangedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(statusChangedQueue).to(exchange).with(ROUTING_KEY_STATUS_CHANGED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}