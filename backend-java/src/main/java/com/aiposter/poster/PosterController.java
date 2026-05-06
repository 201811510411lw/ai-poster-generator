package com.aiposter.poster;

import com.aiposter.common.ApiResponse;
import com.aiposter.poster.dto.GeneratePosterRequest;
import com.aiposter.poster.dto.GeneratePosterResponse;
import com.aiposter.poster.dto.PosterHistoryItemResponse;
import com.aiposter.security.LoginUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posters")
public class PosterController {
    private final PosterService posterService;

    public PosterController(PosterService posterService) {
        this.posterService = posterService;
    }

    @PostMapping("/generate")
    public ApiResponse<GeneratePosterResponse> generate(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody GeneratePosterRequest request) {
        return ApiResponse.ok("海报生成成功", posterService.generate(loginUser.getId(), request));
    }

    @GetMapping("/history")
    public ApiResponse<List<PosterHistoryItemResponse>> history(@AuthenticationPrincipal LoginUser loginUser) {
        return ApiResponse.ok(posterService.listHistory(loginUser.getId()));
    }
}
