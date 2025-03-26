package mx.bank.wkr.currency_exchange.domain.mapper;

import mx.bank.wkr.currency_exchange.domain.model.CurrencyExchangeTransactionModel;
import mx.bank.wkr.currency_exchange.infrastructure.persistence.entity.CurrencyExchangeTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Mapper(componentModel = "spring")
public interface CurrencyExchangeTransactionModelMapper {

    CurrencyExchangeTransactionModelMapper INSTANCE = Mappers.getMapper( CurrencyExchangeTransactionModelMapper.class );

    @Mapping(target = "createdAt", expression = "java(toLocalDateTime(model.createdAt()))")
    @Mapping(target = "updatedAt", expression = "java(toLocalDateTime(model.updatedAt()))")
    @Mapping(target = "status", expression = "java(model.status() != null ? model.status().toString() : null)")
    CurrencyExchangeTransactionEntity toEntity(CurrencyExchangeTransactionModel model);

    @Mapping(target = "createdAt", expression = "java(toDate(entity.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(toDate(entity.getUpdatedAt()))")
    @Mapping(target = "status", expression = "java(TransactionStatusModel.valueOf(entity.getStatus()))")
    CurrencyExchangeTransactionModel toModel(CurrencyExchangeTransactionEntity entity);

    // Funciones auxiliares
    default LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    default Date toDate(LocalDateTime localDateTime) {
        return localDateTime == null ? null : Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}