package xyz.suchdoge.webapi.service.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lib.blockIo.BlockIo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.dto.blockIo.BlockIoResponse;
import xyz.suchdoge.webapi.dto.blockIo.BlockIoWalletResponse;
import xyz.suchdoge.webapi.dto.blockIo.WalletDto;

@Service
public class DogeBlockchainService {
    private final BlockIo blockIo;
    private final ObjectMapper objectMapper;

    public DogeBlockchainService(@Qualifier("dogeBlockIo") BlockIo blockIo, ObjectMapper objectMapper) {
        this.blockIo = blockIo;
        this.objectMapper = objectMapper;
    }

    public WalletDto getAppWallet() throws Exception {
        String jsonResponse = this.blockIo.GetBalance(null).toString();
        BlockIoResponse<WalletDto> walletResponse = this.objectMapper.readValue(jsonResponse, BlockIoWalletResponse.class);
        return walletResponse.getData();
    }

    public void getWallets() throws Exception {
        // print all addresses on this account
        System.out.println(this.blockIo.GetMyAddresses(null));
    }
}
