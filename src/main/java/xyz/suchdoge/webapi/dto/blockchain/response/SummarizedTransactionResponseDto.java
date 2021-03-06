package xyz.suchdoge.webapi.dto.blockchain.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SummarizedTransactionResponseDto {
    private Double additionalFee;
    private Double amountToSend;
    private Double networkFee;
}
