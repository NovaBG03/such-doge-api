package xyz.suchdoge.webapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import xyz.suchdoge.webapi.dto.blockchain.*;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.mapper.blockchain.BlockchainMapper;
import xyz.suchdoge.webapi.model.blockchain.transaction.PreparedTransaction;
import xyz.suchdoge.webapi.model.blockchain.TransactionPriority;
import xyz.suchdoge.webapi.model.blockchain.TransactionFee;
import xyz.suchdoge.webapi.model.blockchain.Wallet;
import xyz.suchdoge.webapi.model.blockchain.transaction.SignedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SubmittedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SummarizedTransaction;
import xyz.suchdoge.webapi.service.TransactionService;
import xyz.suchdoge.webapi.service.blockchain.DogeBlockchainService;

import java.security.Principal;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    private final DogeBlockchainService dogeBlockchainService;
    private final TransactionService transactionService;
    private final BlockchainMapper blockchainMapper;

    public WalletController(DogeBlockchainService dogeBlockchainService,
                            TransactionService transactionService,
                            BlockchainMapper blockchainMapper) {
        this.dogeBlockchainService = dogeBlockchainService;
        this.transactionService = transactionService;
        this.blockchainMapper = blockchainMapper;
    }

    @GetMapping("/test")
    public void test() throws Exception {
        PreparedTransaction preparedTransaction = this.dogeBlockchainService
                .prepareTransaction(10d, "nova", "ivan", TransactionPriority.LOW);

        SummarizedTransaction summarizedTransaction =
                this.dogeBlockchainService.summarizePreparedTransaction(preparedTransaction);

        SignedTransaction singedTransaction = this.dogeBlockchainService.signTransaction(preparedTransaction);
        SubmittedTransaction submittedTransaction = this.dogeBlockchainService.submitTransaction(singedTransaction);
    }

    @GetMapping
    public BalanceResponseDto getWallet(Principal principal) {
        Wallet wallet;
        try {
            wallet = this.dogeBlockchainService.getWallet(principal.getName());
        } catch (Exception e) {
            if (e instanceof DogeHttpException) {
                throw (DogeHttpException) e;
            }
            throw new DogeHttpException("CAN_NOT_GET_ADDRESS", HttpStatus.BAD_REQUEST);
        }
        return this.blockchainMapper.walletToBalanceResponseDto(wallet);
    }

    @GetMapping("/validate")
    public ValidatedAddressResponseDto validateAddress(@RequestParam(name = "address") String address) throws Exception {
        return this.blockchainMapper.validatedAddressToValidatedAddressResponseDto(
                this.dogeBlockchainService.validateAddress(address)
        );
    }

    @GetMapping("/transaction/requirements")
    public TransactionRequirementsResponseDto getTransactionRequirements() {
        return this.dogeBlockchainService.getTransactionRequirements();
    }

    @PostMapping("/transaction/estimatedFee")
    public TransactionFeeResponseDto getEstimatedTransactionFee(@RequestParam String receiverUsername,
                                                                @RequestBody TransactionDto transactionDto) throws Exception {
        // todo validate user with provided username exists
        TransactionFee transactionFee = this.dogeBlockchainService.calculateTransactionFee(
                transactionDto.getAmount(),
                receiverUsername,
                TransactionPriority.valueOf(transactionDto.getPriority().toUpperCase()));
        return this.blockchainMapper.transactionFeeToTransactionFeeResponseDto(transactionFee);
    }

    @PostMapping("/transaction/summarized")
    public SummarizedTransactionResponseDto summarizeDonation(@RequestParam Long memeId,
                                                              @RequestBody TransactionDto transactionDto,
                                                              Principal principal) throws Exception {
        SummarizedTransaction summarizedTransaction = this.transactionService.summarizeDonation(
                transactionDto.getAmount(),
                principal.getName(),
                memeId,
                TransactionPriority.valueOf(transactionDto.getPriority().toUpperCase()));

        return this.blockchainMapper.summarizedTransactionToSummarizedTransactionResponseDto(summarizedTransaction);
    }

    @PostMapping("/transaction/donation")
    public SubmittedTransactionResponseDto donate(@RequestParam Long memeId,
                       @RequestBody TransactionDto transactionDto,
                       Principal principal) throws Exception {
        SubmittedTransaction submittedTransaction = this.transactionService.donate(
                transactionDto.getAmount(),
                principal.getName(),
                memeId,
                TransactionPriority.valueOf(transactionDto.getPriority().toUpperCase()));

        return this.blockchainMapper.submittedTransactionToSubmittedTransactionResponseDto(submittedTransaction);
    }
}
