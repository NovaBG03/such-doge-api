package xyz.suchdoge.webapi.dto.meme;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MyMemeResponseDto extends MemeResponseDto {
    private boolean isApproved;
}
