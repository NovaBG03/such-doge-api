package xyz.suchdoge.webapi.model.blockchain.response;

import xyz.suchdoge.webapi.model.blockchain.BlockError;

public class BlockErrorResponse extends BlockIoResponse<BlockError> {
    public BlockErrorResponse(BlockError data, String status) {
        super(data, status);
    }
}
