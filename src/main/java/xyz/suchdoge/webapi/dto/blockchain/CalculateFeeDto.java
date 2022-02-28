package xyz.suchdoge.webapi.dto.blockchain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalculateFeeDto {
    Double amount;
    String receiverUsername;
    String priority;
}
