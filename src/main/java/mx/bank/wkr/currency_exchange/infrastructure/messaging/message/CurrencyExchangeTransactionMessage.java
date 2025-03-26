package mx.bank.wkr.currency_exchange.infrastructure.messaging.message;

import com.fasterxml.jackson.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

public record CurrencyExchangeTransactionMessage(
        @JsonProperty("id")
        String id,

        @JsonProperty("status")
        TransactionStatusMessage status,

        @JsonProperty("originalAmount")
        BigDecimal originalAmount,

        @JsonProperty("sourceCurrency")
        String sourceCurrency,

        @JsonProperty("targetCurrency")
        String targetCurrency,

        @JsonProperty("buyAmountAndCurrency")
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        @JsonSetter(nulls = Nulls.FAIL)
        AmountAndCurrencyMessage buyAmountAndCurrency,

        @JsonProperty("sellAmountAndCurrency")
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        @JsonSetter(nulls = Nulls.FAIL)
        AmountAndCurrencyMessage sellAmountAndCurrency,

        @JsonProperty("resultingAmount")
        @JsonInclude(JsonInclude.Include.NON_ABSENT)
        @JsonSetter(nulls = Nulls.FAIL)
        BigDecimal resultingAmount,

        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
        Date createdAt,

        @JsonProperty("updatedAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
        Date updatedAt
) {}
