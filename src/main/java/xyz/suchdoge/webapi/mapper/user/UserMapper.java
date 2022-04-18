package xyz.suchdoge.webapi.mapper.user;

import xyz.suchdoge.webapi.dto.user.response.UserInfoResponseDto;
import xyz.suchdoge.webapi.model.user.DogeUser;

/**
 * User mapper.
 *
 * @author Nikita
 */
public interface UserMapper {
    UserInfoResponseDto dogeUserToUserInfoResponseDto(DogeUser user);
}
