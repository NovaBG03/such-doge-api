package xyz.suchdoge.webapi.mapper.blockchain;

import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.blockchain.BalanceResponseDto;
import xyz.suchdoge.webapi.dto.blockchain.NetworkFeeResponseDto;
import xyz.suchdoge.webapi.model.blockchain.NetworkFee;
import xyz.suchdoge.webapi.model.blockchain.Wallet;

@Component
public class BlockchainMapperImpl implements BlockchainMapper {
    @Override
    public BalanceResponseDto walletToBalanceResponseDto(Wallet wallet) {
        if (wallet == null) {
            return null;
        }

        final BalanceResponseDto balanceResponseDto = BalanceResponseDto.builder()
                .address(wallet.getAddress())
                .availableBalance(wallet.getAvailableBalance())
                .pendingReceivedBalance(wallet.getPendingReceivedBalance())
                .network(wallet.getNetwork().toString())
                .build();

        return balanceResponseDto;
    }

    @Override
    public NetworkFeeResponseDto networkFeeToNetworkFeeResponseDto(NetworkFee networkFee) {
        if (networkFee == null) {
            return null;
        }

        final NetworkFeeResponseDto networkFeeResponseDto = NetworkFeeResponseDto.builder()
                .additionalFee(networkFee.getAdditionalFee().doubleValue())
                .maxCustomNetworkFee(networkFee.getMaxCustomNetworkFee().doubleValue())
                .minCustomNetworkFee(networkFee.getMinCustomNetworkFee().doubleValue())
                .networkFee(networkFee.getNetworkFee().doubleValue())
                .transactionSize(networkFee.getTransactionSize())
                .network(networkFee.getNetwork().toString())
                .build();

        return networkFeeResponseDto;
    }
}
