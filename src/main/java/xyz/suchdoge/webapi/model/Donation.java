package xyz.suchdoge.webapi.model;

import lombok.*;
import xyz.suchdoge.webapi.model.user.DogeUser;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @NotNull(message = "DONATION_SUBMITTED_DATE_TIME_CAN_NOT_BE_NULL")
    private LocalDateTime submittedAt;

    @NotNull(message = "DONATION_AMOUNT_CAN_NOT_BE_NULL")
    @DecimalMin(value = "0.01", message = "DONATION_AMOUNT_TOO_LOW")
    private Double amount;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private DogeUser sender;

    @ManyToOne
    @JoinColumn(name = "meme_receiver_id")
    private Meme receiverMeme;
}
