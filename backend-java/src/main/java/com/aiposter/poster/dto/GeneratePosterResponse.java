package com.aiposter.poster.dto;

public class GeneratePosterResponse {
    private Long taskId;
    private String status;
    private String imageUrl;
    private Integer width;
    private Integer height;

    public GeneratePosterResponse(Long taskId, String status, String imageUrl, Integer width, Integer height) {
        this.taskId = taskId;
        this.status = status;
        this.imageUrl = imageUrl;
        this.width = width;
        this.height = height;
    }

    public Long getTaskId() { return taskId; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    public Integer getWidth() { return width; }
    public Integer getHeight() { return height; }
}
