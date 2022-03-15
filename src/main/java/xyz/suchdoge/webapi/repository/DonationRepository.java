package xyz.suchdoge.webapi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.suchdoge.webapi.model.Donation;

@Repository
public interface DonationRepository extends CrudRepository<Donation, Long> {
}
