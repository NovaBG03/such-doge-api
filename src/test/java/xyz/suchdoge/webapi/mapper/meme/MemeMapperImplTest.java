package xyz.suchdoge.webapi.mapper.meme;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import xyz.suchdoge.webapi.dto.meme.request.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.response.ApprovalMemeResponseDto;
import xyz.suchdoge.webapi.dto.meme.response.MemePageResponseDto;
import xyz.suchdoge.webapi.dto.meme.response.MemeResponseDto;
import xyz.suchdoge.webapi.model.Donation;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.model.user.DogeUser;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class MemeMapperImplTest {
    MemeMapperImpl memeMapper;

    @BeforeEach
    void setUp() {
        memeMapper = new MemeMapperImpl();
    }

    @Test
    @DisplayName("Should map meme data dto to meme")
    void shouldMapMemeDataDtoToMeme() {
        String title = "title";
        String description = "description";
        MemeDataDto memeDataDto = MemeDataDto.builder()
                .title(title)
                .description(description)
                .build();

        Meme actual = memeMapper.memeDataDtoToMeme(memeDataDto);

        assertThat(actual)
                .matches(x -> x.getTitle().equals(title), "title is set")
                .matches(x -> x.getDescription().equals(description), "description is set");
    }

    @Test
    @DisplayName("Should map meme data dto to meme when null")
    void shouldMapMemeDataDtoToMemeWhenNull() {
        Meme actual = memeMapper.memeDataDtoToMeme(null);

        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("Should map not approved meme to meme response dto")
    void shouldMapNotApprovedMemeToMemeResponseDto() {
        long id = 1L;
        String title = "title";
        String description = "description";
        String imageKey = "imagekey";
        String publisherUsername = "username";
        DogeUser publisher = DogeUser.builder().username(publisherUsername).build();
        LocalDateTime publishedOn = LocalDateTime.now().minusHours(10);
        Collection<Donation> donations = Lists.newArrayList(
                Donation.builder().amount(10d).build(),
                Donation.builder().amount(10d).build()
        );
        Double expectedDonations = 20d;

        Meme meme = Meme.builder()
                .id(id)
                .title(title)
                .description(description)
                .imageKey(imageKey)
                .publisher(publisher)
                .publishedOn(publishedOn)
                .donations(donations)
                .build();

        MemeResponseDto actual = memeMapper.memeToMemeResponseDto(meme);

        assertThat(actual)
                .matches(x -> x.getId().equals(id), "id is set")
                .matches(x -> x.getTitle().equals(title), "title is set")
                .matches(x -> x.getDescription().equals(description), "description is set")
                .matches(x -> x.getImageKey().equals(imageKey), "image key is set")
                .matches(x -> x.getPublisherUsername().equals(publisherUsername), "publisher username is set")
                .matches(x -> x.getPublishedOn().equals(publishedOn), "published on is set to publish date")
                .matches(x -> x.getDonations().equals(expectedDonations), "expected donations is set");
    }

    @Test
    @DisplayName("Should map approved meme to meme response dto")
    void shouldMapApprovedMemeToMemeResponseDto() {
        long id = 1L;
        String title = "title";
        String description = "description";
        String imageKey = "imagekey";
        String publisherUsername = "username";
        DogeUser publisher = DogeUser.builder().username(publisherUsername).build();
        LocalDateTime publishedOn = LocalDateTime.now().minusHours(10);
        LocalDateTime approvedOn = LocalDateTime.now().minusHours(1);
        Collection<Donation> donations = Lists.newArrayList(
                Donation.builder().amount(10d).build(),
                Donation.builder().amount(10d).build()
        );
        Double expectedDonations = 20d;

        Meme meme = Meme.builder()
                .id(id)
                .title(title)
                .description(description)
                .imageKey(imageKey)
                .publisher(publisher)
                .publishedOn(publishedOn)
                .approvedOn(approvedOn)
                .donations(donations)
                .build();

        MemeResponseDto actual = memeMapper.memeToMemeResponseDto(meme);

        assertThat(actual)
                .matches(x -> x.getId().equals(id), "id is set")
                .matches(x -> x.getTitle().equals(title), "title is set")
                .matches(x -> x.getDescription().equals(description), "description is set")
                .matches(x -> x.getImageKey().equals(imageKey), "image key is set")
                .matches(x -> x.getPublisherUsername().equals(publisherUsername), "publisher username is set")
                .matches(x -> x.getPublishedOn().equals(approvedOn), "published on is set to approved date")
                .matches(x -> x.getDonations().equals(expectedDonations), "expected donations is set");
    }

    @Test
    @DisplayName("Should map meme to meme response dto when null")
    void shouldMapMemeToMemeResponseDtoWhenNull() {
        MemeResponseDto actual = memeMapper.memeToMemeResponseDto(null);
        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("Should map not approved meme to approval meme response dto")
    void shouldMapNotApprovedMemeToApprovalMemeResponseDto() {
        long id = 1L;
        String title = "title";
        String description = "description";
        String imageKey = "imagekey";
        String publisherUsername = "username";
        DogeUser publisher = DogeUser.builder().username(publisherUsername).build();
        LocalDateTime publishedOn = LocalDateTime.now().minusHours(10);
        Collection<Donation> donations = Lists.newArrayList(
                Donation.builder().amount(10d).build(),
                Donation.builder().amount(10d).build()
        );
        Double expectedDonations = 20d;

        Meme meme = Meme.builder()
                .id(id)
                .title(title)
                .description(description)
                .imageKey(imageKey)
                .publisher(publisher)
                .publishedOn(publishedOn)
                .donations(donations)
                .build();

        ApprovalMemeResponseDto actual = memeMapper.memeToApprovalMemeResponseDto(meme);

        assertThat(actual)
                .matches(x -> x.getId().equals(id), "id is set")
                .matches(x -> x.getTitle().equals(title), "title is set")
                .matches(x -> x.getDescription().equals(description), "description is set")
                .matches(x -> x.getImageKey().equals(imageKey), "image key is set")
                .matches(x -> x.getPublisherUsername().equals(publisherUsername), "publisher username is set")
                .matches(x -> x.getPublishedOn().equals(publishedOn), "published on is set to publish date")
                .matches(x -> x.getDonations().equals(expectedDonations), "expected donations is set")
                .matches(x -> !x.isApproved(), "is not approved");
    }

    @Test
    @DisplayName("Should map approved meme to approval meme response dto")
    void shouldMapApprovedMemeToApprovalMemeResponseDto() {
        long id = 1L;
        String title = "title";
        String description = "description";
        String imageKey = "imagekey";
        String publisherUsername = "username";
        DogeUser publisher = DogeUser.builder().username(publisherUsername).build();
        LocalDateTime publishedOn = LocalDateTime.now().minusHours(10);
        LocalDateTime approvedOn = LocalDateTime.now().minusHours(1);
        Collection<Donation> donations = Lists.newArrayList(
                Donation.builder().amount(10d).build(),
                Donation.builder().amount(10d).build()
        );
        Double expectedDonations = 20d;

        Meme meme = Meme.builder()
                .id(id)
                .title(title)
                .description(description)
                .imageKey(imageKey)
                .publisher(publisher)
                .publishedOn(publishedOn)
                .approvedOn(approvedOn)
                .donations(donations)
                .build();

        ApprovalMemeResponseDto actual = memeMapper.memeToApprovalMemeResponseDto(meme);

        assertThat(actual)
                .matches(x -> x.getId().equals(id), "id is set")
                .matches(x -> x.getTitle().equals(title), "title is set")
                .matches(x -> x.getDescription().equals(description), "description is set")
                .matches(x -> x.getImageKey().equals(imageKey), "image key is set")
                .matches(x -> x.getPublisherUsername().equals(publisherUsername), "publisher username is set")
                .matches(x -> x.getPublishedOn().equals(approvedOn), "published on is set to approved date")
                .matches(x -> x.getDonations().equals(expectedDonations), "expected donations is set")
                .matches(ApprovalMemeResponseDto::isApproved, "is approved");
    }

    @Test
    @DisplayName("Should map meme to approval meme response dto when null")
    void shouldMapMemeToApprovalMemeResponseDtoWhenNull() {
        ApprovalMemeResponseDto actual = memeMapper.memeToApprovalMemeResponseDto(null);
        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("Should create meme page response dto when is publisher or admin")
    void shouldCreateMemePageResponseDtoWhenIsPublisherOrAdmin() {
        boolean isAdminOrModerator = true;
        Page<Meme> memePage = new PageImpl<>(Lists.newArrayList(
                Meme.builder().build(),
                Meme.builder().build(),
                Meme.builder().build()
        ));

        MemePageResponseDto memePageResponseDto = memeMapper.createMemePageResponseDto(memePage, isAdminOrModerator);

        assertThat(memePageResponseDto)
                .matches(x -> x.getTotalCount() == 3, "is correct meme count");
    }

    @Test
    @DisplayName("Should create meme page response dto when is not publisher or admin")
    void shouldCreateMemePageResponseDtoWhenIsNotPublisherOrAdmin() {
        boolean isAdminOrModerator = false;
        Page<Meme> memePage = new PageImpl<>(Lists.newArrayList(
                Meme.builder().build(),
                Meme.builder().build(),
                Meme.builder().build()
        ));

        MemePageResponseDto memePageResponseDto = memeMapper.createMemePageResponseDto(memePage, isAdminOrModerator);

        assertThat(memePageResponseDto)
                .matches(x -> x.getTotalCount() == 3, "is correct meme count");
    }

    @Test
    @DisplayName("Should create meme page response dto when null")
    void shouldCreateMemePageResponseDtoWhenNull() {
        MemePageResponseDto actual = memeMapper.createMemePageResponseDto(null, true);
        assertThat(actual).isNull();
    }
}
