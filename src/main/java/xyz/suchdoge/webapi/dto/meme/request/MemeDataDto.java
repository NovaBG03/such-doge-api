package xyz.suchdoge.webapi.dto.meme.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemeDataDto {
    private String title;
    private String description;
}
