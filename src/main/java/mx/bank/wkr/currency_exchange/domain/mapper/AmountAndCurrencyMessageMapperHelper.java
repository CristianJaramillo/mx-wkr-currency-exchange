package mx.bank.wkr.currency_exchange.domain.mapper;

import mx.bank.wkr.currency_exchange.infrastructure.messaging.message.AmountAndCurrencyMessage;

import java.math.BigDecimal;

public class AmountAndCurrencyMessageMapperHelper {
    public static AmountAndCurrencyMessage toAmountAndCurrency(BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            return null;
        }
        return new AmountAndCurrencyMessage(amount, currency);
    }
}
