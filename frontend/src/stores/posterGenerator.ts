import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { deletePosterAsset, generatePoster, uploadPosterAsset } from "@/api/poster";
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
  const mainColor = ref("#F58220");
  const subColor = ref("#FFD43B");
  const brandDescription = ref("正品量贩，便宜又好");
  const styleDescription = ref("橙色门店风，高饱和促销感，突出价格和爆款零食");

  const assets = ref<Asset[]>([]);

  const materialType = ref<MaterialType>("poster");
  const width = ref(1080);
  const height = ref(1920);
  const title = ref("爆款零食限时特价");
  const subtitle = ref("全场好货，门店会员日超值购");
  const activityDescription = ref("会员日满 29 减 5");
  const designRequirement = ref("参考零食量贩门店橙色视觉，突出促销氛围、价格标签和爆款零食组合");
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

  async function removeAsset(assetId: string) {
    await deletePosterAsset(assetId);
    removeAssetFromState(assetId);
  }

  function removeAssetFromState(assetId: string) {
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
