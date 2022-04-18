package xyz.suchdoge.webapi.dto.meme.response;

import lombok.*;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@Builder
public class MemePageResponseDto {
    @Builder.Default
    private Collection<MemeResponseDto> memes = new ArrayList<>();
    private long totalCount;
}
