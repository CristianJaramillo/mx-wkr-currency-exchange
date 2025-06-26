package mx.bank.wkr.currency_exchange.domain.resources;

public record Attachment(
        String type,
        String name,
        String content
) { }