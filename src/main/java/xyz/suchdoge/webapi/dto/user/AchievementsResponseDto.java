package xyz.suchdoge.webapi.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementsResponseDto {
    private String username;
    private Long memesUploaded;
    private Double donationsReceived;
    private Double donationsSent;
}
