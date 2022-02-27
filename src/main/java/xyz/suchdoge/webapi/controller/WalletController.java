package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.suchdoge.webapi.model.blockchain.Address;
import xyz.suchdoge.webapi.model.blockchain.Balance;
import xyz.suchdoge.webapi.model.blockchain.Wallet;
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
        final Balance appWallet = this.dogeBlockchainService.getBalance();
        // this.dogeBlockchainService.createWallet("test1");
        Address address = this.dogeBlockchainService.createWallet("ivan");
        Wallet wallet = this.dogeBlockchainService.getWallet("default");
    }
}
