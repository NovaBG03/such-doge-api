package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Wallet {
    private final String address;
    private final String label;
    private final BigDecimal availableBalance;
    private final BigDecimal pendingReceivedBalance;
    private final Long userId;
    private final Network network;

    public Wallet(@JsonProperty("address") String address,
                  @JsonProperty("label") String label,
                  @JsonProperty("available_balance") BigDecimal availableBalance,
                  @JsonProperty("pending_received_balance") BigDecimal pendingReceivedBalance,
                  @JsonProperty("user_id") Long userId,
                  @JsonProperty("network") String network) {
        this.address = address;
        this.label = label;
        this.availableBalance = availableBalance;
        this.pendingReceivedBalance = pendingReceivedBalance;
        this.userId = userId;
        this.network = Network.valueOf(network);
    }
}
