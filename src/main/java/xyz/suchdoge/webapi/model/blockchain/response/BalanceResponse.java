package xyz.suchdoge.webapi.model.blockchain.response;

import xyz.suchdoge.webapi.model.blockchain.Balance;

public class BalanceResponse extends BlockIoResponse<Balance> {
    public BalanceResponse(Balance data, String status) {
        super(data, status);
    }
}
