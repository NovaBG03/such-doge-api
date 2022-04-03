package xyz.suchdoge.webapi.service.donation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.Donation;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.DonationRepository;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class DonationService {
    private final DonationRepository donationRepository;

    public DonationService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    public void saveDonation(DogeUser sender, Meme receiverMeme, Double amount) {
        if (sender.equals(receiverMeme.getPublisher())) {
            throw new DogeHttpException("CAN_NOT_TRANSFER_ASSETS_TO_THE_SAME_ADDRESS", HttpStatus.BAD_REQUEST);
        }

        this.donationRepository.save(Donation.builder()
                .sender(sender)
                .receiver(receiverMeme.getPublisher())
                .receiverMeme(receiverMeme)
                .amount(amount)
                .submittedAt(LocalDateTime.now())
                .build());
    }

    public Double getDonationsReceived(String username) {
        final Double donationsReceived = donationRepository.getDonationsAmountReceivedBy(username);
        return Objects.requireNonNullElse(donationsReceived, 0d);
    }

    public Double getDonationsSent(String username) {
        final Double donationsSent = donationRepository.getDonationsAmountSentBy(username);
        return Objects.requireNonNullElse(donationsSent, 0d);
    }
}
