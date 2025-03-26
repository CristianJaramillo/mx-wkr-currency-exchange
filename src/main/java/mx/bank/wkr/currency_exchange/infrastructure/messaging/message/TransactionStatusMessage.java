package mx.bank.wkr.currency_exchange.infrastructure.messaging.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionStatusMessage {
    INITIATED("INITIATED"),
    PENDING("PENDING"),
    EXECUTED("EXECUTED"),
    CANCELLED("CANCELLED"),
    FAILED("FAILED");

    private final String value;

    TransactionStatusMessage(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TransactionStatusMessage fromValue(String text) {
        for (TransactionStatusMessage b : TransactionStatusMessage.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
