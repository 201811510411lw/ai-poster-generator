package com.aiposter.storage;

import com.aiposter.common.BusinessException;
import com.volcengine.tos.TOSV2;
import com.volcengine.tos.TOSV2ClientBuilder;
import com.volcengine.tos.model.object.PutObjectInput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "tos")
public class TosStorageService implements StorageService {
    private final TOSV2 tosClient;
    private final String bucket;
    private final String publicBaseUrl;

    public TosStorageService(
            @Value("${storage.tos.region}") String region,
            @Value("${storage.tos.endpoint}") String endpoint,
            @Value("${storage.tos.bucket}") String bucket,
            @Value("${storage.tos.public-base-url:}") String publicBaseUrl,
            @Value("${storage.tos.access-key:${TOS_ACCESS_KEY:}}") String accessKey,
            @Value("${storage.tos.secret-key:${TOS_SECRET_KEY:}}") String secretKey) {
        if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
            throw new BusinessException("TOS_CONFIG_ERROR", "TOS 访问密钥未配置");
        }
        this.bucket = bucket;
        this.publicBaseUrl = trimTrailingSlash(StringUtils.hasText(publicBaseUrl) ? publicBaseUrl : endpoint + "/" + bucket);
        this.tosClient = new TOSV2ClientBuilder()
                .build(region, endpoint, accessKey, secretKey);
    }

    @Override
    public StoredFile store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", "上传文件不能为空");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String extension = resolveExtension(originalFilename);
        String datePath = LocalDate.now().toString().replace("-", "/");
        String objectKey = folder + "/" + datePath + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectInput input = new PutObjectInput()
                    .setBucket(bucket)
                    .setKey(objectKey)
                    .setContent(file.getInputStream())
                    .setContentLength(file.getSize());
            tosClient.putObject(input);
        } catch (IOException ex) {
            throw new BusinessException("FILE_READ_FAILED", "文件读取失败");
        } catch (Exception ex) {
            throw new BusinessException("TOS_UPLOAD_FAILED", "文件上传到 TOS 失败");
        }

        return new StoredFile(objectKey.substring(objectKey.lastIndexOf('/') + 1), objectKey, publicBaseUrl + "/" + objectKey);
    }

    private String resolveExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex).toLowerCase();
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
