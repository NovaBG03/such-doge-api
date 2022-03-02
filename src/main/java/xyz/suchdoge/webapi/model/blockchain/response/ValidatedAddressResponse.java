package xyz.suchdoge.webapi.model.blockchain.response;

import xyz.suchdoge.webapi.model.blockchain.ValidatedAddress;

public class ValidatedAddressResponse extends BlockIoResponse<ValidatedAddress> {
    public ValidatedAddressResponse(ValidatedAddress data, String status) {
        super(data, status);
    }
}
