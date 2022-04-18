package xyz.suchdoge.webapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import xyz.suchdoge.webapi.dto.user.response.AchievementsListResponseDto;
import xyz.suchdoge.webapi.service.donation.DonationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AchievementsServiceTest {
    @Mock
    MemeService memeService;
    @Mock
    DonationService donationService;

    AchievementsService achievementsService;

    @BeforeEach
    void initAchievementsService() {
        achievementsService = new AchievementsService(memeService, donationService);
    }

    @Test
    @DisplayName("Should get all achievements correctly")
    void shouldGetAllAchievementsCorrectly() {
        String username = "ivan";
        Long memesCount = 3L;
        Double donationsReceived = 23d;
        Double donationsSent = 11d;

        when(memeService.getMemesCount(username)).thenReturn(memesCount);
        when(donationService.getDonationsReceived(username)).thenReturn(donationsReceived);
        when(donationService.getDonationsSent(username)).thenReturn(donationsSent);

        AchievementsListResponseDto achievementList = achievementsService.getAchievements(username);

        assertThat(achievementList).isNotNull();
        assertThat(achievementList.getUsername()).isEqualTo(username);
        assertThat(achievementList.getAchievements())
                .anyMatch(x -> x.getName().equals("Memes uploaded") && x.getValue().equals(memesCount.toString()))
                .anyMatch(x -> x.getName().equals("Donations received") && x.getValue().equals(donationsReceived + " DOGE"))
                .anyMatch(x -> x.getName().equals("Donations sent") && x.getValue().equals(donationsSent + " DOGE"));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        String username = "notFoundUser";
        when(memeService.getMemesCount(username)).thenThrow(UsernameNotFoundException.class);
        assertThatThrownBy(() -> achievementsService.getAchievements(username))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
