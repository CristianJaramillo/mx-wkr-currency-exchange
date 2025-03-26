package mx.bank.wkr.currency_exchange.domain.model;

/**
 * Estado actual de la transacción. Posibles valores: INITIATED, EXECUTED, CANCELLED, FAILED, etc.
 */
public enum TransactionStatusModel {
  INITIATED("INITIATED"),
  PENDING("PENDING"),
  EXECUTED("EXECUTED"),
  CANCELLED("CANCELLED"),
  FAILED("FAILED");

  private final String value;

  TransactionStatusModel(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static TransactionStatusModel fromValue(String text) {
    for (TransactionStatusModel b : TransactionStatusModel.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
