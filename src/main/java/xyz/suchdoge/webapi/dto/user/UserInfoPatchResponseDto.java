package xyz.suchdoge.webapi.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@Builder
public class UserInfoPatchResponseDto {
    private UserInfoResponseDto userInfo;

    @Builder.Default
    private Collection<String> errMessages = new ArrayList<>();
}
