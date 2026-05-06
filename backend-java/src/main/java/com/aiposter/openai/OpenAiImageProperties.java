package com.aiposter.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai.image")
public class OpenAiImageProperties {
    private String apiKey;
    private String baseUrl = "https://api.openai.com";
    private String model = "gpt-image-1";
    private String size = "auto";
    private String quality = "medium";

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }
    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
}
