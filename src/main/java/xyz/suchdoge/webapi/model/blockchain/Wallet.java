package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Wallet {
    private final String address;
    private final String label;
    private final Double availableBalance;
    private final Double pendingReceivedBalance;
    private final Long userId;
    private final Network network;

    public Wallet(@JsonProperty("address") String address,
                  @JsonProperty("label") String label,
                  @JsonProperty("available_balance") Double availableBalance,
                  @JsonProperty("pending_received_balance") Double pendingReceivedBalance,
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
