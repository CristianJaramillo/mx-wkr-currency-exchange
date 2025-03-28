package mx.bank.wkr.currency_exchange;

import mx.bank.wkr.currency_exchange.infrastructure.messaging.config.RabbitMqProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RabbitMqProperties.class)
public class WrkCurrencyExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(WrkCurrencyExchangeApplication.class, args);
	}

}
