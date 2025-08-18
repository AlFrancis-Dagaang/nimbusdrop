package dev.pollywag.nimbusdrop.repository;

import dev.pollywag.nimbusdrop.entity.Drop;
import jakarta.transaction.Transactional;
import org.hibernate.sql.Delete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DropRepository extends JpaRepository<Drop, Integer> {
    Optional<Drop> findById(Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM Drop d WHERE d.nimbus.id = :nimbusId")
    int deleteAllByNimbusId(@Param("nimbusId") Long nimbusId);

}
