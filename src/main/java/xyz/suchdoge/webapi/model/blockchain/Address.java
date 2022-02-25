package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Address {
    private final String address;
    private final Long userId;
    private final String label;
    private final Network network;

    public Address(@JsonProperty("address") String address,
                   @JsonProperty("user_id") Long userId,
                   @JsonProperty("label") String label,
                   @JsonProperty("network") String network) {
        this.address = address;
        this.userId = userId;
        this.label = label;
        this.network = Network.valueOf(network);
    }
}
