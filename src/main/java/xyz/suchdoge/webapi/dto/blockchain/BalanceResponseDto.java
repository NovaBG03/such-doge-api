package xyz.suchdoge.webapi.dto.blockchain;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponseDto {
    private String address;
    private BigDecimal availableBalance;
    private BigDecimal pendingReceivedBalance;
    private String network;
}
