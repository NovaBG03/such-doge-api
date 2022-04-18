package xyz.suchdoge.webapi.dto.blockchain.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionFeeResponseDto {
    private Double additionalFee;
    private Double maxCustomNetworkFee;
    private Double minCustomNetworkFee;
    private Double networkFee;
    private Long transactionSize;
    private String network;
}
