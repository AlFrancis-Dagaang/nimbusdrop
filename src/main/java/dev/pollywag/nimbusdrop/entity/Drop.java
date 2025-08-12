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
    private Integer maxDownloadPerDay = 20;
    private Integer maxViewsPerDay = 50;
    private Integer downloadsToday = 0;
    private Integer viewsToday = 0;
    private LocalDateTime downloadsDate;
    private LocalDateTime viewsDate;
    private LocalDateTime uploadedAt;

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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Integer getMaxDownloadPerDay() {
        return maxDownloadPerDay;
    }

    public void setMaxDownloadPerDay(Integer maxDownloadPerDay) {
        this.maxDownloadPerDay = maxDownloadPerDay;
    }

    public Integer getMaxViewsPerDay() {
        return maxViewsPerDay;
    }

    public void setMaxViewsPerDay(Integer maxViewsPerDay) {
        this.maxViewsPerDay = maxViewsPerDay;
    }

    public Integer getViewsToday() {
        return viewsToday;
    }

    public void setViewsToday(Integer viewsToday) {
        this.viewsToday = viewsToday;
    }

    public Integer getDownloadsToday() {
        return downloadsToday;
    }

    public void setDownloadsToday(Integer downloadsToday) {
        this.downloadsToday = downloadsToday;
    }

    public LocalDateTime getDownloadsDate() {
        return downloadsDate;
    }

    public void setDownloadsDate(LocalDateTime downloadsDate) {
        this.downloadsDate = downloadsDate;
    }

    public LocalDateTime getViewsDate() {
        return viewsDate;
    }

    public void setViewsDate(LocalDateTime viewsDate) {
        this.viewsDate = viewsDate;
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
