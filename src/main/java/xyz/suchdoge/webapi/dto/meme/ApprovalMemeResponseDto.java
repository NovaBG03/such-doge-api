package xyz.suchdoge.webapi.dto.meme;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ApprovalMemeResponseDto extends MemeResponseDto {
    private boolean isApproved;
}
