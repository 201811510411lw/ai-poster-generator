package com.aiposter.poster;

import com.aiposter.asset.AssetEntity;
import com.aiposter.poster.dto.GeneratePosterRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class PosterPromptBuilder {
    public String build(GeneratePosterRequest request, List<AssetEntity> assets) {
        PromptTemplate template = PromptTemplate.from(request.getPromptTemplate());
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a GPT-Image2 industrial prompt engine for a Chinese AI poster generator.\n");
        prompt.append("Use Prompt-as-Code structure: subject, layout, hierarchy, typography, color, assets, constraints, and output quality.\n");
        prompt.append("The final image must be a finished marketing design, not a wireframe, moodboard, process sheet, or template preview.\n");
        prompt.append("Keep all Chinese text clean, readable, and faithful to the user content. Do not invent unrelated random text.\n\n");

        prompt.append("Selected template: ").append(template.label).append("\n");
        prompt.append("Template goal: ").append(template.goal).append("\n\n");

        appendUserBrief(prompt, request);
        appendAssetBrief(prompt, assets);
        appendTemplateInstructions(prompt, template, request);
        appendCommonConstraints(prompt);

        return prompt.toString();
    }

    private void appendUserBrief(StringBuilder prompt, GeneratePosterRequest request) {
        prompt.append("User brief:\n");
        appendLine(prompt, "- Poster type", request.getMaterialType());
        appendLine(prompt, "- Canvas", request.getWidth() + " x " + request.getHeight());
        appendLine(prompt, "- Main color", request.getMainColor());
        appendLine(prompt, "- Secondary color", request.getSubColor());
        appendLine(prompt, "- Brand", request.getBrandDescription());
        appendLine(prompt, "- Visual style", request.getStyleDescription());
        appendLine(prompt, "- Title", request.getTitle());
        appendLine(prompt, "- Subtitle", request.getSubtitle());
        appendLine(prompt, "- Activity / offer", request.getActivityDescription());
        appendLine(prompt, "- Design requirement", request.getDesignRequirement());
        prompt.append("\n");
    }

    private void appendAssetBrief(StringBuilder prompt, List<AssetEntity> assets) {
        if (assets == null || assets.isEmpty()) {
            prompt.append("Reference assets: none. Create the visual direction from the user brief.\n\n");
            return;
        }

        prompt.append("Reference assets selected by user:\n");
        String assetSummary = assets.stream()
                .map(asset -> "- " + asset.getAssetType() + ": " + asset.getOriginalFilename()
                        + optionalSize(asset))
                .collect(Collectors.joining("\n"));
        prompt.append(assetSummary).append("\n");
        prompt.append("Use the selected assets as semantic references for subject, product, logo, decoration, background, and visual direction.\n");
        prompt.append("Do not treat file names as visible poster copy unless the user explicitly asks for it.\n\n");
    }

    private void appendTemplateInstructions(StringBuilder prompt, PromptTemplate template, GeneratePosterRequest request) {
        switch (template) {
            case COMMERCIAL_POSTER -> appendCommercialPosterTemplate(prompt);
            case PRODUCT_CAMPAIGN -> appendProductCampaignTemplate(prompt);
            case TYPOGRAPHY_POSTER -> appendTypographyPosterTemplate(prompt, request);
            case INFOGRAPHIC_POSTER -> appendInfographicPosterTemplate(prompt);
            case SOCIAL_MEDIA_POSTER -> appendSocialMediaPosterTemplate(prompt);
        }
    }

    private void appendCommercialPosterTemplate(StringBuilder prompt) {
        prompt.append("Template instructions - Commercial poster:\n");
        prompt.append("- Design one polished activity, product, or campaign poster.\n");
        prompt.append("- Main visual: create a clear hero subject related to the product, activity, or brand.\n");
        prompt.append("- Layout: use a strong poster composition such as centered, left-aligned, diagonal, or top-title/bottom-product.\n");
        prompt.append("- Hierarchy: title first, subtitle second, offer/activity third, brand/supporting details last.\n");
        prompt.append("- Color: build a coherent palette from the main and secondary colors.\n");
        prompt.append("- Output: high-resolution Chinese marketing poster suitable for web and social sharing.\n\n");
    }

    private void appendProductCampaignTemplate(StringBuilder prompt) {
        prompt.append("Template instructions - Product campaign / e-commerce:\n");
        prompt.append("- Create a commercial product campaign poster with the product as the strongest visual anchor.\n");
        prompt.append("- Use exaggerated product scale, clean studio lighting, promotional labels, price-tag energy, or shelf/display context when relevant.\n");
        prompt.append("- Structure selling points into 2-4 short readable blocks; keep copy concise and aligned.\n");
        prompt.append("- The image should feel like a finished retail/e-commerce advertisement, not a generic illustration.\n");
        prompt.append("- Avoid cluttered collage, unreadable small text, fake QR codes, and unrelated discount claims.\n\n");
    }

    private void appendTypographyPosterTemplate(StringBuilder prompt, GeneratePosterRequest request) {
        prompt.append("Template instructions - Conceptual typography poster:\n");
        prompt.append("- Create one premium conceptual typography poster for the exact title.\n");
        appendExactText(prompt, "Exact title", request.getTitle());
        appendExactText(prompt, "Exact subtitle", request.getSubtitle());
        prompt.append("- Typography is the hero: huge, readable, powerful, and intentionally designed.\n");
        prompt.append("- Do not translate, shorten, replace, or misspell the title. Do not add other large readable text.\n");
        prompt.append("- Turn the title meaning, mood, symbolic associations, and visual rhythm into one strong visual metaphor.\n");
        prompt.append("- Use restrained 4-6 color system, intelligent whitespace, editorial poster composition, and refined texture.\n");
        prompt.append("- Avoid generic word art, glossy 3D fonts, random icons, cluttered collage, official logos, and copied campaign slogans.\n\n");
    }

    private void appendInfographicPosterTemplate(StringBuilder prompt) {
        prompt.append("Template instructions - Infographic poster:\n");
        prompt.append("- Generate a readable Chinese infographic poster around the user's topic or campaign.\n");
        prompt.append("- Structure: title area plus 3-5 modules. Each module should have an icon/visual cue, short title, and 1-2 concise lines.\n");
        prompt.append("- Use arrows, separators, numbered steps, or color-coded blocks only when they clarify the logic.\n");
        prompt.append("- Keep information hierarchy clean; avoid long paragraphs and excessive modules.\n");
        prompt.append("- Output should feel like a professional explainer poster, not a dense report page.\n\n");
    }

    private void appendSocialMediaPosterTemplate(StringBuilder prompt) {
        prompt.append("Template instructions - Social media poster:\n");
        prompt.append("- Create a high-impact poster optimized for mobile social sharing.\n");
        prompt.append("- Use a strong hook, bold headline, compact supporting text, and clear focal image.\n");
        prompt.append("- Layout should be readable at phone size, with large typography and generous spacing.\n");
        prompt.append("- Add platform-friendly visual rhythm, stickers, labels, or badges only if they support the message.\n");
        prompt.append("- Avoid tiny unreadable copy, mixed platform UI screenshots, and irrelevant decorative noise.\n\n");
    }

    private void appendCommonConstraints(StringBuilder prompt) {
        prompt.append("Global constraints:\n");
        prompt.append("- Single finished image only. No moodboard, grid board, mockup sheet, captions about the prompt, or process labels.\n");
        prompt.append("- Chinese text must be accurate, clean, readable, and limited to the user-provided copy or concise supporting labels.\n");
        prompt.append("- Maintain strong hierarchy, spacing, modern typography, and brand-consistent colors.\n");
        prompt.append("- Use product/campaign imagery as the center focus when relevant.\n");
        prompt.append("- Do not include watermarks, random logos, gibberish, misspelled text, or unrelated elements.\n");
        prompt.append("- The result should be suitable for display inside a web poster generator product.\n");
    }

    private void appendLine(StringBuilder prompt, String label, String value) {
        if (StringUtils.hasText(value)) {
            prompt.append(label).append(": ").append(value.trim()).append("\n");
        }
    }

    private void appendLine(StringBuilder prompt, String label, Integer value) {
        if (value != null) {
            prompt.append(label).append(": ").append(value).append("\n");
        }
    }

    private void appendExactText(StringBuilder prompt, String label, String value) {
        if (StringUtils.hasText(value)) {
            prompt.append("- ").append(label).append(": \"").append(value.trim()).append("\"\n");
        }
    }

    private String optionalSize(AssetEntity asset) {
        if (asset.getWidth() == null || asset.getHeight() == null) {
            return "";
        }
        return " (" + asset.getWidth() + "x" + asset.getHeight() + ")";
    }

    private enum PromptTemplate {
        COMMERCIAL_POSTER("commercial-poster", "通用商业海报", "Build a balanced campaign poster from brand, offer, layout, color, and hierarchy."),
        PRODUCT_CAMPAIGN("product-campaign", "产品促销/电商海报", "Make the product and selling points the core of the commercial visual."),
        TYPOGRAPHY_POSTER("typography-poster", "概念字体海报", "Use the exact title as the main visual structure and conceptual metaphor."),
        INFOGRAPHIC_POSTER("infographic-poster", "信息图海报", "Turn the content into readable modules, icons, arrows, and clear information hierarchy."),
        SOCIAL_MEDIA_POSTER("social-media-poster", "社媒传播海报", "Optimize the poster for mobile social feeds with a bold hook and compact copy.");

        private final String code;
        private final String label;
        private final String goal;

        PromptTemplate(String code, String label, String goal) {
            this.code = code;
            this.label = label;
            this.goal = goal;
        }

        private static PromptTemplate from(String code) {
            if (!StringUtils.hasText(code)) {
                return COMMERCIAL_POSTER;
            }
            String normalized = code.trim().toLowerCase(Locale.ROOT);
            for (PromptTemplate template : values()) {
                if (template.code.equals(normalized)) {
                    return template;
                }
            }
            return COMMERCIAL_POSTER;
        }
    }
}
