package xyz.suchdoge.webapi.model.blockchain.response;

import xyz.suchdoge.webapi.model.blockchain.Wallet;

public class WalletResponse extends BlockIoResponse<Wallet> {
    public WalletResponse(Wallet data, String status) {
        super(data, status);
    }
}
