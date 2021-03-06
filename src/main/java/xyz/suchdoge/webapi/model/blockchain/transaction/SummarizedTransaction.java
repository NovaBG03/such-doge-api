package xyz.suchdoge.webapi.model.blockchain.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import xyz.suchdoge.webapi.model.blockchain.BlockchainObject;

@Getter
public class SummarizedTransaction extends BlockchainObject {
    private Double additionalFee;
    private Double amountToSend;
    private final Double networkFee;

    public SummarizedTransaction(@JsonProperty("blockio_fee") Double additionalFee,
                                 @JsonProperty("total_amount_to_send") Double amountToSend,
                                 @JsonProperty("network_fee") Double networkFee,
                                 @JsonProperty("network") String network) {
        super(network);
        this.additionalFee = additionalFee;
        this.amountToSend = amountToSend;
        this.networkFee = networkFee;
    }

    public void addApplicationFee(Double fee) {
        this.additionalFee += fee;
        this.amountToSend -= fee;
    }
}
