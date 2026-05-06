package com.aiposter.asset;

import com.aiposter.asset.dto.AssetUploadResponse;
import com.aiposter.common.BusinessException;
import com.aiposter.poster.PosterGenerationAssetRepository;
import com.aiposter.storage.StorageService;
import com.aiposter.storage.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AssetService {
    private static final Logger log = LoggerFactory.getLogger(AssetService.class);
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg");
    private static final Set<String> ALLOWED_ASSET_TYPES = Set.of(
            "product", "logo", "decoration", "background", "reference", "other"
    );
    private static final String ASSET_STORAGE_FOLDER = "ai-poster-generator-image";

    private final AssetRepository assetRepository;
    private final PosterGenerationAssetRepository posterGenerationAssetRepository;
    private final StorageService storageService;

    public AssetService(
            AssetRepository assetRepository,
            PosterGenerationAssetRepository posterGenerationAssetRepository,
            StorageService storageService) {
        this.assetRepository = assetRepository;
        this.posterGenerationAssetRepository = posterGenerationAssetRepository;
        this.storageService = storageService;
    }

    @Transactional
    public AssetUploadResponse upload(Long userId, String assetType, MultipartFile file) {
        validateAssetType(assetType);
        validateFile(file);

        ImageSize imageSize = readImageSize(file);
        StoredFile storedFile = storageService.store(file, ASSET_STORAGE_FOLDER);

        try {
            AssetEntity asset = new AssetEntity();
            asset.setUserId(userId);
            asset.setAssetType(assetType);
            asset.setFilename(storedFile.getFilename());
            asset.setOriginalFilename(resolveOriginalFilename(file, storedFile));
            asset.setContentType(file.getContentType());
            asset.setFileSize(file.getSize());
            asset.setWidth(imageSize.width());
            asset.setHeight(imageSize.height());
            asset.setStoragePath(storedFile.getStoragePath());
            asset.setAccessUrl(storedFile.getAccessUrl());

            AssetEntity saved = assetRepository.save(asset);
            log.info("素材上传成功: userId={}, assetId={}, assetType={}, filename={}", userId, saved.getId(), assetType, saved.getFilename());
            return toResponse(saved);
        } catch (RuntimeException ex) {
            safeDeleteStoredFile(storedFile);
            throw ex;
        }
    }

    public List<AssetUploadResponse> listByUser(Long userId) {
        return assetRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AssetUploadResponse updateAssetType(Long userId, Long assetId, String assetType) {
        validateAssetType(assetType);
        AssetEntity asset = findOwnedAsset(userId, assetId);
        asset.setAssetType(assetType);
        AssetEntity saved = assetRepository.save(asset);
        log.info("素材类型更新成功: userId={}, assetId={}, assetType={}", userId, assetId, assetType);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long userId, Long assetId) {
        AssetEntity asset = findOwnedAsset(userId, assetId);
        posterGenerationAssetRepository.deleteByAssetId(assetId);
        assetRepository.delete(asset);
        assetRepository.flush();
        storageService.delete(asset.getStoragePath());
        log.info("素材删除成功: userId={}, assetId={}, filename={}", userId, assetId, asset.getFilename());
    }

    private AssetEntity findOwnedAsset(Long userId, Long assetId) {
        AssetEntity asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new BusinessException("ASSET_NOT_FOUND", "素材不存在"));

        if (!userId.equals(asset.getUserId())) {
            throw new BusinessException("ASSET_FORBIDDEN", "无权操作该素材");
        }
        return asset;
    }

    private void validateAssetType(String assetType) {
        if (!StringUtils.hasText(assetType) || !ALLOWED_ASSET_TYPES.contains(assetType)) {
            throw new BusinessException("INVALID_ASSET_TYPE", "素材类型不合法");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("EMPTY_FILE", "上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("FILE_TOO_LARGE", "单个素材不能超过 20MB");
        }
        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("UNSUPPORTED_FILE_TYPE", "仅支持 PNG、JPG、JPEG 图片");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("UNSUPPORTED_FILE_TYPE", "仅支持 PNG、JPG、JPEG 图片");
        }
    }

    private ImageSize readImageSize(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new BusinessException("INVALID_IMAGE", "图片文件无效");
            }
            return new ImageSize(image.getWidth(), image.getHeight());
        } catch (IOException ex) {
            throw new BusinessException("INVALID_IMAGE", "图片读取失败");
        }
    }

    private String resolveOriginalFilename(MultipartFile file, StoredFile storedFile) {
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return storedFile.getFilename();
        }
        return StringUtils.cleanPath(originalFilename);
    }

    private String resolveExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        String cleanFilename = StringUtils.cleanPath(filename).toLowerCase(Locale.ROOT);
        int dotIndex = cleanFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleanFilename.length() - 1) {
            return "";
        }
        return cleanFilename.substring(dotIndex);
    }

    private void safeDeleteStoredFile(StoredFile storedFile) {
        try {
            storageService.delete(storedFile.getStoragePath());
        } catch (RuntimeException cleanupError) {
            log.warn("素材数据库保存失败后清理文件失败: storagePath={}", storedFile.getStoragePath(), cleanupError);
        }
    }

    private AssetUploadResponse toResponse(AssetEntity asset) {
        return new AssetUploadResponse(
                asset.getId(),
                asset.getAssetType(),
                asset.getFilename(),
                asset.getOriginalFilename(),
                asset.getContentType(),
                asset.getFileSize(),
                asset.getWidth(),
                asset.getHeight(),
                asset.getAccessUrl(),
                asset.getCreatedAt()
        );
    }

    private record ImageSize(Integer width, Integer height) {
    }
}
