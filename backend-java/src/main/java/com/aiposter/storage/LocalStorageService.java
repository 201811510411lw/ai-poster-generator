package com.aiposter.storage;

import com.aiposter.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {
    private final Path basePath;
    private final String publicBaseUrl;

    public LocalStorageService(
            @Value("${storage.local.base-path:./data/uploads}") String basePath,
            @Value("${storage.local.public-base-url:/files}") String publicBaseUrl) {
        this.basePath = Path.of(basePath).toAbsolutePath().normalize();
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
    }

    @Override
    public StoredFile store(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", "上传文件不能为空");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String extension = resolveExtension(originalFilename);
        String datePath = LocalDate.now().toString().replace("-", "/");
        String filename = UUID.randomUUID() + extension;
        Path targetDir = basePath.resolve(folder).resolve(datePath).normalize();
        Path targetFile = targetDir.resolve(filename).normalize();

        if (!targetFile.startsWith(basePath)) {
            throw new BusinessException("INVALID_FILE_PATH", "文件路径非法");
        }

        try {
            Files.createDirectories(targetDir);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException("FILE_SAVE_FAILED", "文件保存失败");
        }

        String relativePath = basePath.relativize(targetFile).toString().replace("\\", "/");
        return new StoredFile(filename, targetFile.toString(), publicBaseUrl + "/" + relativePath);
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
