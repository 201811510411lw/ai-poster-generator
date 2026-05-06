package com.aiposter.poster.dto;

import java.util.List;

public class GeneratePosterRequest {
    private String materialType;
    private Integer width;
    private Integer height;
    private String mainColor;
    private String subColor;
    private String brandDescription;
    private String styleDescription;
    private String title;
    private String subtitle;
    private String activityDescription;
    private String designRequirement;
    private String outputFormat;
    private List<Long> assetIds;

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
    public List<Long> getAssetIds() { return assetIds; }
    public void setAssetIds(List<Long> assetIds) { this.assetIds = assetIds; }
}
