package dev.pollywag.nimbusdrop.repository;

import dev.pollywag.nimbusdrop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(long username);

    @Query("Select u.username from User u where u.email = :email")
    Optional<String> findUserDisplayNameByEmail(@Param("email") String email);

}
