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

    Page<Meme> findAllByApprovedOnNotNull(Pageable pageable);

    Page<Meme> findAllByApprovedOnNull(Pageable pageable);
}
