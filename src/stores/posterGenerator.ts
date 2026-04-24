import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { generatePoster, uploadPosterAsset } from "@/api/poster";
import { materialSizeMap } from "@/utils/constants";
import type {
  Asset,
  AssetType,
  GeneratePosterPayload,
  GenerationStatus,
  MaterialType,
  OutputFormat,
} from "@/types/poster";

export const usePosterGeneratorStore = defineStore("poster-generator", () => {
  const mainColor = ref("#E60012");
  const subColor = ref("#FFD700");
  const brandDescription = ref("");
  const styleDescription = ref("");

  const assets = ref<Asset[]>([]);

  const materialType = ref<MaterialType>("poster");
  const width = ref(1080);
  const height = ref(1920);
  const title = ref("");
  const subtitle = ref("");
  const activityDescription = ref("");
  const designRequirement = ref("");
  const outputFormat = ref<OutputFormat>("png");

  const generationStatus = ref<GenerationStatus>("idle");
  const generatedImageUrl = ref<string | null>(null);
  const errorMessage = ref<string | null>(null);

  const canGenerate = computed(() => {
    return width.value > 0 && height.value > 0 && !!(title.value || subtitle.value || designRequirement.value);
  });

  function setMaterialType(type: MaterialType) {
    materialType.value = type;
    if (type !== "custom") {
      width.value = materialSizeMap[type].width;
      height.value = materialSizeMap[type].height;
    }
  }

  async function addAsset(file: File, assetType: AssetType) {
    const response = await uploadPosterAsset({ file, assetType });

    assets.value.push({
      id: response.assetId,
      file,
      url: response.url,
      filename: file.name,
      assetType,
      width: response.width,
      height: response.height,
      size: file.size,
    });
  }

  function updateAssetType(assetId: string, nextType: AssetType) {
    const target = assets.value.find((item) => item.id === assetId);
    if (target) {
      target.assetType = nextType;
    }
  }

  function removeAsset(assetId: string) {
    const index = assets.value.findIndex((item) => item.id === assetId);
    if (index >= 0) {
      const [removed] = assets.value.splice(index, 1);
      if (removed?.url.startsWith("blob:")) {
        URL.revokeObjectURL(removed.url);
      }
    }
  }

  async function generate() {
    generationStatus.value = "generating";
    errorMessage.value = null;

    try {
      const payload: GeneratePosterPayload = {
        materialType: materialType.value,
        width: width.value,
        height: height.value,
        mainColor: mainColor.value,
        subColor: subColor.value,
        brandDescription: brandDescription.value,
        styleDescription: styleDescription.value,
        title: title.value,
        subtitle: subtitle.value,
        activityDescription: activityDescription.value,
        designRequirement: designRequirement.value,
        outputFormat: outputFormat.value,
        assetIds: assets.value.map((asset) => asset.id),
      };

      const response = await generatePoster(payload);
      generatedImageUrl.value = response.imageUrl;
      width.value = response.width;
      height.value = response.height;
      generationStatus.value = "success";
    } catch (error) {
      generationStatus.value = "error";
      errorMessage.value = error instanceof Error ? error.message : "生成失败，请稍后重试";
    }
  }

  return {
    mainColor,
    subColor,
    brandDescription,
    styleDescription,
    assets,
    materialType,
    width,
    height,
    title,
    subtitle,
    activityDescription,
    designRequirement,
    outputFormat,
    generationStatus,
    generatedImageUrl,
    errorMessage,
    canGenerate,
    setMaterialType,
    addAsset,
    updateAssetType,
    removeAsset,
    generate,
  };
});
