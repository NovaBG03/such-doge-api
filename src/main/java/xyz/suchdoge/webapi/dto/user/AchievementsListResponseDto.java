package xyz.suchdoge.webapi.dto.user;

import lombok.*;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@Builder
public class AchievementsListResponseDto {
    @Builder.Default
    private Collection<AchievementResponseDto> achievements = new ArrayList<>();
    private String username;
}
