package mx.bank.wkr.currency_exchange.infrastructure.messaging;


import lombok.extern.slf4j.Slf4j;
import mx.bank.wkr.currency_exchange.infrastructure.messaging.message.CurrencyExchangeMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CurrencyExchangeTransactionConsumer {

    @RabbitListener(queues = "currency.exchange.transaction")
    public void handle(@Payload CurrencyExchangeMessage currencyExchangeMessage) {
        log.info("🔁 Evento recibido: {}", currencyExchangeMessage);
        // Aquí va la lógica de procesamiento
    }
}