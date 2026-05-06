export type MaterialType =
  | "main"
  | "poster"
  | "rollup"
  | "tv"
  | "flag"
  | "custom";

export type AssetType =
  | "product"
  | "logo"
  | "decoration"
  | "background"
  | "reference"
  | "other";

export type OutputFormat = "png" | "jpg";

export type GenerationStatus = "idle" | "generating" | "success" | "error";

export interface Asset {
  id: string;
  file?: File;
  url: string;
  filename: string;
  assetType: AssetType;
  width?: number;
  height?: number;
  size: number;
}

export interface UploadAssetPayload {
  file: File;
  assetType: AssetType;
}

export interface UploadAssetResponse {
  success: boolean;
  assetId: string;
  url: string;
  width?: number;
  height?: number;
}

export interface GeneratePosterPayload {
  materialType: MaterialType;
  width: number;
  height: number;
  mainColor: string;
  subColor: string;
  brandDescription: string;
  styleDescription: string;
  title: string;
  subtitle: string;
  activityDescription: string;
  designRequirement: string;
  outputFormat: OutputFormat;
  assetIds: string[];
}

export interface GeneratePosterResponse {
  success: boolean;
  taskId: string;
  status: "success" | "error";
  imageUrl: string;
  width: number;
  height: number;
}

export interface PosterHistoryItem {
  taskId: string;
  title: string;
  subtitle: string;
  status: "pending" | "success" | "error";
  imageUrl?: string;
  width: number;
  height: number;
  materialType: string;
  assetIds: string[];
  errorMessage?: string;
  createdAt?: string;
}
