package xyz.suchdoge.webapi.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoDto {
    private String email;
    private String publicKey;
}