package xyz.suchdoge.webapi.dto.user.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestActivationResponseDto {
    private long secondsTillNextRequest;
}
