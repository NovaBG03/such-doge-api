package xyz.suchdoge.webapi.dto.meme;

import lombok.*;
import xyz.suchdoge.webapi.model.DogeUser;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemeResponseDto {
    private Long id;
    private String title;
    private String description;
    private byte[] image;
//    private DogeUser publisher; todo create user dto
    private LocalDateTime publishedOn;
}
