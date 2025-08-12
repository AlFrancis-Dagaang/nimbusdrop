package dev.pollywag.nimbusdrop.repository;

import dev.pollywag.nimbusdrop.entity.Nimbus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NimbusRepository extends JpaRepository <Nimbus, Long> {

}
