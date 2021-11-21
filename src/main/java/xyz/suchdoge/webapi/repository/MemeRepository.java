package xyz.suchdoge.webapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.suchdoge.webapi.model.Meme;

import java.util.Collection;
import java.util.List;

@Repository
public interface MemeRepository extends JpaRepository<Meme, Long> {
    long countByApprovedOnNotNull();

    long countByApprovedOnNull();

    long countByPublisherUsername(String publisherUsername);

    long countByPublisherUsernameAndApprovedOnNull(String publisherUsername);

    long countByPublisherUsernameAndApprovedOnNotNull(String publisherUsername);

    Page<Meme> findAllByApprovedOnNull(Pageable pageable);

    Page<Meme> findAllByApprovedOnNotNull(Pageable pageable);

    Page<Meme> findAllByPublisherUsername(String publisherUsername, Pageable pageable);

    Page<Meme> findAllByPublisherUsernameAndApprovedOnNull(String publisherUsername, Pageable pageable);

    Page<Meme> findAllByPublisherUsernameAndApprovedOnNotNull(String publisherUsername, Pageable pageable);
}
