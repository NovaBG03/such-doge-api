package xyz.suchdoge.webapi.mapper.blockchain;

import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.blockchain.response.*;
import xyz.suchdoge.webapi.model.blockchain.TransactionFee;
import xyz.suchdoge.webapi.model.blockchain.ValidatedAddress;
import xyz.suchdoge.webapi.model.blockchain.Wallet;
import xyz.suchdoge.webapi.model.blockchain.transaction.SubmittedTransaction;
import xyz.suchdoge.webapi.model.blockchain.transaction.SummarizedTransaction;

@Component
public class BlockchainMapperImpl implements BlockchainMapper {
    @Override
    public BalanceResponseDto walletToBalanceResponseDto(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        return BalanceResponseDto.builder()
                .address(wallet.getAddress().getValue())
                .availableBalance(wallet.getAvailableBalance())
                .pendingReceivedBalance(wallet.getPendingReceivedBalance())
                .network(wallet.getNetwork().toString())
                .build();
    }

    @Override
    public TransactionFeeResponseDto transactionFeeToTransactionFeeResponseDto(TransactionFee transactionFee) {
        if (transactionFee == null) {
            return null;
        }

        return TransactionFeeResponseDto.builder()
                .additionalFee(transactionFee.getAdditionalFee())
                .maxCustomNetworkFee(transactionFee.getMaxCustomNetworkFee())
                .minCustomNetworkFee(transactionFee.getMinCustomNetworkFee())
                .networkFee(transactionFee.getNetworkFee())
                .transactionSize(transactionFee.getTransactionSize())
                .network(transactionFee.getNetwork().toString())
                .build();
    }

    @Override
    public ValidatedAddressResponseDto validatedAddressToValidatedAddressResponseDto(ValidatedAddress validatedAddress) {
        if (validatedAddress == null) {
            return null;
        }

        return ValidatedAddressResponseDto.builder()
                .isValid(validatedAddress.getIsValid())
                .address(validatedAddress.getAddress())
                .network(validatedAddress.getNetwork().toString())
                .build();
    }

    @Override
    public SummarizedTransactionResponseDto summarizedTransactionToSummarizedTransactionResponseDto(
            SummarizedTransaction summarizedTransaction) {
        if (summarizedTransaction == null) {
            return null;
        }

        return SummarizedTransactionResponseDto.builder()
                .amountToSend(summarizedTransaction.getAmountToSend())
                .networkFee(summarizedTransaction.getNetworkFee())
                .additionalFee(summarizedTransaction.getAdditionalFee())
                .build();
    }

    @Override
    public SubmittedTransactionResponseDto submittedTransactionToSubmittedTransactionResponseDto(
            SubmittedTransaction submittedTransaction) {
        if (submittedTransaction == null) {
            return null;
        }

        return SubmittedTransactionResponseDto.builder()
                .transactionId(submittedTransaction.getTransactionId())
                .network(submittedTransaction.getNetwork().toString())
                .build();
    }
}
