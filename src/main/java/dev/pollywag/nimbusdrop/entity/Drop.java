package dev.pollywag.nimbusdrop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

import java.time.LocalDateTime;

@Entity
@Table(name="drops")
public class Drop {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String dropName;
    private String contentType;
    private Long size;
    private LocalDateTime uploadedAt;
    private String dropKey;

    @ManyToOne
    @JoinColumn(name = "nimbus_id", nullable = false)
    private Nimbus nimbus;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDropName() {
        return dropName;
    }

    public void setDropName(String dropName) {
        this.dropName = dropName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDropKey() {
        return dropKey;
    }

    public void setDropKey(String dropKey) {
        this.dropKey = dropKey;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }



    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Nimbus getNimbus() {
        return nimbus;
    }

    public void setNimbus(Nimbus nimbus) {
        this.nimbus = nimbus;
    }
}
