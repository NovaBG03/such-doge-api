package xyz.suchdoge.webapi.mapper.user;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import xyz.suchdoge.webapi.dto.user.response.UserInfoResponseDto;
import xyz.suchdoge.webapi.model.user.DogeRole;
import xyz.suchdoge.webapi.model.user.DogeRoleLevel;
import xyz.suchdoge.webapi.model.user.DogeUser;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperImplTest {
    UserMapperImpl userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
    }

    @Test
    @DisplayName("Should map user to user info response dto")
    void shouldMapUserToUserInfoResponseDto() {
        String username = "ivan";
        String email = "ivan@abv.bg";
        String publicKey = "publickey";
        DogeRole userRole = DogeRole.builder().level(DogeRoleLevel.USER).build();
        Collection<DogeRole> roles = Lists.newArrayList(userRole);
        DogeUser user = DogeUser.builder()
                .username(username)
                .email(email)
                .dogePublicKey(publicKey)
                .roles(roles)
                .build();

        UserInfoResponseDto actual = userMapper.dogeUserToUserInfoResponseDto(user);

        assertThat(actual)
                .matches(x -> x.getUsername().equals(username), "username is set")
                .matches(x -> x.getEmail().equals(email), "email is set")
                .matches(x -> x.getPublicKey().equals(publicKey), "public key is set")
                .matches(x -> x.getAuthorities().size() == 1, "set 1 authority");
    }

    @Test
    @DisplayName("Should map user to user info response dto when null")
    void shouldMapUserToUserInfoResponseDtoWhenNull() {
        UserInfoResponseDto actual = userMapper.dogeUserToUserInfoResponseDto(null);
        assertThat(actual).isNull();
    }
}
