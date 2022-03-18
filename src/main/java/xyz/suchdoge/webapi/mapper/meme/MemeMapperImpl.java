package xyz.suchdoge.webapi.mapper.meme;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.ApprovalMemeResponseDto;
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

        return Meme.builder()
                .title(memeDto.getTitle().trim())
                .description(memeDto.getDescription().trim())
                .build();
    }

    @Override
    public MemeResponseDto memeToMemeResponseDto(Meme meme) {
        if (meme == null) {
            return null;
        }

        return MemeResponseDto.builder()
                .id(meme.getId())
                .title(meme.getTitle())
                .description(meme.getDescription())
                .imageKey(meme.getImageKey())
                .publisherUsername(meme.getPublisher().getUsername())
                .publishedOn(meme.isApproved() ? meme.getApprovedOn() : meme.getPublishedOn())
                .build();
    }

    @Override
    public ApprovalMemeResponseDto memeToApprovalMemeMyResponseDto(Meme meme) {
        if (meme == null) {
            return null;
        }

        return ApprovalMemeResponseDto.builder()
                .id(meme.getId())
                .title(meme.getTitle())
                .description(meme.getDescription())
                .imageKey(meme.getImageKey())
                .publisherUsername(meme.getPublisher().getUsername())
                .publishedOn(meme.isApproved() ? meme.getApprovedOn() : meme.getPublishedOn())
                .isApproved(meme.isApproved())
                .build();
    }

    @Override
    public MemePageResponseDto createMemePageResponseDto(Page<Meme> memes, boolean isPublisherOrAdmin) {
        if (memes == null) {
            return null;
        }

        List<MemeResponseDto> memeResponseDtos;
        if (isPublisherOrAdmin) {
            memeResponseDtos = StreamSupport.stream(memes.spliterator(), false)
                    .map(this::memeToApprovalMemeMyResponseDto).
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
