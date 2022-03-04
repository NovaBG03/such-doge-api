package xyz.suchdoge.webapi.model.blockchain.response;

import xyz.suchdoge.webapi.model.blockchain.TransactionFee;

public class NetworkFeeResponse extends BlockIoResponse<TransactionFee> {
    public NetworkFeeResponse(TransactionFee data, String status) {
        super(data, status);
    }
}
