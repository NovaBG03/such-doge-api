package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Balance extends BlockchainObject {
    private final Double availableBalance;
    private final Double pendingReceivedBalance;

    @JsonCreator
    public Balance(
            @JsonProperty("available_balance") Double availableBalance,
            @JsonProperty("pending_received_balance") Double pendingReceivedBalance,
            @JsonProperty("network") String network
    ) {
        super(network);
        this.availableBalance = availableBalance;
        this.pendingReceivedBalance = pendingReceivedBalance;
    }
}
