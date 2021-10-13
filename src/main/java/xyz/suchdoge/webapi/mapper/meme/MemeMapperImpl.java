package xyz.suchdoge.webapi.mapper.meme;

import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.MemeResponseDto;
import xyz.suchdoge.webapi.model.Meme;

@Component
public class MemeMapperImpl implements MemeMapper {
    @Override
    public Meme memeDataDtoToMeme(MemeDataDto memeDto) {
        if (memeDto == null) {
            return null;
        }

        final Meme meme = Meme.builder()
                .title(memeDto.getTitle())
                .description(memeDto.getDescription())
                .build();

        return meme;
    }

    @Override
    public MemeResponseDto memeToMemeResponseDto(Meme meme) {
        if (meme == null) {
            return null;
        }

        final MemeResponseDto memeResponseDto = MemeResponseDto.builder()
                .id(meme.getId())
                .title(meme.getTitle())
                .description(meme.getDescription())
                .image(meme.getImage())
                .publisher(meme.getPublisher())
                .publishedOn(meme.isApproved() ? meme.getApprovedOn() : meme.getPublishedOn())
                .build();

        return memeResponseDto;
    }
}
