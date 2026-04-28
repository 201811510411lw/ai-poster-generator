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

function svgDataUrl(svg: string) {
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
}

function createAssetSvg(kind: AssetType, title: string, color = "#F58220") {
  const bg = kind === "background" ? "#FFF3DF" : "#FFFDF9";
  const decoration =
    kind === "background"
      ? `<path d="M0 92 C72 52 132 92 192 55 C256 17 314 42 360 18 L360 260 L0 260 Z" fill="#FFD43B" opacity="0.88"/><path d="M0 132 C82 86 140 126 212 84 C278 45 328 58 360 38 L360 260 L0 260 Z" fill="#F58220" opacity="0.9"/>`
      : kind === "logo"
        ? `<text x="180" y="88" text-anchor="middle" font-size="42" font-weight="900" fill="${color}">零食有鸣</text><text x="180" y="124" text-anchor="middle" font-size="20" font-weight="700" fill="#8A5A2E">便宜又好</text>`
        : kind === "decoration"
          ? `<circle cx="180" cy="98" r="62" fill="${color}"/><text x="180" y="84" text-anchor="middle" font-size="22" font-weight="800" fill="white">限时特价</text><text x="180" y="124" text-anchor="middle" font-size="34" font-weight="900" fill="white">爆款</text>`
          : `<ellipse cx="180" cy="182" rx="104" ry="20" fill="#C98A43" opacity="0.22"/><rect x="105" y="76" width="68" height="92" rx="18" fill="#E53935"/><rect x="172" y="52" width="76" height="122" rx="22" fill="#FFD43B"/><rect x="214" y="88" width="54" height="78" rx="16" fill="#F58220"/><text x="180" y="222" text-anchor="middle" font-size="20" font-weight="900" fill="#3A2A1E">爆款零食</text>`;

  return svgDataUrl(`
    <svg xmlns="http://www.w3.org/2000/svg" width="360" height="260" viewBox="0 0 360 260">
      <rect width="360" height="260" rx="28" fill="${bg}"/>
      ${decoration}
      <text x="24" y="236" font-size="20" font-weight="700" fill="#6B4A2F">${title}</text>
    </svg>
  `);
}

