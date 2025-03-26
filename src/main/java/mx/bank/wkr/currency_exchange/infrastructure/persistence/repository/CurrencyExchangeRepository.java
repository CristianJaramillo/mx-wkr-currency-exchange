package mx.bank.wkr.currency_exchange.infrastructure.persistence.repository;

import mx.bank.wkr.currency_exchange.infrastructure.persistence.entity.CurrencyExchangeTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyExchangeRepository extends JpaRepository<CurrencyExchangeTransactionEntity, String> {
    // Puedes agregar consultas personalizadas si lo necesitas, por ejemplo:
    // List<ExchangeCurrencyEntity> findByStatus(String status);
}
