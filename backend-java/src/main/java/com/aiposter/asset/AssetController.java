package com.aiposter.asset;

import com.aiposter.asset.dto.AssetUploadResponse;
import com.aiposter.common.ApiResponse;
import com.aiposter.security.LoginUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping("/upload")
    public ApiResponse<AssetUploadResponse> upload(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam("assetType") String assetType,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok("素材上传成功", assetService.upload(loginUser.getId(), assetType, file));
    }

    @GetMapping
    public ApiResponse<List<AssetUploadResponse>> list(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.ok(assetService.listByUser(loginUser.getId()));
    }
}
