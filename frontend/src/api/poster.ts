import { del, get, post, postForm, put } from "@/api/request";
import type {
  Asset,
  AssetType,
  GeneratePosterPayload,
  GeneratePosterResponse,
  PosterHistoryItem,
  UploadAssetPayload,
  UploadAssetResponse,
} from "@/types/poster";

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

interface GeneratePosterApiResponse {
  taskId: number;
  status: "success" | "error";
  imageUrl: string;
  width: number;
  height: number;
}

interface PosterHistoryApiResponse {
  taskId: number;
  title?: string;
  subtitle?: string;
  status: "pending" | "success" | "failed" | "error";
  imageUrl?: string;
  width: number;
  height: number;
  materialType: string;
  assetIds: number[];
  errorMessage?: string;
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

export async function updatePosterAssetType(assetId: string, assetType: AssetType): Promise<Asset> {
  const params = new URLSearchParams({ assetType });
  const result = await put<AssetUploadApiResponse>(`/api/assets/${assetId}/type?${params.toString()}`);
  return toAsset(result);
}

export async function deletePosterAsset(assetId: string) {
  await del<void>(`/api/assets/${assetId}`);
}

export async function generatePoster(
  payload: GeneratePosterPayload,
): Promise<GeneratePosterResponse> {
  const result = await post<GeneratePosterApiResponse>("/api/posters/generate", {
    ...payload,
    assetIds: payload.assetIds.map((assetId) => Number(assetId)).filter((assetId) => Number.isFinite(assetId)),
  });

  return {
    success: true,
    taskId: String(result.taskId),
    status: result.status,
    imageUrl: resolveAssetUrl(result.imageUrl),
    width: result.width,
    height: result.height,
  };
}

export async function listPosterHistory(): Promise<PosterHistoryItem[]> {
  const result = await get<PosterHistoryApiResponse[]>("/api/posters/history");
  return result.map((item) => ({
    taskId: String(item.taskId),
    title: item.title || "未命名海报",
    subtitle: item.subtitle || "",
    status: item.status === "failed" ? "error" : item.status,
    imageUrl: item.imageUrl ? resolveAssetUrl(item.imageUrl) : undefined,
    width: item.width,
    height: item.height,
    materialType: item.materialType,
    assetIds: item.assetIds.map(String),
    errorMessage: item.errorMessage,
    createdAt: item.createdAt,
  }));
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
