package xyz.suchdoge.webapi.mapper.blockchain;

import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.blockchain.BalanceResponseDto;
import xyz.suchdoge.webapi.dto.blockchain.NetworkFeeResponseDto;
import xyz.suchdoge.webapi.dto.blockchain.ValidatedAddressResponseDto;
import xyz.suchdoge.webapi.model.blockchain.NetworkFee;
import xyz.suchdoge.webapi.model.blockchain.ValidatedAddress;
import xyz.suchdoge.webapi.model.blockchain.Wallet;

@Component
public class BlockchainMapperImpl implements BlockchainMapper {
    @Override
    public BalanceResponseDto walletToBalanceResponseDto(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        return BalanceResponseDto.builder()
                .address(wallet.getAddress())
                .availableBalance(wallet.getAvailableBalance())
                .pendingReceivedBalance(wallet.getPendingReceivedBalance())
                .network(wallet.getNetwork().toString())
                .build();
    }

    @Override
    public NetworkFeeResponseDto networkFeeToNetworkFeeResponseDto(NetworkFee networkFee) {
        if (networkFee == null) {
            return null;
        }

        return NetworkFeeResponseDto.builder()
                .additionalFee(networkFee.getAdditionalFee())
                .maxCustomNetworkFee(networkFee.getMaxCustomNetworkFee())
                .minCustomNetworkFee(networkFee.getMinCustomNetworkFee())
                .networkFee(networkFee.getNetworkFee())
                .transactionSize(networkFee.getTransactionSize())
                .network(networkFee.getNetwork().toString())
                .build();
    }

    @Override
    public ValidatedAddressResponseDto validatedAddressToValidatedAddressDto(ValidatedAddress validatedAddress) {
        if (validatedAddress == null) {
            return null;
        }

        return ValidatedAddressResponseDto.builder()
                .isValid(validatedAddress.getIsValid())
                .address(validatedAddress.getAddress())
                .network(validatedAddress.getNetwork().toString())
                .build();
    }
}
