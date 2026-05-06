import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { REQUEST_CANCELED_MESSAGE } from "@/api/request";
import { deletePosterAsset, generatePoster, listPosterAssets, listPosterHistory, updatePosterAssetType, uploadPosterAsset } from "@/api/poster";
import { materialSizeMap } from "@/utils/constants";
import type {
  Asset,
  AssetType,
  GeneratePosterPayload,
  GenerationStatus,
  MaterialType,
  OutputFormat,
  PosterHistoryItem,
} from "@/types/poster";

export const usePosterGeneratorStore = defineStore("poster-generator", () => {
  const mainColor = ref("#F58220");
  const subColor = ref("#FFD43B");
  const brandDescription = ref("正品量贩，便宜又好");
  const styleDescription = ref("橙色门店风，高饱和促销感，突出价格和爆款零食");

  const assets = ref<Asset[]>([]);
  const selectedAssetIds = ref<string[]>([]);
  const assetsLoaded = ref(false);
  const assetsLoading = ref(false);

  const history = ref<PosterHistoryItem[]>([]);
  const historyLoaded = ref(false);
  const historyLoading = ref(false);

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
  const generationAbortController = ref<AbortController | null>(null);

  const selectedAssets = computed(() => {
    const selectedSet = new Set(selectedAssetIds.value);
    return assets.value.filter((asset) => selectedSet.has(asset.id));
  });

  const hasPosterContent = computed(() => {
    return !!(title.value?.trim() || subtitle.value?.trim() || designRequirement.value?.trim());
  });

  const isGenerationRequestActive = computed(() => {
    return generationStatus.value === "generating" && !!generationAbortController.value;
  });

  const canGenerate = computed(() => {
    return !isGenerationRequestActive.value
      && width.value > 0
      && height.value > 0
      && hasPosterContent.value
      && selectedAssetIds.value.length > 0;
  });

  function setMaterialType(type: MaterialType) {
    materialType.value = type;
    if (type !== "custom") {
      width.value = materialSizeMap[type].width;
      height.value = materialSizeMap[type].height;
    }
  }

  async function loadAssets(force = false) {
    if (assetsLoaded.value && !force) {
      return;
    }

    assetsLoading.value = true;
    try {
      assets.value = await listPosterAssets();
      const existingAssetIds = new Set(assets.value.map((asset) => asset.id));
      selectedAssetIds.value = selectedAssetIds.value.filter((assetId) => existingAssetIds.has(assetId));
      assetsLoaded.value = true;
    } finally {
      assetsLoading.value = false;
    }
  }

  async function loadHistory(force = false) {
    if (historyLoaded.value && !force) {
      return;
    }

    historyLoading.value = true;
    try {
      history.value = await listPosterHistory();
      historyLoaded.value = true;
    } finally {
      historyLoading.value = false;
    }
  }

  async function addAsset(file: File, assetType: AssetType) {
    const response = await uploadPosterAsset({ file, assetType });

    const asset: Asset = {
      id: response.assetId,
      file,
      url: response.url,
      filename: file.name,
      assetType,
      width: response.width,
      height: response.height,
      size: file.size,
    };

    assets.value.unshift(asset);
    selectAsset(asset.id);
  }

  async function updateAssetType(assetId: string, nextType: AssetType) {
    const target = assets.value.find((item) => item.id === assetId);
    if (!target || target.assetType === nextType) {
      return;
    }

    const previousType = target.assetType;
    target.assetType = nextType;

    try {
      const updatedAsset = await updatePosterAssetType(assetId, nextType);
      Object.assign(target, updatedAsset);
    } catch (error) {
      target.assetType = previousType;
      throw error;
    }
  }

  function isAssetSelected(assetId: string) {
    return selectedAssetIds.value.includes(assetId);
  }

  function selectAsset(assetId: string) {
    if (!selectedAssetIds.value.includes(assetId)) {
      selectedAssetIds.value.push(assetId);
    }
  }

  function toggleAssetSelection(assetId: string) {
    if (selectedAssetIds.value.includes(assetId)) {
      selectedAssetIds.value = selectedAssetIds.value.filter((item) => item !== assetId);
    } else {
      selectedAssetIds.value.push(assetId);
    }
  }

  function clearSelectedAssets() {
    selectedAssetIds.value = [];
  }

  async function removeAsset(assetId: string) {
    await deletePosterAsset(assetId);
    removeAssetFromState(assetId);
  }

  function removeAssetFromState(assetId: string) {
    const index = assets.value.findIndex((item) => item.id === assetId);
    if (index >= 0) {
      const [removed] = assets.value.splice(index, 1);
      selectedAssetIds.value = selectedAssetIds.value.filter((item) => item !== assetId);
      if (removed?.url.startsWith("blob:")) {
        URL.revokeObjectURL(removed.url);
      }
    }
  }

  async function generate() {
    if (isGenerationRequestActive.value) {
      return;
    }

    if (selectedAssetIds.value.length === 0) {
      errorMessage.value = "请先选择至少 1 个素材再生成";
      throw new Error(errorMessage.value);
    }

    if (!hasPosterContent.value) {
      errorMessage.value = "请至少填写标题、副标题或设计要求";
      throw new Error(errorMessage.value);
    }

    const abortController = new AbortController();
    generationAbortController.value = abortController;
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
        assetIds: selectedAssetIds.value,
      };

      const response = await generatePoster(payload, abortController.signal);
      generatedImageUrl.value = response.imageUrl;
      width.value = response.width;
      height.value = response.height;
      generationStatus.value = "success";
      await loadHistory(true);
    } catch (error) {
      if (error instanceof Error && error.message === REQUEST_CANCELED_MESSAGE) {
        generationStatus.value = "canceled";
        errorMessage.value = null;
        return;
      }

      generationStatus.value = "error";
      errorMessage.value = error instanceof Error ? error.message : "生成失败，请稍后重试";
      throw error;
    } finally {
      if (generationAbortController.value === abortController) {
        generationAbortController.value = null;
      }

      if (generationStatus.value === "generating") {
        generationStatus.value = generatedImageUrl.value ? "success" : "idle";
      }
    }
  }

  function cancelGeneration() {
    if (!isGenerationRequestActive.value) {
      return false;
    }

    generationAbortController.value?.abort();
    generationStatus.value = "canceled";
    errorMessage.value = null;
    generationAbortController.value = null;
    return true;
  }

  return {
    mainColor,
    subColor,
    brandDescription,
    styleDescription,
    assets,
    selectedAssetIds,
    selectedAssets,
    assetsLoaded,
    assetsLoading,
    history,
    historyLoaded,
    historyLoading,
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
    isGenerationRequestActive,
    setMaterialType,
    loadAssets,
    loadHistory,
    addAsset,
    updateAssetType,
    isAssetSelected,
    selectAsset,
    toggleAssetSelection,
    clearSelectedAssets,
    removeAsset,
    generate,
    cancelGeneration,
  };
});
