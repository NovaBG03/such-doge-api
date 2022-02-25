package xyz.suchdoge.webapi.model.blockchain.response;

import xyz.suchdoge.webapi.model.blockchain.Address;

public class AddressResponse extends BlockIoResponse<Address> {
    public AddressResponse(Address data, String status) {
        super(data, status);
    }
}
