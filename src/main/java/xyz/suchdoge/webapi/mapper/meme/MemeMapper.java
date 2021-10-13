package xyz.suchdoge.webapi.mapper.meme;

import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.MemeResponseDto;
import xyz.suchdoge.webapi.model.Meme;

public interface MemeMapper {
    Meme memeDataDtoToMeme(MemeDataDto memeDto);

    MemeResponseDto memeToMemeResponseDto(Meme meme);
}
