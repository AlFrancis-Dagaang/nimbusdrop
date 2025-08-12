package dev.pollywag.nimbusdrop.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private List<Nimbus> nimbus = new ArrayList<Nimbus>();

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaxStorageBytes() {
        return maxStorageBytes;
    }

    public void setMaxStorageBytes(Long maxStorageBytes) {
        this.maxStorageBytes = maxStorageBytes;
    }

    public Long getMaxUploadsPerDay() {
        return maxUploadsPerDay;
    }

    public void setMaxUploadsPerDay(Long maxUploadsPerDay) {
        this.maxUploadsPerDay = maxUploadsPerDay;
    }

    public Long getUsedStorageBytes() {
        return usedStorageBytes;
    }

    public void setUsedStorageBytes(Long usedStorageBytes) {
        this.usedStorageBytes = usedStorageBytes;
    }

    public Long getUploadsToday() {
        return uploadsToday;
    }

    public void setUploadsToday(Long uploadsToday) {
        this.uploadsToday = uploadsToday;
    }

    public LocalDateTime getUploadsDate() {
        return uploadsDate;
    }

    public void setUploadsDate(LocalDateTime uploadsDate) {
        this.uploadsDate = uploadsDate;
    }

    public List<Nimbus> getNimbus() {
        return nimbus;
    }

    public void setNimbus(List<Nimbus> nimbus) {
        this.nimbus = nimbus;
    }
}
