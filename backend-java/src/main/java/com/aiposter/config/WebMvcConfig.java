package com.aiposter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final Path uploadBasePath;

    public WebMvcConfig(@Value("${storage.local.base-path:./data/uploads}") String uploadBasePath) {
        this.uploadBasePath = Path.of(uploadBasePath).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations(uploadBasePath.toUri().toString());
    }
}
