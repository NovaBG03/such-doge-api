package xyz.suchdoge.webapi.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.model.blockchain.TransactionPriority;
import xyz.suchdoge.webapi.model.blockchain.transaction.PreparedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SignedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SubmittedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SummarizedTransaction;
import xyz.suchdoge.webapi.service.blockchain.DogeBlockchainService;

@Service
public class TransactionService {
    private final DogeBlockchainService dogeBlockchainService;
    private final DogeUserService userService;
    private final MemeService memeService;

    public TransactionService(DogeBlockchainService dogeBlockchainService,
                              DogeUserService userService,
                              MemeService memeService) {
        this.dogeBlockchainService = dogeBlockchainService;
        this.userService = userService;
        this.memeService = memeService;
    }

    public SummarizedTransaction summarizeDonation(Double amount,
                                                   String fromUsername,
                                                   Long receiverMemeId,
                                                   TransactionPriority priority) throws Exception {
        final Meme receiverMeme = this.memeService.getMeme(receiverMemeId, fromUsername);
        return this.summarizeTransaction(amount, fromUsername, receiverMeme.getPublisher().getUsername(), priority);
    }

    public SubmittedTransaction donate(Double amount, String fromUsername, Long receiverMemeId, TransactionPriority priority) throws Exception {
        final Meme receiverMeme = this.memeService.getMeme(receiverMemeId, fromUsername);
        final SubmittedTransaction submittedTransaction =
                this.performTransaction(amount, fromUsername, receiverMeme.getPublisher().getUsername(), priority);
        // todo add transaction info to the database
        return submittedTransaction;
    }

    private SummarizedTransaction summarizeTransaction(Double amount,
                                                       String fromUsername,
                                                       String receiverUsername,
                                                       TransactionPriority priority) throws Exception {
        validateTransaction(amount, fromUsername, receiverUsername);

        PreparedTransaction preparedTransaction = this.dogeBlockchainService
                .prepareTransaction(amount, fromUsername, receiverUsername, priority);

        SummarizedTransaction summarizedTransaction =
                this.dogeBlockchainService.summarizePreparedTransaction(preparedTransaction);

        summarizedTransaction.addApplicationFee(this.dogeBlockchainService.calculateAdditionalFee(amount));
        return summarizedTransaction;
    }

    private SubmittedTransaction performTransaction(Double amount,
                                                    String fromUsername,
                                                    String receiverUsername,
                                                    TransactionPriority priority) throws Exception {
        PreparedTransaction preparedTransaction = this.dogeBlockchainService
                .prepareTransaction(amount, fromUsername, receiverUsername, priority);

        SignedTransaction singedTransaction = this.dogeBlockchainService.signTransaction(preparedTransaction);
        return this.dogeBlockchainService.submitTransaction(singedTransaction);
    }

    private void validateTransaction(Double amount, String fromUsername, String receiverUsername) {
        this.userService.getConfirmedUser(fromUsername);

        this.dogeBlockchainService.validateTransactionAmount(amount);

        if (fromUsername.equals(receiverUsername)) {
            throw new DogeHttpException("CAN_NOT_TRANSFER_ASSETS_TO_THE_SAME_ADDRESS", HttpStatus.BAD_REQUEST);
        }
    }
}
