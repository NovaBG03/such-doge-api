package xyz.suchdoge.webapi.model.blockchain.response;

import lombok.Getter;

@Getter
public class BlockIoResponse<T> {
    private final T data;
    private final Status status;

    public BlockIoResponse(T data, String status) {
        this.data = data;
        this.status = Status.valueOf(status.toUpperCase());
    }
}
