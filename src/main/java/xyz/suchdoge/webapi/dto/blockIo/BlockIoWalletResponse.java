package xyz.suchdoge.webapi.dto.blockIo;

public class BlockIoWalletResponse extends BlockIoResponse<WalletDto> {
    public BlockIoWalletResponse(WalletDto data, String status) {
        super(data, status);
    }
}
