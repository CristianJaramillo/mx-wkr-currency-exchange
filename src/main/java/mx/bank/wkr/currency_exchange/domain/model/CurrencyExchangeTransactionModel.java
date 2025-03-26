package mx.bank.wkr.currency_exchange.domain.model;

import java.math.BigDecimal;
import java.util.Date;

public record CurrencyExchangeTransactionModel(
        String id,
        TransactionStatusModel status,
        BigDecimal originalAmount,
        String sourceCurrency,
        String targetCurrency,
        BigDecimal buyAmount,
        String buyCurrency,
        BigDecimal sellAmount,
        String sellCurrency,
        BigDecimal resultingAmount,
        Date createdAt,
        Date updatedAt
) {}
