package com.aiposter.poster;

import com.aiposter.asset.AssetEntity;
import com.aiposter.asset.AssetRepository;
import com.aiposter.common.BusinessException;
import com.aiposter.openai.OpenAiImageClient;
import com.aiposter.poster.dto.GeneratePosterRequest;
import com.aiposter.poster.dto.GeneratePosterResponse;
import com.aiposter.poster.dto.PosterHistoryItemResponse;
import com.aiposter.storage.StorageService;
import com.aiposter.storage.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PosterService {
    private static final Logger log = LoggerFactory.getLogger(PosterService.class);
    private static final Set<String> ALLOWED_OUTPUT_FORMATS = Set.of("png", "jpg");
    private static final Map<String, String> MODEL_SIZE_BY_MATERIAL_TYPE = Map.of(
            "main", "1024x1024",
            "poster", "1024x1536",
            "rollup", "1024x1536",
            "tv", "1536x1024",
            "flag", "1536x1024"
    );

    private final PosterTaskRepository posterTaskRepository;
    private final PosterGenerationAssetRepository posterGenerationAssetRepository;
    private final AssetRepository assetRepository;
    private final PosterPromptBuilder promptBuilder;
    private final OpenAiImageClient openAiImageClient;
    private final StorageService storageService;

    public PosterService(
            PosterTaskRepository posterTaskRepository,
            PosterGenerationAssetRepository posterGenerationAssetRepository,
            AssetRepository assetRepository,
            PosterPromptBuilder promptBuilder,
            OpenAiImageClient openAiImageClient,
            StorageService storageService) {
        this.posterTaskRepository = posterTaskRepository;
        this.posterGenerationAssetRepository = posterGenerationAssetRepository;
        this.assetRepository = assetRepository;
        this.promptBuilder = promptBuilder;
        this.openAiImageClient = openAiImageClient;
        this.storageService = storageService;
    }

    public GeneratePosterResponse generate(Long userId, GeneratePosterRequest request) {
        validateRequest(request);

        List<AssetEntity> selectedAssets = resolveSelectedAssets(userId, request.getAssetIds());
        String prompt = promptBuilder.build(request, selectedAssets);

        PosterTaskEntity task = createPendingTask(userId, request, prompt);
        saveTaskAssets(task.getId(), selectedAssets);

        try {
            String modelSize = resolveModelSize(request);
            String imageBase64 = openAiImageClient.generateImageBase64(prompt, modelSize);
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            StoredFile storedFile = storageService.store(imageBytes, "poster-" + task.getId() + ".png", "image/png", "generated-posters");

            task.setStatus(PosterTaskStatus.SUCCESS);
            task.setResultFilename(storedFile.getFilename());
            task.setResultStoragePath(storedFile.getStoragePath());
            task.setResultImageUrl(storedFile.getAccessUrl());
            posterTaskRepository.save(task);

            log.info("海报生成成功: userId={}, taskId={}, modelSize={}", userId, task.getId(), modelSize);
            return new GeneratePosterResponse(task.getId(), "success", task.getResultImageUrl(), request.getWidth(), request.getHeight());
        } catch (RuntimeException ex) {
            task.setStatus(PosterTaskStatus.FAILED);
            task.setErrorMessage(resolveErrorMessage(ex));
            posterTaskRepository.save(task);
            log.warn("海报生成失败: userId={}, taskId={}, error={}", userId, task.getId(), ex.getMessage());
            throw ex;
        }
    }

    public List<PosterHistoryItemResponse> listHistory(Long userId) {
        return posterTaskRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toHistoryItem)
                .toList();
    }

    private PosterTaskEntity createPendingTask(Long userId, GeneratePosterRequest request, String prompt) {
        PosterTaskEntity task = new PosterTaskEntity();
        task.setUserId(userId);
        task.setMaterialType(defaultString(request.getMaterialType(), "poster"));
        task.setWidth(request.getWidth());
        task.setHeight(request.getHeight());
        task.setMainColor(request.getMainColor());
        task.setSubColor(request.getSubColor());
        task.setBrandDescription(request.getBrandDescription());
        task.setStyleDescription(request.getStyleDescription());
        task.setTitle(request.getTitle());
        task.setSubtitle(request.getSubtitle());
        task.setActivityDescription(request.getActivityDescription());
        task.setDesignRequirement(request.getDesignRequirement());
        task.setOutputFormat(defaultString(request.getOutputFormat(), "png"));
        task.setPromptText(prompt);
        task.setStatus(PosterTaskStatus.PENDING);
        return posterTaskRepository.save(task);
    }

    private void saveTaskAssets(Long taskId, List<AssetEntity> assets) {
        List<PosterGenerationAssetEntity> relations = new ArrayList<>();
        for (int i = 0; i < assets.size(); i++) {
            AssetEntity asset = assets.get(i);
            PosterGenerationAssetEntity relation = new PosterGenerationAssetEntity();
            relation.setTaskId(taskId);
            relation.setAssetId(asset.getId());
            relation.setAssetRole(asset.getAssetType());
            relation.setSortOrder(i);
            relations.add(relation);
        }
        posterGenerationAssetRepository.saveAll(relations);
    }

    private List<AssetEntity> resolveSelectedAssets(Long userId, List<Long> assetIds) {
        if (assetIds == null || assetIds.isEmpty()) {
            return List.of();
        }

        List<Long> uniqueAssetIds = assetIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (uniqueAssetIds.isEmpty()) {
            return List.of();
        }

        List<AssetEntity> assets = assetRepository.findByUserIdAndIdIn(userId, uniqueAssetIds);
        Set<Long> foundIds = assets.stream().map(AssetEntity::getId).collect(Collectors.toSet());
        Set<Long> requestedIds = new HashSet<>(uniqueAssetIds);
        if (!foundIds.containsAll(requestedIds)) {
            throw new BusinessException("ASSET_NOT_FOUND", "存在无权使用或不存在的素材");
        }
        return uniqueAssetIds.stream()
                .map(id -> assets.stream()
                        .filter(asset -> id.equals(asset.getId()))
                        .findFirst()
                        .orElseThrow())
                .toList();
    }

    private PosterHistoryItemResponse toHistoryItem(PosterTaskEntity task) {
        List<Long> assetIds = posterGenerationAssetRepository.findByTaskIdOrderBySortOrderAsc(task.getId())
                .stream()
                .map(PosterGenerationAssetEntity::getAssetId)
                .toList();

        return new PosterHistoryItemResponse(
                task.getId(),
                task.getTitle(),
                task.getSubtitle(),
                task.getStatus().name().toLowerCase(),
                task.getResultImageUrl(),
                task.getWidth(),
                task.getHeight(),
                task.getMaterialType(),
                assetIds,
                task.getErrorMessage(),
                task.getCreatedAt()
        );
    }

    private void validateRequest(GeneratePosterRequest request) {
        if (request == null) {
            throw new BusinessException("INVALID_REQUEST", "生成参数不能为空");
        }
        if (request.getWidth() == null || request.getWidth() <= 0 || request.getHeight() == null || request.getHeight() <= 0) {
            throw new BusinessException("INVALID_SIZE", "海报尺寸不合法");
        }
        if (!StringUtils.hasText(request.getTitle()) && !StringUtils.hasText(request.getSubtitle()) && !StringUtils.hasText(request.getDesignRequirement())) {
            throw new BusinessException("EMPTY_POSTER_CONTENT", "请至少填写标题、副标题或设计要求");
        }
        String outputFormat = defaultString(request.getOutputFormat(), "png");
        if (!ALLOWED_OUTPUT_FORMATS.contains(outputFormat)) {
            throw new BusinessException("INVALID_OUTPUT_FORMAT", "输出格式不合法");
        }
    }

    private String resolveModelSize(GeneratePosterRequest request) {
        String materialType = defaultString(request.getMaterialType(), "poster");
        if (MODEL_SIZE_BY_MATERIAL_TYPE.containsKey(materialType)) {
            return MODEL_SIZE_BY_MATERIAL_TYPE.get(materialType);
        }
        if (request.getWidth() != null && request.getHeight() != null) {
            if (request.getWidth() > request.getHeight()) {
                return "1536x1024";
            }
            if (request.getHeight() > request.getWidth()) {
                return "1024x1536";
            }
        }
        return "1024x1024";
    }

    private String resolveErrorMessage(RuntimeException ex) {
        if (ex instanceof BusinessException businessException) {
            return businessException.getMessage();
        }
        return "海报生成失败，请稍后重试";
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }
}
