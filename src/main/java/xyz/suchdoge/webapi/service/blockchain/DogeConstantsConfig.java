package xyz.suchdoge.webapi.service.blockchain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("application.blockchain.doge")
@Getter
@Setter
public class DogeConstantsConfig {
    private String appWalletLabel;
    private Double minTransactionAmount;
    private Double maxTransactionAmount;
    private Double transactionFeePercent;
}
