package xyz.suchdoge.webapi.dto.blockchain.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceResponseDto {
    private String address;
    private Double availableBalance;
    private Double pendingReceivedBalance;
    private String network;
}
