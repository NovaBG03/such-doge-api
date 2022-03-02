package xyz.suchdoge.webapi.mapper.blockchain;

import xyz.suchdoge.webapi.dto.blockchain.BalanceResponseDto;
import xyz.suchdoge.webapi.dto.blockchain.NetworkFeeResponseDto;
import xyz.suchdoge.webapi.dto.blockchain.ValidatedAddressResponseDto;
import xyz.suchdoge.webapi.model.blockchain.NetworkFee;
import xyz.suchdoge.webapi.model.blockchain.ValidatedAddress;
import xyz.suchdoge.webapi.model.blockchain.Wallet;

public interface BlockchainMapper {
    BalanceResponseDto walletToBalanceResponseDto(Wallet wallet);
    NetworkFeeResponseDto networkFeeToNetworkFeeResponseDto(NetworkFee networkFee);
    ValidatedAddressResponseDto validatedAddressToValidatedAddressDto(ValidatedAddress validatedAddress);
}
