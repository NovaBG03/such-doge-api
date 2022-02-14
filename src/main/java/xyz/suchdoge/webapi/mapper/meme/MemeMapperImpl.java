package xyz.suchdoge.webapi.mapper.meme;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.MyMemeResponseDto;
import xyz.suchdoge.webapi.dto.meme.MemePageResponseDto;
import xyz.suchdoge.webapi.dto.meme.MemeResponseDto;
import xyz.suchdoge.webapi.model.Meme;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
                .imageKey(meme.getImageKey())
                .publisherUsername(meme.getPublisher().getUsername())
                .publishedOn(meme.isApproved() ? meme.getApprovedOn() : meme.getPublishedOn())
                .build();

        return memeResponseDto;
    }

    @Override
    public MyMemeResponseDto memeToMemeMyResponseDto(Meme meme) {
        if (meme == null) {
            return null;
        }

        final MyMemeResponseDto memeMyResponseDto = MyMemeResponseDto.builder()
                .id(meme.getId())
                .title(meme.getTitle())
                .description(meme.getDescription())
                .imageKey(meme.getImageKey())
                .publisherUsername(meme.getPublisher().getUsername())
                .publishedOn(meme.isApproved() ? meme.getApprovedOn() : meme.getPublishedOn())
                .isApproved(meme.isApproved())
                .build();

        return memeMyResponseDto;
    }

    @Override
    public MemePageResponseDto createMemePageResponseDto(Page<Meme> memes, boolean isAdminOrModerator) {
        if (memes == null) {
            return MemePageResponseDto.builder()
                    .totalCount(memes.getTotalElements())
                    .build();
        }

        List<MemeResponseDto> memeResponseDtos;
        if (isAdminOrModerator) {
            memeResponseDtos = StreamSupport.stream(memes.spliterator(), false)
                    .map(this::memeToMemeMyResponseDto).
                    collect(Collectors.toList());
        } else {
            memeResponseDtos = StreamSupport.stream(memes.spliterator(), false)
                    .map(this::memeToMemeResponseDto).
                    collect(Collectors.toList());
        }

        return MemePageResponseDto.builder()
                .memes(memeResponseDtos)
                .totalCount(memes.getTotalElements())
                .build();
    }
}
