package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ValidatedAddress {
    private final Boolean isValid;
    private final String address;
    private final Network network;

    public ValidatedAddress(@JsonProperty("is_valid") boolean isValid,
                            @JsonProperty("address") String address,
                            @JsonProperty("network") String network) {
        this.isValid = isValid;
        this.address = address;
        this.network = Network.valueOf(network);
    }
}
