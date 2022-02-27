package xyz.suchdoge.webapi.mapper.blockchain;

import xyz.suchdoge.webapi.dto.blockchain.BalanceResponseDto;
import xyz.suchdoge.webapi.model.blockchain.Wallet;

public interface BlockchainMapper {
    BalanceResponseDto walletToBalanceResponseDto(Wallet wallet);
}
