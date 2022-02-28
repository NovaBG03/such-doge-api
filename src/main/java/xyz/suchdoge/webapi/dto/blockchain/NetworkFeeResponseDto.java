package xyz.suchdoge.webapi.dto.blockchain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NetworkFeeResponseDto {
    private Double additionalFee;
    private Double maxCustomNetworkFee;
    private Double minCustomNetworkFee;
    private Double networkFee;
    private Long transactionSize;
    private String network;
}
