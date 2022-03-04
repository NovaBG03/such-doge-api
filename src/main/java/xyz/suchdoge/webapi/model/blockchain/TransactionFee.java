package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TransactionFee extends BlockchainObject {
    private Double additionalFee;
    private final Double maxCustomNetworkFee;
    private final Double minCustomNetworkFee;
    private final Double networkFee;
    private final Long transactionSize;

    public TransactionFee(@JsonProperty("blockio_fee") String additionalFee,
                          @JsonProperty("estimated_max_custom_network_fee") String maxCustomNetworkFee,
                          @JsonProperty("estimated_min_custom_network_fee") String minCustomNetworkFee,
                          @JsonProperty("estimated_network_fee") String networkFee,
                          @JsonProperty("estimated_tx_size") String transactionSize,
                          @JsonProperty("network") String network) {
        super(network);
        this.additionalFee = Double.valueOf(additionalFee);
        this.maxCustomNetworkFee = Double.valueOf(maxCustomNetworkFee);
        this.minCustomNetworkFee = Double.valueOf(minCustomNetworkFee);
        this.networkFee = Double.valueOf(networkFee);
        this.transactionSize = Long.valueOf(transactionSize);
    }

    public void addFee(Double fee) {
        this.additionalFee += fee;
    }
}
