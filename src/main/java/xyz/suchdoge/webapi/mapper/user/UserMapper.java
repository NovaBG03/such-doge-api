package xyz.suchdoge.webapi.mapper.user;

import xyz.suchdoge.webapi.dto.user.UserInfoResponseDto;
import xyz.suchdoge.webapi.model.DogeUser;

public interface UserMapper {
    UserInfoResponseDto dogeUserToUserInfoResponseDto(DogeUser user);
}
