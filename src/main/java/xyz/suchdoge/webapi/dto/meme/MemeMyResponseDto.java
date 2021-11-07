package xyz.suchdoge.webapi.dto.meme;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemeMyResponseDto {
    private Long id;
    private String title;
    private String description;
    private byte[] imageBytes;
    private String publisherUsername;
    private LocalDateTime publishedOn;
    private boolean isApproved;
}
