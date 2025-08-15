package dev.pollywag.nimbusdrop.repository;

import dev.pollywag.nimbusdrop.entity.Drop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DropRepository extends JpaRepository<Drop, Integer> {
    Optional<Drop> findById(Long id);
}