function createDemoPosterSvg() {
  return svgDataUrl(`
    <svg xmlns="http://www.w3.org/2000/svg" width="1080" height="1920" viewBox="0 0 1080 1920">
      <defs>
        <linearGradient id="snackBg" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#FFF1D6"/>
          <stop offset="52%" stop-color="#FFB347"/>
          <stop offset="100%" stop-color="#F58220"/>
        </linearGradient>
        <filter id="shadow" x="-30%" y="-30%" width="160%" height="160%">
          <feDropShadow dx="0" dy="24" stdDeviation="26" flood-color="#5B2D08" flood-opacity="0.24"/>
        </filter>
      </defs>
      <rect width="1080" height="1920" rx="44" fill="url(#snackBg)"/>
      <circle cx="918" cy="178" r="122" fill="#FFFFFF" opacity="0.28"/>
      <circle cx="132" cy="1260" r="210" fill="#FFD43B" opacity="0.36"/>
      <path d="M0 1300 C180 1190 310 1336 480 1234 C670 1120 820 1248 1080 1105 L1080 1920 L0 1920 Z" fill="#FFF8EF" opacity="0.94"/>

      <rect x="88" y="112" width="300" height="82" rx="41" fill="#2F1E12" opacity="0.94"/>
      <text x="238" y="166" text-anchor="middle" font-size="38" font-weight="900" fill="#FFD43B">便宜又好</text>

      <text x="88" y="330" font-size="110" font-weight="900" fill="#2F1E12" letter-spacing="-5">爆款零食</text>
      <text x="88" y="460" font-size="110" font-weight="900" fill="#2F1E12" letter-spacing="-5">限时特价</text>
      <text x="96" y="542" font-size="36" font-weight="800" fill="#6B3A12">全场好货，门店会员日超值购</text>

      <g filter="url(#shadow)">
        <circle cx="246" cy="720" r="132" fill="#E53935"/>
        <text x="246" y="676" text-anchor="middle" font-size="36" font-weight="900" fill="white">会员专享</text>
        <text x="246" y="738" text-anchor="middle" font-size="58" font-weight="900" fill="#FFD43B">满29</text>
        <text x="246" y="798" text-anchor="middle" font-size="58" font-weight="900" fill="#FFD43B">减5</text>
      </g>

      <g filter="url(#shadow)">
        <rect x="560" y="580" width="160" height="310" rx="36" fill="#E53935"/>
        <text x="640" y="735" text-anchor="middle" font-size="52" font-weight="900" fill="white">辣条</text>
        <rect x="700" y="530" width="180" height="360" rx="42" fill="#FFD43B"/>
        <text x="790" y="710" text-anchor="middle" font-size="52" font-weight="900" fill="#2F1E12">薯片</text>
        <rect x="470" y="780" width="190" height="260" rx="40" fill="#FFFFFF" opacity="0.96"/>
        <text x="565" y="920" text-anchor="middle" font-size="48" font-weight="900" fill="#F58220">饼干</text>
        <rect x="642" y="872" width="230" height="300" rx="44" fill="#F58220"/>
        <text x="757" y="1032" text-anchor="middle" font-size="54" font-weight="900" fill="white">坚果</text>
      </g>

      <rect x="86" y="1448" width="908" height="190" rx="38" fill="#2F1E12" opacity="0.94"/>
      <text x="136" y="1535" font-size="46" font-weight="900" fill="#FFD43B">今日门店活动</text>
      <text x="136" y="1608" font-size="34" font-weight="800" fill="white">精选爆品 · 正品量贩 · 超值囤货</text>
      <text x="136" y="1690" font-size="28" font-weight="700" fill="#FFE0B2">适用于门店张贴 / 社群转发 / 朋友圈宣传</text>
    </svg>
  `);
}

const demoAssets: Asset[] = [
  { id: "demo-product", url: createAssetSvg("product", "爆款零食_组合.png"), filename: "爆款零食_组合.png", assetType: "product", size: 520000 },
  { id: "demo-logo", url: createAssetSvg("logo", "门店_Logo.png"), filename: "门店_Logo.png", assetType: "logo", size: 120000 },
  { id: "demo-bg", url: createAssetSvg("background", "橙色活动背景.jpg"), filename: "橙色活动背景.jpg", assetType: "background", size: 850000 },
  { id: "demo-sale", url: createAssetSvg("decoration", "限时特价.png", "#E53935"), filename: "限时特价.png", assetType: "decoration", size: 160000 },
  { id: "demo-price", url: createAssetSvg("decoration", "爆款标签.png", "#F58220"), filename: "爆款标签.png", assetType: "decoration", size: 180000 },
  { id: "demo-yellow", url: createAssetSvg("decoration", "会员日标签.png", "#FFD43B"), filename: "会员日标签.png", assetType: "decoration", size: 220000 },
];

export const usePosterGeneratorStore = defineStore("poster-generator", () => {
  const mainColor = ref("#F58220");
  const subColor = ref("#FFD43B");
  const brandDescription = ref("正品量贩，便宜又好");
  const styleDescription = ref("橙色门店风，高饱和促销感，突出价格和爆款零食");

  const assets = ref<Asset[]>(demoAssets);

  const materialType = ref<MaterialType>("poster");
  const width = ref(1080);
  const height = ref(1920);
  const title = ref("爆款零食限时特价");
  const subtitle = ref("全场好货，门店会员日超值购");
  const activityDescription = ref("会员日满 29 减 5");
  const designRequirement = ref("参考零食量贩门店橙色视觉，突出促销氛围、价格标签和爆款零食组合");
  const outputFormat = ref<OutputFormat>("png");

  const generationStatus = ref<GenerationStatus>("success");
  const generatedImageUrl = ref<string | null>(createDemoPosterSvg());
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
