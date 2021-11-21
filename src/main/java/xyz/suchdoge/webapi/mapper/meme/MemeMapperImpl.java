package xyz.suchdoge.webapi.mapper.meme;

import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.MemeMyResponseDto;
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
                .title(memeDto.getTitle().trim())
                .description(memeDto.getDescription().trim())
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
                .imageBytes(meme.getImage())
                .publisherUsername(meme.getPublisher().getUsername())
                .publishedOn(meme.isApproved() ? meme.getApprovedOn() : meme.getPublishedOn())
                .build();

        return memeResponseDto;
    }

    @Override
    public MemeMyResponseDto memeToMemeMyResponseDto(Meme meme) {
        if (meme == null) {
            return null;
        }

        final MemeMyResponseDto memeMyResponseDto = MemeMyResponseDto.builder()
                .id(meme.getId())
                .title(meme.getTitle())
                .description(meme.getDescription())
                .imageBytes(meme.getImage())
                .publisherUsername(meme.getPublisher().getUsername())
                .publishedOn(meme.isApproved() ? meme.getApprovedOn() : meme.getPublishedOn())
                .isApproved(meme.isApproved())
                .build();

        return memeMyResponseDto;
    }
}
