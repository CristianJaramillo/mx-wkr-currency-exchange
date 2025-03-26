package mx.bank.wkr.currency_exchange.domain.model;

import java.math.BigDecimal;
import java.util.Date;

public class CurrencyExchangeTransactionModelBuilder {

    private String id;
    private TransactionStatusModel status;
    private BigDecimal originalAmount;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal buyAmount;
    private String buyCurrency;
    private BigDecimal sellAmount;
    private String sellCurrency;
    private BigDecimal resultingAmount;
    private Date createdAt;
    private Date updatedAt;

    public CurrencyExchangeTransactionModelBuilder id(String id) {
        this.id = id;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder status(TransactionStatusModel status) {
        this.status = status;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder originalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder sourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder targetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder buyAmount(BigDecimal buyAmount) {
        this.buyAmount = buyAmount;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder buyCurrency(String buyCurrency) {
        this.buyCurrency = buyCurrency;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder sellAmount(BigDecimal sellAmount) {
        this.sellAmount = sellAmount;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder sellCurrency(String sellCurrency) {
        this.sellCurrency = sellCurrency;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder resultingAmount(BigDecimal resultingAmount) {
        this.resultingAmount = resultingAmount;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder createdAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public CurrencyExchangeTransactionModelBuilder updatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public CurrencyExchangeTransactionModel build() {
        return new CurrencyExchangeTransactionModel(
                id,
                status,
                originalAmount,
                sourceCurrency,
                targetCurrency,
                buyAmount,
                buyCurrency,
                sellAmount,
                sellCurrency,
                resultingAmount,
                createdAt,
                updatedAt
        );
    }

    public static CurrencyExchangeTransactionModelBuilder from(CurrencyExchangeTransactionModel original) {
        return new CurrencyExchangeTransactionModelBuilder()
                .id(original.id())
                .status(original.status())
                .originalAmount(original.originalAmount())
                .sourceCurrency(original.sourceCurrency())
                .targetCurrency(original.targetCurrency())
                .buyAmount(original.buyAmount())
                .buyCurrency(original.buyCurrency())
                .sellAmount(original.sellAmount())
                .sellCurrency(original.sellCurrency())
                .resultingAmount(original.resultingAmount())
                .createdAt(original.createdAt())
                .updatedAt(original.updatedAt());
    }
}
