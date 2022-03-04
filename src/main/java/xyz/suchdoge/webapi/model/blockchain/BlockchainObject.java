package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public abstract class BlockchainObject {
    private final Network network;

    protected BlockchainObject(@JsonProperty("network") String network) {
        this.network = Network.valueOf(network);
    }
}
