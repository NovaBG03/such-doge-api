package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Address extends BlockchainObject {
    private final String value;
    private final Long userId;
    private final String label;

    public Address(@JsonProperty("address") String address,
                   @JsonProperty("user_id") Long userId,
                   @JsonProperty("label") String label,
                   @JsonProperty("network") String network) {
        super(network);
        this.value = address;
        this.userId = userId;
        this.label = label;
    }
}
