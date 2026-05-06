package com.aiposter.poster.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PosterHistoryItemResponse {
    private Long taskId;
    private String title;
    private String subtitle;
    private String status;
    private String imageUrl;
    private Integer width;
    private Integer height;
    private String materialType;
    private List<Long> assetIds;
    private String errorMessage;
    private LocalDateTime createdAt;

    public PosterHistoryItemResponse(
            Long taskId,
            String title,
            String subtitle,
            String status,
            String imageUrl,
            Integer width,
            Integer height,
            String materialType,
            List<Long> assetIds,
            String errorMessage,
            LocalDateTime createdAt) {
        this.taskId = taskId;
        this.title = title;
        this.subtitle = subtitle;
        this.status = status;
        this.imageUrl = imageUrl;
        this.width = width;
        this.height = height;
        this.materialType = materialType;
        this.assetIds = assetIds;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
    }

    public Long getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public Integer getWidth() { return width; }
    public Integer getHeight() { return height; }
    public String getMaterialType() { return materialType; }
    public List<Long> getAssetIds() { return assetIds; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
