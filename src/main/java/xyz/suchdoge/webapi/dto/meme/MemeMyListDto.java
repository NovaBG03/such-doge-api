package xyz.suchdoge.webapi.dto.meme;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class MemeMyListDto {
    private Collection<MemeMyResponseDto> memes;

    public MemeMyListDto(Collection<MemeMyResponseDto> memes) {
        this.memes = memes;
    }
}
