package dev.pollywag.nimbusdrop.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="nimbus_space")
public class NimbusSpace {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private Long maxStorageBytes;
    private Long maxUploadsPerDay;
    private Long usedStorageBytes = 0L;
    private Long uploadsToday = 0L;

    private LocalDateTime uploadsDate;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "nimbusSpace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Nimbus> nimbus;

    @PrePersist
    public void prePersist() {
        if (maxStorageBytes == null) {
            maxStorageBytes = 104_857_600L; // 100mb default
        }
        if (maxUploadsPerDay == null) {
            maxUploadsPerDay = 50L;
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
