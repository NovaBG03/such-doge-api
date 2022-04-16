package xyz.suchdoge.webapi.service.donation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.Donation;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.DonationRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonationServiceTest {
    @Mock
    DonationRepository donationRepository;

    DonationService donationService;

    @BeforeEach
    void setUp() {
        donationService = new DonationService(donationRepository);
    }

    @Test
    @DisplayName("Should save donation successfully")
    void shouldSaveDonationSuccessfully() {
        DogeUser sender = DogeUser.builder().id(UUID.randomUUID()).build();
        DogeUser receiver = DogeUser.builder().id(UUID.randomUUID()).build();
        Meme receiverMeme = Meme.builder().publisher(receiver).build();
        Double amount = 10d;

        donationService.saveDonation(sender, receiverMeme, amount);

        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
        verify(donationRepository).save(donationArgumentCaptor.capture());
        Donation capturedDonation = donationArgumentCaptor.getValue();

        assertThat(capturedDonation)
                .matches(x -> x.getSender().equals(sender), "is correct sender")
                .matches(x -> x.getReceiver().equals(receiver), "is correct receiver")
                .matches(x -> x.getReceiverMeme().equals(receiverMeme), "is correct meme")
                .matches(x -> x.getAmount().equals(amount), "is correct amount")
                .matches(x -> x.getSubmittedAt().isBefore(LocalDateTime.now()), "is submitted at correctly set");
    }

    @Test
    @DisplayName("Should throw exception when donation receiver and sender are the same")
    void shouldThrowExceptionWhenDonationReceiverAndSenderAreTheSame() {
        DogeUser user = DogeUser.builder().id(UUID.randomUUID()).build();
        Meme receiverMeme = Meme.builder().publisher(user).build();
        Double amount = 10d;

        assertThatThrownBy(() -> donationService.saveDonation(user, receiverMeme, amount))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("CAN_NOT_TRANSFER_ASSETS_TO_THE_SAME_ADDRESS");
    }

    @Test
    @DisplayName("Should get donations received correctly")
    void shouldGetDonationsReceivedCorrectly() {
        String username = "username";
        Double receivedAmount = 3d;

        when(donationRepository.getDonationsAmountReceivedBy(username)).thenReturn(receivedAmount);

        Double actual = donationService.getDonationsReceived(username);
        assertThat(actual).isEqualTo(receivedAmount);
    }

    @Test
    @DisplayName("Should get donations received no donations made")
    void shouldGetDonationsReceivedNoDonationsMade() {
        String username = "username";
        Double receivedAmount = null;

        when(donationRepository.getDonationsAmountReceivedBy(username)).thenReturn(receivedAmount);

        Double actual = donationService.getDonationsReceived(username);
        assertThat(actual).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Should get donations sent correctly")
    void shouldGetDonationsSentCorrectly() {
        String username = "username";
        Double sentAmount = 3d;

        when(donationRepository.getDonationsAmountSentBy(username)).thenReturn(sentAmount);

        Double actual = donationService.getDonationsSent(username);
        assertThat(actual).isEqualTo(sentAmount);
    }

    @Test
    @DisplayName("Should get donations sent no donations made")
    void shouldGetDonationsSentNoDonationsMade() {
        String username = "username";
        Double receivedAmount = null;

        when(donationRepository.getDonationsAmountSentBy(username)).thenReturn(receivedAmount);

        Double actual = donationService.getDonationsSent(username);
        assertThat(actual).isEqualTo(0);
    }
}
