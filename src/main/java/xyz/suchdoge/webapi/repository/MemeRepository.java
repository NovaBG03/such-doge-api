package xyz.suchdoge.webapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.suchdoge.webapi.model.Meme;

@Repository
public interface MemeRepository extends JpaRepository<Meme, Long> {
}
