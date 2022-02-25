package xyz.suchdoge.webapi.model.blockchain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class BlockError {
    private final String message;

    public BlockError(@JsonProperty("error_message") String message) {
        this.message = message;
    }
}
