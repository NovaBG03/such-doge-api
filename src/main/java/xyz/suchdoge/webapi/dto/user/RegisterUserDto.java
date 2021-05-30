package xyz.suchdoge.webapi.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {
    private String username;
    private String email;
    private String password;
}
