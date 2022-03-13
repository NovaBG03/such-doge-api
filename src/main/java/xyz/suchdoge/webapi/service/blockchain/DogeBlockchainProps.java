package xyz.suchdoge.webapi.service.blockchain;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Doge blockchain configuration properties.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("application.blockchain.doge")
public class DogeBlockchainProps {
    /**
     * Application doge wallet label.
     */
    private String appWalletLabel;

    /**
     * Minimum transaction amount.
     */
    private Double minTransactionAmount;

    /**
     * Maximum transaction amount
     */
    private Double maxTransactionAmount;

    /**
     * Transaction fee percent.
     */
    private Double transactionFeePercent;
}
