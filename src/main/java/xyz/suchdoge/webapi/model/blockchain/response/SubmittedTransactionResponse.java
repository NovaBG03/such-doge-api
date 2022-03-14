package xyz.suchdoge.webapi.model.blockchain.response;

import xyz.suchdoge.webapi.model.blockchain.transaction.SubmittedTransaction;

public class SubmittedTransactionResponse extends BlockIoResponse<SubmittedTransaction> {
    public SubmittedTransactionResponse(SubmittedTransaction data, String status) {
        super(data, status);
    }
}
