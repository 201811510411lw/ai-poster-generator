package com.aiposter;

import com.aiposter.openai.OpenAiImageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OpenAiImageProperties.class)
public class PosterAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(PosterAdminApplication.class, args);
    }
}
