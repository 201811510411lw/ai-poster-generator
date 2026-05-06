package com.aiposter.storage;

import com.aiposter.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "storage", name = "type", havingValue = "local", matchIfMissing = true)
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
        String extension = resolveExtension(originalFilename, file.getContentType());
        String filename = UUID.randomUUID() + extension;
        Path targetFile = resolveTargetFile(folder, filename);

        try {
            Files.createDirectories(targetFile.getParent());
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException("FILE_SAVE_FAILED", "文件保存失败");
        }

        return toStoredFile(filename, targetFile);
    }

    @Override
    public StoredFile store(byte[] content, String originalFilename, String contentType, String folder) {
        if (content == null || content.length == 0) {
            throw new BusinessException("EMPTY_FILE", "文件内容不能为空");
        }

        String extension = resolveExtension(originalFilename, contentType);
        String filename = UUID.randomUUID() + extension;
        Path targetFile = resolveTargetFile(folder, filename);

        try {
            Files.createDirectories(targetFile.getParent());
            Files.write(targetFile, content);
        } catch (IOException ex) {
            throw new BusinessException("FILE_SAVE_FAILED", "文件保存失败");
        }

        return toStoredFile(filename, targetFile);
    }

    @Override
    public void delete(String storagePath) {
        if (!StringUtils.hasText(storagePath)) {
            return;
        }

        Path targetFile = Path.of(storagePath).toAbsolutePath().normalize();
        if (!targetFile.startsWith(basePath)) {
            throw new BusinessException("INVALID_FILE_PATH", "文件路径非法");
        }

        try {
            Files.deleteIfExists(targetFile);
        } catch (IOException ex) {
            throw new BusinessException("FILE_DELETE_FAILED", "文件删除失败");
        }
    }

    private Path resolveTargetFile(String folder, String filename) {
        String datePath = LocalDate.now().toString().replace("-", "/");
        Path targetDir = basePath.resolve(folder).resolve(datePath).normalize();
        Path targetFile = targetDir.resolve(filename).normalize();

        if (!targetFile.startsWith(basePath)) {
            throw new BusinessException("INVALID_FILE_PATH", "文件路径非法");
        }
        return targetFile;
    }

    private StoredFile toStoredFile(String filename, Path targetFile) {
        String relativePath = basePath.relativize(targetFile).toString().replace("\\", "/");
        return new StoredFile(filename, targetFile.toString(), publicBaseUrl + "/" + relativePath);
    }

    private String resolveExtension(String filename, String contentType) {
        if (StringUtils.hasText(filename)) {
            String cleanFilename = StringUtils.cleanPath(filename).toLowerCase(Locale.ROOT);
            int dotIndex = cleanFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < cleanFilename.length() - 1) {
                return cleanFilename.substring(dotIndex);
            }
        }

        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return ".jpg";
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        return "";
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
