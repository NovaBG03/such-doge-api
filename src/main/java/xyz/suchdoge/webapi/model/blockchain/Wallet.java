package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Wallet extends BlockchainObject {
    private final Address address;
    private final Double availableBalance;
    private final Double pendingReceivedBalance;

    public Wallet(@JsonProperty("address") String address,
                  @JsonProperty("user_id") Long userId,
                  @JsonProperty("label") String label,
                  @JsonProperty("available_balance") Double availableBalance,
                  @JsonProperty("pending_received_balance") Double pendingReceivedBalance,
                  @JsonProperty("network") String network) {
        super(network);
        this.address = new Address(address, userId, label, network);
        this.availableBalance = availableBalance;
        this.pendingReceivedBalance = pendingReceivedBalance;
    }
}
