package xyz.suchdoge.webapi.dto.blockchain.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequirementsResponseDto {
    private Double minTransactionAmount;
    private Double maxTransactionAmount;
    private Double transactionFeePercent;
    private String network;
}
