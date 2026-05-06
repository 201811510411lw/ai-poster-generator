package com.aiposter.poster;

import com.aiposter.asset.AssetEntity;
import com.aiposter.poster.dto.GeneratePosterRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PosterPromptBuilder {
    public String build(GeneratePosterRequest request, List<AssetEntity> assets) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Create a polished commercial Chinese poster.\n");
        prompt.append("The final image should look like a finished marketing design, not a wireframe.\n");
        prompt.append("Keep all Chinese text clean, readable, and faithful to the user content. Do not invent extra random text.\n\n");

        appendLine(prompt, "Poster type", request.getMaterialType());
        appendLine(prompt, "Canvas", request.getWidth() + " x " + request.getHeight());
        appendLine(prompt, "Main color", request.getMainColor());
        appendLine(prompt, "Secondary color", request.getSubColor());
        appendLine(prompt, "Brand", request.getBrandDescription());
        appendLine(prompt, "Visual style", request.getStyleDescription());
        appendLine(prompt, "Title", request.getTitle());
        appendLine(prompt, "Subtitle", request.getSubtitle());
        appendLine(prompt, "Activity / offer", request.getActivityDescription());
        appendLine(prompt, "Design requirement", request.getDesignRequirement());

        if (assets != null && !assets.isEmpty()) {
            prompt.append("\nUser selected reference assets:\n");
            String assetSummary = assets.stream()
                    .map(asset -> "- " + asset.getAssetType() + ": " + asset.getOriginalFilename()
                            + optionalSize(asset))
                    .collect(Collectors.joining("\n"));
            prompt.append(assetSummary).append("\n");
            prompt.append("Use these assets as semantic references for layout and visual direction.\n");
        }

        prompt.append("\nLayout guidance:\n");
        prompt.append("- Make the title the strongest visual focus.\n");
        prompt.append("- Use product or campaign imagery as the center focus when relevant.\n");
        prompt.append("- Add strong hierarchy, spacing, and modern typography.\n");
        prompt.append("- The result should be suitable for display in a web poster generator product.\n");

        return prompt.toString();
    }

    private void appendLine(StringBuilder prompt, String label, String value) {
        if (StringUtils.hasText(value)) {
            prompt.append(label).append(": ").append(value.trim()).append("\n");
        }
    }

    private String optionalSize(AssetEntity asset) {
        if (asset.getWidth() == null || asset.getHeight() == null) {
            return "";
        }
        return " (" + asset.getWidth() + "x" + asset.getHeight() + ")";
    }
}
