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

function createAssetSvg(kind: AssetType, title: string, color = "#2563EB") {
  const bg = kind === "background" ? "#DFF3FF" : "#F8FAFC";
  const decoration =
    kind === "background"
      ? `<path d="M0 110 C70 70 120 98 190 54 C250 18 310 44 360 20 L360 220 L0 220 Z" fill="#86EFAC" opacity="0.92"/><path d="M0 132 C76 88 142 118 205 78 C264 42 320 58 360 36 L360 220 L0 220 Z" fill="#22C55E" opacity="0.8"/>`
      : kind === "logo"
        ? `<text x="180" y="88" text-anchor="middle" font-size="46" font-weight="800" fill="${color}">纯牛奶</text><text x="180" y="124" text-anchor="middle" font-size="20" fill="#64748B">PURE MILK</text>`
        : kind === "decoration"
          ? `<circle cx="180" cy="98" r="62" fill="${color}"/><text x="180" y="82" text-anchor="middle" font-size="22" font-weight="800" fill="white">限时特惠</text><text x="180" y="124" text-anchor="middle" font-size="34" font-weight="900" fill="white">半价</text>`
          : `<ellipse cx="180" cy="175" rx="92" ry="18" fill="#CBD5E1" opacity="0.45"/><rect x="135" y="42" width="90" height="142" rx="28" fill="#F8FAFC" stroke="#DCE6F2" stroke-width="4"/><rect x="145" y="112" width="70" height="46" rx="12" fill="#EFF6FF" stroke="#BFDBFE"/><text x="180" y="142" text-anchor="middle" font-size="22" font-weight="800" fill="#2563EB">纯牛奶</text>`;

  return svgDataUrl(`
    <svg xmlns="http://www.w3.org/2000/svg" width="360" height="260" viewBox="0 0 360 260">
      <rect width="360" height="260" rx="28" fill="${bg}"/>
      ${decoration}
      <text x="24" y="236" font-size="20" font-weight="700" fill="#334155">${title}</text>
    </svg>
  `);
}

