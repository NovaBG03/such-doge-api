package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TransactionFeeError extends BlockError {
    private final Double additionalFee;
    private final Double maxCustomNetworkFee;
    private final Double minCustomNetworkFee;
    private final Double networkFee;
    private final Double maxWithdrawalFee;
    private final Double availableBalance;
    private final Double minimumBalanceNeeded;

    public TransactionFeeError(@JsonProperty("error_message") String message,
                               @JsonProperty("blockio_fee") Double additionalFee,
                               @JsonProperty("estimated_max_custom_network_fee") Double maxCustomNetworkFee,
                               @JsonProperty("estimated_min_custom_network_fee") Double minCustomNetworkFee,
                               @JsonProperty("estimated_network_fee") Double networkFee,
                               @JsonProperty("max_withdrawal_available") Double maxWithdrawalBalance,
                               @JsonProperty("available_balance") Double availableBalance,
                               @JsonProperty("minimum_balance_needed") Double minimumBalanceNeeded) {
        super(message);
        this.additionalFee = additionalFee;
        this.maxCustomNetworkFee = maxCustomNetworkFee;
        this.minCustomNetworkFee = minCustomNetworkFee;
        this.networkFee = networkFee;
        this.maxWithdrawalFee = maxWithdrawalBalance;
        this.availableBalance = availableBalance;
        this.minimumBalanceNeeded = minimumBalanceNeeded;
    }
}
