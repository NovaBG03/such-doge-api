package xyz.suchdoge.webapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.suchdoge.webapi.dto.blockchain.*;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.mapper.blockchain.BlockchainMapper;
import xyz.suchdoge.webapi.model.blockchain.TransactionPriority;
import xyz.suchdoge.webapi.model.blockchain.NetworkFee;
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
    public NetworkFee test() throws Exception {
        NetworkFee networkFee = this.dogeBlockchainService
                .calculateNetworkFee(2.2d, "nova", TransactionPriority.LOW);
        return networkFee;
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

    @GetMapping("validate")
    public ValidatedAddressResponseDto validateAddress(@RequestParam(name = "address") String address) throws Exception {
        return this.blockchainMapper.validatedAddressToValidatedAddressDto(
                this.dogeBlockchainService.validateAddress(address)
        );
    }

    @GetMapping("transaction/fee")
    public NetworkFeeResponseDto getTransactionFee(@RequestBody CalculateFeeDto calculateFeeRequestDto) throws Exception {
        NetworkFee networkFee = this.dogeBlockchainService.calculateNetworkFee(
                calculateFeeRequestDto.getAmount(),
                calculateFeeRequestDto.getReceiverUsername(),
                TransactionPriority.valueOf(calculateFeeRequestDto.getPriority().toUpperCase()));
        return this.blockchainMapper.networkFeeToNetworkFeeResponseDto(networkFee);
    }

    @GetMapping("transaction/requirements")
    public TransactionRequirementsResponseDto getTransactionRequirements() {
        return this.dogeBlockchainService.getTransactionRequirements();
    }

}