function createDemoPosterSvg() {
  return svgDataUrl(`
    <svg xmlns="http://www.w3.org/2000/svg" width="1080" height="1920" viewBox="0 0 1080 1920">
      <defs>
        <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#8DCEFF"/>
          <stop offset="48%" stop-color="#EAF7FF"/>
          <stop offset="100%" stop-color="#F8FBFF"/>
        </linearGradient>
        <linearGradient id="milk" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#FFFFFF"/>
          <stop offset="100%" stop-color="#F1F5F9"/>
        </linearGradient>
        <filter id="shadow" x="-30%" y="-30%" width="160%" height="160%">
          <feDropShadow dx="0" dy="24" stdDeviation="28" flood-color="#0F172A" flood-opacity="0.22"/>
        </filter>
      </defs>
      <rect width="1080" height="1920" rx="44" fill="url(#sky)"/>
      <circle cx="900" cy="210" r="120" fill="#FFFFFF" opacity="0.35"/>
      <path d="M0 980 C210 820 410 930 620 790 C800 670 970 710 1080 620 L1080 1920 L0 1920 Z" fill="#A7F3D0" opacity="0.86"/>
      <path d="M0 1080 C240 890 430 1050 650 900 C840 770 1000 805 1080 735 L1080 1920 L0 1920 Z" fill="#22C55E" opacity="0.78"/>
      <path d="M0 1310 C180 1210 255 1308 420 1220 C610 1118 820 1224 1080 1088 L1080 1920 L0 1920 Z" fill="#F8FAFC" opacity="0.96"/>
      <path d="M0 1540 C180 1430 350 1580 540 1458 C760 1316 910 1510 1080 1390 L1080 1920 L0 1920 Z" fill="#FFFFFF" opacity="0.94"/>

      <text x="112" y="260" font-size="112" font-weight="900" fill="#1D4ED8" letter-spacing="-5">纯天然</text>
      <text x="112" y="390" font-size="112" font-weight="900" fill="#1D4ED8" letter-spacing="-5">好牛奶</text>
      <text x="120" y="462" font-size="34" font-weight="700" fill="#2563EB">精选优质牧场，全家营养好选择</text>

      <circle cx="225" cy="650" r="112" fill="#1E40AF"/>
      <text x="225" y="600" text-anchor="middle" font-size="34" font-weight="800" fill="white">限时特惠</text>
      <text x="225" y="656" text-anchor="middle" font-size="48" font-weight="900" fill="white">第二件</text>
      <text x="225" y="722" text-anchor="middle" font-size="64" font-weight="900" fill="white">半价</text>

      <g filter="url(#shadow)">
        <ellipse cx="710" cy="1510" rx="180" ry="44" fill="#0F172A" opacity="0.14"/>
        <rect x="610" y="500" width="200" height="820" rx="86" fill="url(#milk)" stroke="#E2E8F0" stroke-width="8"/>
        <rect x="642" y="450" width="136" height="90" rx="32" fill="#F8FAFC" stroke="#CBD5E1" stroke-width="6"/>
        <rect x="614" y="950" width="192" height="220" rx="36" fill="#FFFFFF" stroke="#BFDBFE" stroke-width="8"/>
        <text x="710" y="1046" text-anchor="middle" font-size="48" font-weight="900" fill="#1D4ED8">纯牛奶</text>
        <text x="710" y="1098" text-anchor="middle" font-size="24" font-weight="700" fill="#2563EB">PURE MILK</text>
        <path d="M626 1168 C680 1138 746 1212 804 1170 L804 1255 L626 1255 Z" fill="#1D4ED8" opacity="0.9"/>
      </g>

      <path d="M40 1460 C145 1370 268 1514 370 1414 C470 1318 578 1470 690 1390 C810 1304 954 1438 1040 1348" fill="none" stroke="#FFFFFF" stroke-width="48" stroke-linecap="round" opacity="0.92"/>
      <path d="M760 354 C850 270 945 236 1035 210" fill="none" stroke="#65A30D" stroke-width="18" stroke-linecap="round"/>
    </svg>
  `);
}

const demoAssets: Asset[] = [
  { id: "demo-product", url: createAssetSvg("product", "纯牛奶_产品图.png"), filename: "纯牛奶_产品图.png", assetType: "product", size: 520000 },
  { id: "demo-logo", url: createAssetSvg("logo", "品牌_Logo.png"), filename: "品牌_Logo.png", assetType: "logo", size: 120000 },
  { id: "demo-bg", url: createAssetSvg("background", "自然背景.jpg"), filename: "自然背景.jpg", assetType: "background", size: 850000 },
  { id: "demo-leaf", url: createAssetSvg("decoration", "绿叶.png", "#22C55E"), filename: "绿叶.png", assetType: "decoration", size: 160000 },
  { id: "demo-sale", url: createAssetSvg("decoration", "促销标签.png", "#1D4ED8"), filename: "促销标签.png", assetType: "decoration", size: 180000 },
  { id: "demo-splash", url: createAssetSvg("decoration", "牛奶飞溅.png", "#93C5FD"), filename: "牛奶飞溅.png", assetType: "decoration", size: 220000 },
];

export const usePosterGeneratorStore = defineStore("poster-generator", () => {
  const mainColor = ref("#E60012");
  const subColor = ref("#FFD700");
  const brandDescription = ref("天然营养，好喝更健康");
  const styleDescription = ref("清新自然，简约现代，突出产品品质感");

  const assets = ref<Asset[]>(demoAssets);

  const materialType = ref<MaterialType>("poster");
  const width = ref(1080);
  const height = ref(1920);
  const title = ref("纯天然好牛奶");
  const subtitle = ref("精选优质牧场，全家营养好选择");
  const activityDescription = ref("限时特惠，第二件半价");
  const designRequirement = ref("突出产品，画面简洁大气，重点突出牛奶的纯净和营养");
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
