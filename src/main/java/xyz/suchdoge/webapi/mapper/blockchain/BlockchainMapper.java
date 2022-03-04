package xyz.suchdoge.webapi.mapper.blockchain;

import xyz.suchdoge.webapi.dto.blockchain.BalanceResponseDto;
import xyz.suchdoge.webapi.dto.blockchain.SummarizedTransactionResponseDto;
import xyz.suchdoge.webapi.dto.blockchain.TransactionFeeResponseDto;
import xyz.suchdoge.webapi.dto.blockchain.ValidatedAddressResponseDto;
import xyz.suchdoge.webapi.model.blockchain.TransactionFee;
import xyz.suchdoge.webapi.model.blockchain.ValidatedAddress;
import xyz.suchdoge.webapi.model.blockchain.Wallet;
import xyz.suchdoge.webapi.model.blockchain.transaction.SummarizedTransaction;

public interface BlockchainMapper {
    BalanceResponseDto walletToBalanceResponseDto(Wallet wallet);
    TransactionFeeResponseDto transactionFeeToTransactionFeeResponseDto(TransactionFee TransactionFee);
    ValidatedAddressResponseDto validatedAddressToValidatedAddressResponseDto(ValidatedAddress validatedAddress);
    SummarizedTransactionResponseDto summarizedTransactionToSummarizedTransactionResponseDto(SummarizedTransaction summarizedTransaction);
}
