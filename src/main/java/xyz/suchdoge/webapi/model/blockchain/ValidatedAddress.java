package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ValidatedAddress extends BlockchainObject {
    private final Boolean isValid;
    private final String address;

    public ValidatedAddress(@JsonProperty("is_valid") boolean isValid,
                            @JsonProperty("address") String address,
                            @JsonProperty("network") String network) {
        super(network);
        this.isValid = isValid;
        this.address = address;
    }
}
