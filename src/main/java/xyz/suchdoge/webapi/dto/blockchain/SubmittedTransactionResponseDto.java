package xyz.suchdoge.webapi.dto.blockchain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubmittedTransactionResponseDto {
    private String transactionId;
    private String network;
}
