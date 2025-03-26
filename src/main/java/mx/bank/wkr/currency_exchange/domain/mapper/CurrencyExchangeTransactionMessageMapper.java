package mx.bank.wkr.currency_exchange.domain.mapper;

import mx.bank.wkr.currency_exchange.domain.model.CurrencyExchangeTransactionModel;
import mx.bank.wkr.currency_exchange.infrastructure.messaging.message.CurrencyExchangeTransactionMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Mapper(componentModel = "spring", uses = AmountAndCurrencyMessageMapperHelper.class)
public interface CurrencyExchangeTransactionMessageMapper {
    CurrencyExchangeTransactionMessageMapper INSTANCE = Mappers.getMapper(CurrencyExchangeTransactionMessageMapper.class );

    @Mappings({
            @Mapping(target = "buyAmountAndCurrency", expression = "java(AmountAndCurrencyMessageMapperHelper.toAmountAndCurrency(currencyExchangeTransactionModel.buyAmount(), currencyExchangeTransactionModel.buyCurrency()))"),
            @Mapping(target = "sellAmountAndCurrency", expression = "java(AmountAndCurrencyMessageMapperHelper.toAmountAndCurrency(currencyExchangeTransactionModel.sellAmount(), currencyExchangeTransactionModel.sellCurrency()))")
    })
    CurrencyExchangeTransactionMessage toMessage(CurrencyExchangeTransactionModel currencyExchangeTransactionModel);


    @Mappings({
            @Mapping(target = "buyAmount", source = "buyAmountAndCurrency.amount"),
            @Mapping(target = "buyCurrency", source = "buyAmountAndCurrency.currency"),
            @Mapping(target = "sellAmount", source = "sellAmountAndCurrency.amount"),
            @Mapping(target = "sellCurrency", source = "sellAmountAndCurrency.currency")
    })
    CurrencyExchangeTransactionModel toModel(CurrencyExchangeTransactionMessage message);

    // Funciones auxiliares
    default LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    default Date toDate(LocalDateTime localDateTime) {
        return localDateTime == null ? null : Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
