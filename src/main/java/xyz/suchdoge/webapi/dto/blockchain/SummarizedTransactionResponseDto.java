package xyz.suchdoge.webapi.dto.blockchain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SummarizedTransactionResponseDto {
    private Double additionalFee;
    private Double totalAmountToSend;
    private Double networkFee;
}
