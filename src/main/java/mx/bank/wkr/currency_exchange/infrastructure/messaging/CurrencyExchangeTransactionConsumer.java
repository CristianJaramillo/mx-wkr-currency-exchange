package mx.bank.wkr.currency_exchange.infrastructure.messaging;


import lombok.extern.slf4j.Slf4j;
import mx.bank.wkr.currency_exchange.domain.mapper.CurrencyExchangeTransactionMessageMapper;
import mx.bank.wkr.currency_exchange.domain.mapper.CurrencyExchangeTransactionModelMapper;
import mx.bank.wkr.currency_exchange.domain.model.CurrencyExchangeTransactionModel;
import mx.bank.wkr.currency_exchange.infrastructure.messaging.config.RabbitMqProperties;
import mx.bank.wkr.currency_exchange.infrastructure.messaging.message.CurrencyExchangeTransactionMessage;
import mx.bank.wkr.currency_exchange.infrastructure.persistence.entity.CurrencyExchangeTransactionEntity;
import mx.bank.wkr.currency_exchange.infrastructure.persistence.repository.CurrencyExchangeRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CurrencyExchangeTransactionConsumer {

    private final CurrencyExchangeRepository currencyExchangeRepository;

    public CurrencyExchangeTransactionConsumer(CurrencyExchangeRepository currencyExchangeRepository) {
        this.currencyExchangeRepository = currencyExchangeRepository;
    }


    @RabbitListener(queues = "${spring.rabbitmq.queue}")
    public void handle(@Payload CurrencyExchangeTransactionMessage currencyExchangeTransactionMessage) {
        log.info("🔁 Evento recibido: {}", currencyExchangeTransactionMessage);

        CurrencyExchangeTransactionModel currencyExchangeTransactionModel = CurrencyExchangeTransactionMessageMapper.INSTANCE.toModel(currencyExchangeTransactionMessage);

        CurrencyExchangeTransactionEntity currencyExchangeTransactionEntity = CurrencyExchangeTransactionModelMapper.INSTANCE.toEntity(currencyExchangeTransactionModel);

        try {

            if(currencyExchangeTransactionEntity.getId() != null) {
                if(currencyExchangeRepository.findById(currencyExchangeTransactionEntity.getId()).isPresent()) {
                    log.info("✅ Transacción previamente registrada: {}", currencyExchangeTransactionEntity.getId());
                    return;
                }
            }

            currencyExchangeRepository.save(currencyExchangeTransactionEntity);
            log.info("✅ Persistido después de caída: {}", currencyExchangeTransactionEntity.getId());

        } catch (Exception e) {
            log.warn("🚨 Aún no se puede persistir. Reintentando...");
            throw e; // Para activar el retry
        }

    }
}