package xyz.suchdoge.webapi.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xyz.suchdoge.webapi.model.Donation;

@Repository
public interface DonationRepository extends CrudRepository<Donation, Long> {

    @Query("select sum(d.amount) " +
            "from Donation d join DogeUser u on d.receiver.id = u.id " +
            "where u.username = :username " +
            "group by u.username")
    Double getDonationsAmountReceivedBy(@Param("username") String receiverUsername);

    @Query("select sum(d.amount) " +
            "from Donation d join DogeUser u on d.sender.id = u.id " +
            "where u.username = :username " +
            "group by u.username")
    Double getDonationsAmountSentBy(@Param("username") String senderUsername);
}
