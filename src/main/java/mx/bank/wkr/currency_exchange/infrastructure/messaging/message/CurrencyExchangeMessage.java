package mx.bank.wkr.currency_exchange.infrastructure.messaging.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public record CurrencyExchangeMessage(
        @JsonProperty("transaction")
        TransactionMessage transactionMessage,
        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        Date createdAt
) {}
