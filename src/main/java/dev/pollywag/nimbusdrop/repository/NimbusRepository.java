package dev.pollywag.nimbusdrop.repository;

import dev.pollywag.nimbusdrop.entity.Nimbus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NimbusRepository extends JpaRepository <Nimbus, Long> {
    Optional<Nimbus> findNimbusById(Long id);

}
