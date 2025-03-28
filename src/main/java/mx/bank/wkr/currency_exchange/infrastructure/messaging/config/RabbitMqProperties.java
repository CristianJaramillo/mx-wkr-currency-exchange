package mx.bank.wkr.currency_exchange.infrastructure.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.rabbitmq")
public record RabbitMqProperties (
        String host,
        String queue
) {}
