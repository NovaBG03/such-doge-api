package xyz.suchdoge.webapi.service.blockchain;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Block.io configuration properties.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties("application.blockchain")
public class BlockIoProps {
    /**
     * Block.io doge api key - testnet or mainnet.
     */
    private String dogeApiKey;

    /**
     * Block.io secret pin.
     */
    private String pin;

    /**
     * Block.io api version.
     */
    private int apiVersion;
}
