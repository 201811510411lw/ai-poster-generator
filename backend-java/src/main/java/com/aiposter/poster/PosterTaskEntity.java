package com.aiposter.poster;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "poster_generation_task")
public class PosterTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "material_type", nullable = false, length = 32)
    private String materialType;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(name = "main_color", length = 32)
    private String mainColor;

    @Column(name = "sub_color", length = 32)
    private String subColor;

    @Column(name = "brand_description", length = 1024)
    private String brandDescription;

    @Column(name = "style_description", length = 1024)
    private String styleDescription;

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @Column(name = "activity_description", length = 1024)
    private String activityDescription;

    @Column(name = "design_requirement", length = 2048)
    private String designRequirement;

    @Column(name = "output_format", nullable = false, length = 16)
    private String outputFormat;

    @Column(name = "prompt_text", columnDefinition = "TEXT")
    private String promptText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PosterTaskStatus status;

    @Column(name = "result_filename", length = 255)
    private String resultFilename;

    @Column(name = "result_storage_path", length = 512)
    private String resultStoragePath;

    @Column(name = "result_image_url", length = 512)
    private String resultImageUrl;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (status == null) {
            status = PosterTaskStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public String getMainColor() { return mainColor; }
    public void setMainColor(String mainColor) { this.mainColor = mainColor; }
    public String getSubColor() { return subColor; }
    public void setSubColor(String subColor) { this.subColor = subColor; }
    public String getBrandDescription() { return brandDescription; }
    public void setBrandDescription(String brandDescription) { this.brandDescription = brandDescription; }
    public String getStyleDescription() { return styleDescription; }
    public void setStyleDescription(String styleDescription) { this.styleDescription = styleDescription; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getActivityDescription() { return activityDescription; }
    public void setActivityDescription(String activityDescription) { this.activityDescription = activityDescription; }
    public String getDesignRequirement() { return designRequirement; }
    public void setDesignRequirement(String designRequirement) { this.designRequirement = designRequirement; }
    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
    public String getPromptText() { return promptText; }
    public void setPromptText(String promptText) { this.promptText = promptText; }
    public PosterTaskStatus getStatus() { return status; }
    public void setStatus(PosterTaskStatus status) { this.status = status; }
    public String getResultFilename() { return resultFilename; }
    public void setResultFilename(String resultFilename) { this.resultFilename = resultFilename; }
    public String getResultStoragePath() { return resultStoragePath; }
    public void setResultStoragePath(String resultStoragePath) { this.resultStoragePath = resultStoragePath; }
    public String getResultImageUrl() { return resultImageUrl; }
    public void setResultImageUrl(String resultImageUrl) { this.resultImageUrl = resultImageUrl; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
