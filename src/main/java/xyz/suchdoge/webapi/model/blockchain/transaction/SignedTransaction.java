package xyz.suchdoge.webapi.model.blockchain.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Collection;

@Getter
public class SignedTransaction {
    private final String transactionHex;
    private final String transactionType;
    private final Collection<Signature> signatures;

    public SignedTransaction(@JsonProperty("tx_hex") String transactionHex,
                             @JsonProperty("tx_type") String transactionType,
                             @JsonProperty("signatures") Collection<Signature> signatures) {
        this.transactionHex = transactionHex;
        this.transactionType = transactionType;
        this.signatures = signatures;
    }
}
