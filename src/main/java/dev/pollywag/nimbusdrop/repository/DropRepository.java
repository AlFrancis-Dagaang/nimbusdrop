package dev.pollywag.nimbusdrop.repository;

import dev.pollywag.nimbusdrop.entity.Drop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DropRepository extends JpaRepository<Drop, Integer> {
}
