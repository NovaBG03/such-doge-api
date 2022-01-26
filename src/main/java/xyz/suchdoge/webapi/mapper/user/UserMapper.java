package xyz.suchdoge.webapi.mapper.user;

import xyz.suchdoge.webapi.dto.user.UserInfoPatchResponseDto;
import xyz.suchdoge.webapi.dto.user.UserInfoResponseDto;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.user.DogeUser;

import java.util.Collection;

public interface UserMapper {
    UserInfoResponseDto dogeUserToUserInfoResponseDto(DogeUser user);

    UserInfoPatchResponseDto dogeUserToUserInfoPatchResponseDto(DogeUser user, Collection<DogeHttpException> exceptions);
}
