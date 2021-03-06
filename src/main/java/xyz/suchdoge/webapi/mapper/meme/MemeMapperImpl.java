package xyz.suchdoge.webapi.mapper.meme;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.meme.request.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.response.ApprovalMemeResponseDto;
import xyz.suchdoge.webapi.dto.meme.response.MemePageResponseDto;
import xyz.suchdoge.webapi.dto.meme.response.MemeResponseDto;
import xyz.suchdoge.webapi.model.Donation;
import xyz.suchdoge.webapi.model.Meme;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Meme mapper.
 *
 * @author Nikita
 */
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
                .publisherUsername(meme.getPublisher() != null ? meme.getPublisher().getUsername() : null)
                .publishedOn(meme.isApproved() ? meme.getApprovedOn() : meme.getPublishedOn())
                .donations(meme.getDonations().stream().mapToDouble(Donation::getAmount).sum())
                .build();
    }

    @Override
    public ApprovalMemeResponseDto memeToApprovalMemeResponseDto(Meme meme) {
        if (meme == null) {
            return null;
        }

        return ApprovalMemeResponseDto.builder()
                .id(meme.getId())
                .title(meme.getTitle())
                .description(meme.getDescription())
                .imageKey(meme.getImageKey())
                .publisherUsername(meme.getPublisher() != null ? meme.getPublisher().getUsername() : null)
                .publishedOn(meme.isApproved() ? meme.getApprovedOn() : meme.getPublishedOn())
                .isApproved(meme.isApproved())
                .donations(meme.getDonations().stream().mapToDouble(Donation::getAmount).sum())
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
                    .map(this::memeToApprovalMemeResponseDto).
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
