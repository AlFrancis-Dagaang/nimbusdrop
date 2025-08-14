package dev.pollywag.nimbusdrop.repository;

import dev.pollywag.nimbusdrop.entity.Nimbus;
import dev.pollywag.nimbusdrop.entity.NimbusSpace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NimbusSpaceRepository extends JpaRepository<NimbusSpace, Long> {
    Optional<NimbusSpace> findNimbusSpaceById(Long id);
}
