package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Balance {
    private final BigDecimal availableBalance;
    private final BigDecimal pendingReceivedBalance;
    private final Network network;

    @JsonCreator
    public Balance(
            @JsonProperty("available_balance") BigDecimal availableBalance,
            @JsonProperty("pending_received_balance") BigDecimal pendingReceivedBalance,
            @JsonProperty("network") String network
    ) {
        this.availableBalance = availableBalance;
        this.pendingReceivedBalance = pendingReceivedBalance;
        this.network = Network.valueOf(network);
    }
}
