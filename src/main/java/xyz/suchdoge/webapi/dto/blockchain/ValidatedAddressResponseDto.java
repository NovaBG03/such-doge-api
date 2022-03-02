package xyz.suchdoge.webapi.dto.blockchain;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ValidatedAddressResponseDto {
    private boolean isValid;
    private String address;
    private String network;
}
