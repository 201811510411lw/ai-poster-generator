import type { AssetType, MaterialType } from "@/types/poster";

export const materialOptions: Array<{ label: string; value: MaterialType }> = [
  { label: "主图", value: "main" },
  { label: "海报", value: "poster" },
  { label: "易拉宝", value: "rollup" },
  { label: "电视屏", value: "tv" },
  { label: "吊旗", value: "flag" },
  { label: "自定义", value: "custom" },
];

export const assetTypeOptions: Array<{ label: string; value: AssetType }> = [
  { label: "产品图", value: "product" },
  { label: "Logo", value: "logo" },
  { label: "PNG装饰元素", value: "decoration" },
  { label: "背景图", value: "background" },
  { label: "风格参考图", value: "reference" },
  { label: "其他素材", value: "other" },
];

export const materialSizeMap: Record<
  Exclude<MaterialType, "custom">,
  { width: number; height: number }
> = {
  main: { width: 800, height: 800 },
  poster: { width: 1080, height: 1920 },
  rollup: { width: 800, height: 2000 },
  tv: { width: 1920, height: 1080 },
  flag: { width: 600, height: 900 },
};
