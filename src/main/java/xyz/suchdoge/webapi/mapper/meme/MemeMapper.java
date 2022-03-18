package xyz.suchdoge.webapi.mapper.meme;

import org.springframework.data.domain.Page;
import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.ApprovalMemeResponseDto;
import xyz.suchdoge.webapi.dto.meme.MemePageResponseDto;
import xyz.suchdoge.webapi.dto.meme.MemeResponseDto;
import xyz.suchdoge.webapi.model.Meme;

public interface MemeMapper {
    Meme memeDataDtoToMeme(MemeDataDto memeDto);

    MemeResponseDto memeToMemeResponseDto(Meme meme);

    ApprovalMemeResponseDto memeToApprovalMemeMyResponseDto(Meme meme);

    MemePageResponseDto createMemePageResponseDto(Page<Meme> memes, boolean isPublisherOrAdmin);
}
