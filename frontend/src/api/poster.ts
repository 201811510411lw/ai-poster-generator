import { del, get, postForm } from "@/api/request";
import type {
  Asset,
  AssetType,
  GeneratePosterPayload,
  GeneratePosterResponse,
  UploadAssetPayload,
  UploadAssetResponse,
} from "@/types/poster";

const sleep = (ms: number) => new Promise((resolve) => window.setTimeout(resolve, ms));

interface AssetUploadApiResponse {
  assetId: number;
  assetType: AssetType;
  filename: string;
  originalFilename: string;
  contentType?: string;
  fileSize: number;
  width?: number;
  height?: number;
  url: string;
  createdAt?: string;
}

export async function listPosterAssets(): Promise<Asset[]> {
  const result = await get<AssetUploadApiResponse[]>("/api/assets");
  return result.map(toAsset);
}

export async function uploadPosterAsset(
  payload: UploadAssetPayload,
): Promise<UploadAssetResponse> {
  const formData = new FormData();
  formData.append("assetType", payload.assetType);
  formData.append("file", payload.file);

  const result = await postForm<AssetUploadApiResponse>("/api/assets/upload", formData);

  return {
    success: true,
    assetId: String(result.assetId),
    url: resolveAssetUrl(result.url),
    width: result.width,
    height: result.height,
  };
}

export async function deletePosterAsset(assetId: string) {
  await del<void>(`/api/assets/${assetId}`);
}

export async function generatePoster(
  payload: GeneratePosterPayload,
): Promise<GeneratePosterResponse> {
  await sleep(1400);

  const footerDescription = payload.activityDescription || payload.styleDescription;
  const footerDescriptionText = footerDescription
    ? `<text x="56" y="${Math.max(payload.height - 110, 80)}" fill="white" font-size="22" font-family="Arial, sans-serif" opacity="0.92">${escapeXml(footerDescription)}</text>`
    : "";

  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="${payload.width}" height="${payload.height}" viewBox="0 0 ${payload.width} ${payload.height}">
      <defs>
        <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="${payload.mainColor}" />
          <stop offset="100%" stop-color="${payload.subColor}" />
        </linearGradient>
      </defs>
      <rect width="100%" height="100%" rx="24" fill="url(#bg)" />
      <rect x="32" y="32" width="${Math.max(payload.width - 64, 0)}" height="${Math.max(payload.height - 64, 0)}" rx="20" fill="rgba(255,255,255,0.12)" stroke="rgba(255,255,255,0.28)" />
      <text x="56" y="88" fill="white" font-size="28" font-family="Arial, sans-serif" opacity="0.88">AI Poster Generator</text>
      <text x="56" y="150" fill="white" font-size="54" font-weight="700" font-family="Arial, sans-serif">${escapeXml(payload.title || "请填写主标题")}</text>
      <text x="56" y="210" fill="white" font-size="28" font-family="Arial, sans-serif" opacity="0.92">${escapeXml(payload.subtitle || "在这里预览生成结果")}</text>
      ${footerDescriptionText}
      <text x="56" y="${Math.max(payload.height - 64, 120)}" fill="white" font-size="18" font-family="Arial, sans-serif" opacity="0.7">${payload.width} x ${payload.height} · ${payload.outputFormat.toUpperCase()}</text>
    </svg>
  `;

  return {
    success: true,
    taskId: crypto.randomUUID(),
    status: "success",
    imageUrl: `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`,
    width: payload.width,
    height: payload.height,
  };
}

function toAsset(result: AssetUploadApiResponse): Asset {
  return {
    id: String(result.assetId),
    url: resolveAssetUrl(result.url),
    filename: result.originalFilename || result.filename,
    assetType: result.assetType,
    width: result.width,
    height: result.height,
    size: result.fileSize,
  };
}

function resolveAssetUrl(url: string) {
  if (!url || url.startsWith("http://") || url.startsWith("https://") || url.startsWith("data:") || url.startsWith("blob:")) {
    return url;
  }

  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
  return `${apiBaseUrl.replace(/\/$/, "")}/${url.replace(/^\//, "")}`;
}

function escapeXml(input: string) {
  return input
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&apos;");
}
