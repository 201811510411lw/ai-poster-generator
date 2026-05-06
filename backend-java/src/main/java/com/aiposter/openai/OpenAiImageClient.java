package com.aiposter.openai;

import com.aiposter.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiImageClient {
    private static final Logger log = LoggerFactory.getLogger(OpenAiImageClient.class);

    private final OpenAiImageProperties properties;
    private final RestClient restClient;

    public OpenAiImageClient(OpenAiImageProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(resolveBaseUrl(properties.getBaseUrl()))
                .build();
    }

    public String generateImageBase64(String prompt, String size) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new BusinessException("OPENAI_API_KEY_MISSING", "OpenAI API Key 未配置");
        }
        if (!StringUtils.hasText(prompt)) {
            throw new BusinessException("EMPTY_PROMPT", "生成提示词不能为空");
        }

        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "prompt", prompt,
                "size", StringUtils.hasText(size) ? size : properties.getSize(),
                "quality", properties.getQuality()
        );

        OpenAiImageResponse response;
        try {
            response = restClient.post()
                    .uri("/v1/images/generations")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(properties.getApiKey()))
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAiImageResponse.class);
        } catch (Exception ex) {
            log.warn("调用 OpenAI 图片生成失败: baseUrl={}, model={}, size={}, reason={}",
                    resolveBaseUrl(properties.getBaseUrl()),
                    properties.getModel(),
                    StringUtils.hasText(size) ? size : properties.getSize(),
                    ex.getMessage(),
                    ex);
            throw new BusinessException("OPENAI_IMAGE_GENERATION_FAILED", "调用 OpenAI 图片生成失败");
        }

        if (response == null || response.data() == null || response.data().isEmpty()
                || !StringUtils.hasText(response.data().getFirst().b64_json())) {
            throw new BusinessException("OPENAI_EMPTY_IMAGE", "OpenAI 未返回图片数据");
        }

        return response.data().getFirst().b64_json();
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "https://api.openai.com";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String resolveBaseUrl(String value) {
        String baseUrl = trimTrailingSlash(value);
        return baseUrl.endsWith("/v1") ? baseUrl.substring(0, baseUrl.length() - 3) : baseUrl;
    }

    public record OpenAiImageResponse(List<ImageData> data) {
    }

    public record ImageData(String b64_json) {
    }
}
