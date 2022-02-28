package xyz.suchdoge.webapi.model.blockchain.response;

import xyz.suchdoge.webapi.model.blockchain.NetworkFee;

public class NetworkFeeResponse extends BlockIoResponse<NetworkFee> {
    public NetworkFeeResponse(NetworkFee data, String status) {
        super(data, status);
    }
}
