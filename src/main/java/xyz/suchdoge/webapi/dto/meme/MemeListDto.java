package xyz.suchdoge.webapi.dto.meme;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class MemeListDto {
    private Collection<MemeResponseDto> memes;

    public MemeListDto(Collection<MemeResponseDto> memes) {
        this.memes = memes;
    }
}
