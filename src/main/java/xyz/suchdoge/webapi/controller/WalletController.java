package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.suchdoge.webapi.dto.blockIo.WalletDto;
import xyz.suchdoge.webapi.service.blockchain.DogeBlockchainService;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    private final DogeBlockchainService dogeBlockchainService;

    public WalletController(DogeBlockchainService dogeBlockchainService) {
        this.dogeBlockchainService = dogeBlockchainService;
    }

    @GetMapping("/test")
    public void test() throws Exception {
        final WalletDto walletDto = this.dogeBlockchainService.getAppWallet();
        this.dogeBlockchainService.getWallets();
    }
}
