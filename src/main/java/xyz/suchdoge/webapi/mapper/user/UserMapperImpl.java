package xyz.suchdoge.webapi.mapper.user;

import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.user.UserInfoPatchResponseDto;
import xyz.suchdoge.webapi.dto.user.UserInfoResponseDto;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.user.DogeUser;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public UserInfoResponseDto dogeUserToUserInfoResponseDto(DogeUser user) {
        if (user == null) {
            return null;
        }

        return UserInfoResponseDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .publicKey(user.getDogePublicKey())
                .authorities(user.getRoles()
                        .stream()
                        .flatMap(role -> role.getLevel().getAuthorities().stream())
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    public UserInfoPatchResponseDto dogeUserToUserInfoPatchResponseDto(DogeUser user, Collection<DogeHttpException> exceptions) {
        return UserInfoPatchResponseDto.builder()
                .userInfo(this.dogeUserToUserInfoResponseDto(user))
                .errMessages(exceptions.stream()
                        .map(DogeHttpException::getMessage)
                        .collect(Collectors.toList()))
                .build();
    }
}
