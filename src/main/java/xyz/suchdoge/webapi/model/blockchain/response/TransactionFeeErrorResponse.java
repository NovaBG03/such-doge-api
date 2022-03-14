package xyz.suchdoge.webapi.model.blockchain.response;

import xyz.suchdoge.webapi.model.blockchain.TransactionFeeError;

public class TransactionFeeErrorResponse extends BlockIoResponse<TransactionFeeError> {
    public TransactionFeeErrorResponse(TransactionFeeError data, String status) {
        super(data, status);
    }
}
