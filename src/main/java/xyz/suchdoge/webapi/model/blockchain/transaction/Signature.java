package xyz.suchdoge.webapi.model.blockchain.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Signature {
    private final String publicKey;
    private final String signature;
    private final Integer input_index;

    public Signature(@JsonProperty("public_key") String publicKey,
                     @JsonProperty("signature") String signature,
                     @JsonProperty("input_index") Integer input_index) {
        this.publicKey = publicKey;
        this.signature = signature;
        this.input_index = input_index;
    }
}
