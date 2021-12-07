package xyz.suchdoge.webapi.mapper.user;

import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.user.UserInfoResponseDto;
import xyz.suchdoge.webapi.model.DogeUser;

import java.util.stream.Collectors;

@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public UserInfoResponseDto dogeUserToUserInfoResponseDto(DogeUser user) {
        if (user == null) {
            return null;
        }

        final UserInfoResponseDto userInfoDto = UserInfoResponseDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .publicKey(user.getDogePublicKey())
                .authorities(user.getRoles()
                        .stream()
                        .flatMap(role -> role.getLevel().getAuthorities().stream())
                        .collect(Collectors.toSet()))
                .build();

        return userInfoDto;
    }
}
