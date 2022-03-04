package xyz.suchdoge.webapi.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.model.blockchain.TransactionPriority;
import xyz.suchdoge.webapi.model.blockchain.transaction.PreparedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SummarizedTransaction;
import xyz.suchdoge.webapi.service.blockchain.DogeBlockchainService;

@Service
public class TransactionService {
    private final DogeBlockchainService dogeBlockchainService;
    private final MemeService memeService;

    public TransactionService(DogeBlockchainService dogeBlockchainService, MemeService memeService) {
        this.dogeBlockchainService = dogeBlockchainService;
        this.memeService = memeService;
    }

    public SummarizedTransaction summarizeDonation(Double amount,
                                                   String fromUsername,
                                                   Long receiverMemeId,
                                                   TransactionPriority priority) throws Exception {
        final Meme receiverMeme = this.memeService.getMeme(receiverMemeId, fromUsername);
        return this.summarizeTransaction(amount, fromUsername, receiverMeme.getPublisher().getUsername(), priority);
    }

    private SummarizedTransaction summarizeTransaction(Double amount,
                                                       String fromUsername,
                                                       String receiverUsername,
                                                       TransactionPriority priority) throws Exception {
        this.dogeBlockchainService.validateTransactionAmount(amount);
        if (fromUsername.equals(receiverUsername)) {
            throw new DogeHttpException("CAN_NOT_TRANSFER_ASSETS_TO_THE_SAME_ADDRESS", HttpStatus.BAD_REQUEST);
        }

        PreparedTransaction preparedTransaction = this.dogeBlockchainService
                .prepareTransaction(amount, fromUsername, receiverUsername, priority);

        SummarizedTransaction summarizedTransaction =
                this.dogeBlockchainService.summarizePreparedTransaction(preparedTransaction);

        summarizedTransaction.addFee(this.dogeBlockchainService.calculateAdditionalFee(amount));
        return summarizedTransaction;
    }
}
