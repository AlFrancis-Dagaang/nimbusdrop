package dev.pollywag.nimbusdrop.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="drop_shared_links")
public class DropSharedLink {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String token;  // UUID.randomUUID().toString()
    private Long dropId;   // reference to DropFile
    private LocalDateTime expiresAt;// nullable = no expiration

    public DropSharedLink() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getDropId() {
        return dropId;
    }

    public void setDropId(Long fileId) {
        this.dropId = fileId;
    }
}
