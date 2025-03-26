package mx.bank.wkr.currency_exchange.infrastructure.messaging.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import mx.bank.wkr.currency_exchange.infrastructure.messaging.message.serializer.BigDecimalFourDigitsSerializer;

import java.math.BigDecimal;

public record AmountAndCurrencyMessage (
        @JsonProperty("amount")
        @JsonSerialize(using = BigDecimalFourDigitsSerializer.class)
        BigDecimal amount,

        @JsonProperty("currency")
        String currency
){}
