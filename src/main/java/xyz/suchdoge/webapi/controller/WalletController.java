package xyz.suchdoge.webapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.suchdoge.webapi.dto.blockchain.BalanceResponseDto;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.mapper.blockchain.BlockchainMapper;
import xyz.suchdoge.webapi.model.blockchain.Address;
import xyz.suchdoge.webapi.model.blockchain.Balance;
import xyz.suchdoge.webapi.model.blockchain.Wallet;
import xyz.suchdoge.webapi.service.blockchain.DogeBlockchainService;

import java.security.Principal;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    private final DogeBlockchainService dogeBlockchainService;
    private final BlockchainMapper blockchainMapper;

    public WalletController(DogeBlockchainService dogeBlockchainService, BlockchainMapper blockchainMapper) {
        this.dogeBlockchainService = dogeBlockchainService;
        this.blockchainMapper = blockchainMapper;
    }

    @GetMapping("/test")
    public void test() throws Exception {
        final Balance appWallet = this.dogeBlockchainService.getBalance();
        // this.dogeBlockchainService.createWallet("test1");
        Address address = this.dogeBlockchainService.createWallet("ivan");
        Wallet wallet = this.dogeBlockchainService.getWallet("default");
    }

    @GetMapping
    public BalanceResponseDto getWallet(Principal principal) {
        Wallet wallet;
        try {
            wallet = this.dogeBlockchainService.getWallet(principal.getName());
        } catch (Exception e) {
            if (e instanceof DogeHttpException) {
                throw (DogeHttpException)e;
            }
            throw new DogeHttpException("CAN_NOT_GET_ADDRESS", HttpStatus.BAD_REQUEST);
        }
        return this.blockchainMapper.walletToBalanceResponseDto(wallet);
    }
}
