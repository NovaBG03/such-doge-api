package xyz.suchdoge.webapi.dto.user.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementResponseDto {
    private String name;
    private String value;
}
