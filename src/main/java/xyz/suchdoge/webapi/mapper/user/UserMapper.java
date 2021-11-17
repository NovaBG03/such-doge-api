package xyz.suchdoge.webapi.mapper.user;

import xyz.suchdoge.webapi.dto.user.UserInfoDto;
import xyz.suchdoge.webapi.model.DogeUser;

public interface UserMapper {
    UserInfoDto dogeUserToUserInfoDto(DogeUser user);
}
