package dev.pollywag.nimbusdrop.repository;

import dev.pollywag.nimbusdrop.entity.DropSharedLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DropShareLinkRepository extends JpaRepository <DropSharedLink, Long> {


    Optional<DropSharedLink> findByToken(String token);


}
