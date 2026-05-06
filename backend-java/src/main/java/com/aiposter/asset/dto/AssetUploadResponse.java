package com.aiposter.asset.dto;

import java.time.LocalDateTime;

public class AssetUploadResponse {
    private Long assetId;
    private String assetType;
    private String filename;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private String url;
    private LocalDateTime createdAt;

    public AssetUploadResponse(Long assetId, String assetType, String filename, String originalFilename,
                               String contentType, Long fileSize, Integer width, Integer height,
                               String url, LocalDateTime createdAt) {
        this.assetId = assetId;
        this.assetType = assetType;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
        this.url = url;
        this.createdAt = createdAt;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetType() {
        return assetType;
    }

    public String getFilename() {
        return filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
