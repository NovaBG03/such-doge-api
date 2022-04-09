package xyz.suchdoge.webapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.suchdoge.webapi.model.Meme;

import java.util.Optional;

@Repository
public interface MemeRepository extends JpaRepository<Meme, Long> {
    Optional<Meme> getOptionalById(Long id);

//    long countByApprovedOnNotNull();
//
//    long countByApprovedOnNull();
//
//    long countByPublisherUsername(String publisherUsername);
//
//    long countByPublisherUsernameAndApprovedOnNull(String publisherUsername);

    long countByPublisherUsernameAndApprovedOnNotNull(String publisherUsername);

    Page<Meme> findAllByApprovedOnNull(Pageable pageable);

    Page<Meme> findAllByApprovedOnNotNull(Pageable pageable);

    Page<Meme> findAllByPublisherUsername(String publisherUsername, Pageable pageable);

    Page<Meme> findAllByPublisherUsernameAndApprovedOnNull(String publisherUsername, Pageable pageable);

    Page<Meme> findAllByPublisherUsernameAndApprovedOnNotNull(String publisherUsername, Pageable pageable);

    @Query(nativeQuery = true,
            value = "select * from meme m " +
                    "left join donation d on m.id = d.meme_receiver_id " +
                    "group by m.id " +
                    "order by max(d.submitted_at) desc, approved_on desc")
    Page<Meme> findAllByApprovedOnNotNullOrderByLatestTipped(Pageable pageable);

    @Query(nativeQuery = true,
            value = "select * from meme m " +
                    "left join donation d on m.id = d.meme_receiver_id " +
                    "join user u on m.publisher_id = u.id " +
                    "where u.username = :publisherUsername " +
                    "group by m.id " +
                    "order by max(d.submitted_at) desc, approved_on desc")
    Page<Meme> findAllByPublisherUsernameApprovedOnNotNullOrderByLatestTipped(@Param("publisherUsername") String publisherUsername, Pageable pageable);

    @Query(nativeQuery = true,
            value = "select * from meme m " +
                    "left join donation d on m.id = d.meme_receiver_id " +
                    "group by m.id " +
                    "order by sum(d.amount) desc, approved_on desc")
    Page<Meme> findAllByApprovedOnNotNullOrderByMostTipped(Pageable pageable);

    @Query(nativeQuery = true,
            value = "select * from meme m " +
                    "left join donation d on m.id = d.meme_receiver_id " +
                    "join user u on m.publisher_id = u.id " +
                    "where u.username = :publisherUsername " +
                    "group by m.id " +
                    "order by sum(d.amount) desc, approved_on desc")
    Page<Meme> findAllByPublisherUsernameApprovedOnNotNullOrderByMostTipped(@Param("publisherUsername") String publisherUsername, Pageable pageable);

    @Query(nativeQuery = true,
            value = "select * from meme m " +
                    "left join donation d on m.id = d.meme_receiver_id " +
                    "where d.submitted_at BETWEEN NOW() - INTERVAL :days DAY AND NOW() " +
                    "group by m.id " +
                    "order by sum(d.amount) desc, approved_on desc")
    Page<Meme> findAllByApprovedOnNotNullOrderByTopTipped(Pageable pageable, @Param("days") int daysFromNow);
}
