package xyz.suchdoge.webapi.model.blockchain.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import xyz.suchdoge.webapi.model.blockchain.BlockchainObject;

@Getter
public class SubmittedTransaction extends BlockchainObject {
    private final String transactionId;

    public SubmittedTransaction(@JsonProperty("txid") String transactionId,
                                @JsonProperty("network") String network) {
        super(network);
        this.transactionId = transactionId;
    }
}
