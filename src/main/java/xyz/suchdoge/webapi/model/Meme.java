package xyz.suchdoge.webapi.model;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @NotNull(message = "MEME_TITLE_NULL")
    @Length.List({
            @Length(min = 3, message = "MEME_TITLE_TOO_SHORT"),
            @Length(max= 30, message = "MEME_TITLE_TOO_LONG")
    })
    private String title;

    @Length.List({
            @Length(min = 3, message = "MEME_DESCRIPTION_TOO_SHORT"),
            @Length(max = 100, message = "MEME_DESCRIPTION_TOO_LONG")
    })
    private String description;

    @Lob
    @NotNull(message = "MEME_IMAGE_NULL")
    private byte[] image;

    @ManyToOne
    @JoinColumn(name = "publisher_id", nullable = false)
    private DogeUser publisher;

    private LocalDateTime publishedOn;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private DogeUser approvedBy;

    private LocalDateTime approvedOn;

    public boolean isApproved() {
        return this.approvedBy != null
                && this.approvedOn != null
                && this.approvedOn.isBefore(LocalDateTime.now());
    }
}
