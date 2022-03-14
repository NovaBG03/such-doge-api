package xyz.suchdoge.webapi.mapper.blockchain;

import xyz.suchdoge.webapi.dto.blockchain.*;
import xyz.suchdoge.webapi.model.blockchain.TransactionFee;
import xyz.suchdoge.webapi.model.blockchain.ValidatedAddress;
import xyz.suchdoge.webapi.model.blockchain.Wallet;
import xyz.suchdoge.webapi.model.blockchain.transaction.SubmittedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SummarizedTransaction;

public interface BlockchainMapper {
    BalanceResponseDto walletToBalanceResponseDto(Wallet wallet);
    TransactionFeeResponseDto transactionFeeToTransactionFeeResponseDto(TransactionFee TransactionFee);
    ValidatedAddressResponseDto validatedAddressToValidatedAddressResponseDto(ValidatedAddress validatedAddress);
    SummarizedTransactionResponseDto summarizedTransactionToSummarizedTransactionResponseDto(SummarizedTransaction summarizedTransaction);
    SubmittedTransactionResponseDto submittedTransactionToSubmittedTransactionResponseDto(SubmittedTransaction submittedTransaction);
}
