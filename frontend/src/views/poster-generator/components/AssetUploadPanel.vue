<template>
  <SectionCard title="素材库" class="h-full">
    <template #header-extra>
      <button class="grid h-7 w-7 place-items-center rounded-full text-slate-400 transition hover:bg-slate-50 hover:text-[var(--primary)]">
        <HelpCircle :size="16" />
      </button>
    </template>

    <div class="flex h-full min-h-0 flex-col overflow-hidden">
      <div class="grid shrink-0 grid-cols-2 gap-3">
        <n-select
          v-model:value="uploadAssetType"
          size="small"
          class="asset-filter-select w-full"
          :options="assetTypeSelectOptions"
          placeholder="上传素材类型"
        />
        <n-select
          v-model:value="filterType"
          size="small"
          class="asset-filter-select w-full"
          :options="filterOptions"
          placeholder="按类型筛选"
        />
      </div>

      <n-upload
        class="mt-4 shrink-0"
        multiple
        directory-dnd
        :max="10"
        :show-file-list="false"
        accept=".png,.jpg,.jpeg,image/png,image/jpeg"
        :custom-request="handleCustomUpload"
      >
        <n-upload-dragger>
          <div class="rounded-[20px] border border-dashed border-violet-200/80 bg-[linear-gradient(180deg,#ffffff,#fbfaff)] px-5 py-7 text-center transition hover:border-[var(--primary)] hover:bg-violet-50/40">
            <div class="mx-auto grid h-12 w-12 place-items-center rounded-2xl bg-white text-slate-500 shadow-sm ring-1 ring-slate-200">
              <Upload :size="23" />
            </div>
            <div class="mt-4 text-sm font-bold text-slate-700">拖拽或点击上传素材</div>
            <p class="mt-2 text-xs leading-5 text-slate-400">支持 JPG / PNG，最大 20MB。当前类型：{{ assetTypeLabelMap[uploadAssetType] }}</p>
            <n-button class="gradient-button mt-5 h-9 px-5 text-xs font-bold" type="primary">
              <template #icon>
                <PlusCircle :size="15" />
              </template>
              上传素材
            </n-button>
          </div>
        </n-upload-dragger>
      </n-upload>

      <div class="mt-5 grid shrink-0 grid-cols-2 gap-3 border-b border-slate-100 pb-4">
        <button
          type="button"
          class="flex h-10 w-full items-center justify-start rounded-xl border border-violet-200 bg-violet-50/70 px-4 text-sm font-bold text-[var(--primary)] transition hover:border-violet-300 hover:bg-violet-50"
          @click="filterType = 'all'"
        >
          <span>全部素材</span>
          <span class="ml-1 font-extrabold">({{ assets.length }})</span>
        </button>
        <div class="flex h-10 items-center justify-start rounded-xl bg-slate-50 px-4 text-sm font-bold text-slate-500">
          当前显示 {{ filteredAssets.length }} 个
        </div>
      </div>

      <div class="mt-3 flex shrink-0 items-center justify-between rounded-2xl bg-orange-50/60 px-3 py-2 text-xs">
        <span class="font-bold text-orange-600">已选择 {{ store.selectedAssetIds.length }} 个素材参与生成</span>
        <button
          v-if="store.selectedAssetIds.length"
          type="button"
          class="font-bold text-slate-400 transition hover:text-orange-500"
          @click="store.clearSelectedAssets()"
        >
          清空选择
        </button>
      </div>

      <div class="workspace-scroll mt-4 min-h-0 flex-1 overflow-y-auto pr-2">
        <div v-if="store.assetsLoading" class="rounded-2xl border border-dashed border-slate-200 bg-slate-50/80 px-5 py-8 text-center text-sm font-semibold text-slate-400">
          正在加载素材库...
        </div>

        <div v-else-if="filteredAssets.length" class="grid grid-cols-2 gap-4">
          <article
            v-for="asset in filteredAssets"
            :key="asset.id"
            class="group relative cursor-pointer rounded-2xl border bg-white p-2.5 transition hover:-translate-y-0.5 hover:shadow-[0_14px_28px_rgba(79,70,229,0.10)]"
            :class="store.isAssetSelected(asset.id) ? 'border-orange-300 ring-2 ring-orange-100' : 'border-slate-200/80 hover:border-violet-200'"
            @click="store.toggleAssetSelection(asset.id)"
          >
            <button
              type="button"
              class="absolute left-2 top-2 z-20 grid h-6 w-6 place-items-center rounded-full shadow-sm ring-1 transition"
              :class="store.isAssetSelected(asset.id) ? 'bg-orange-500 text-white ring-orange-400' : 'bg-white/90 text-slate-300 ring-slate-200 hover:text-orange-500'"
              :title="store.isAssetSelected(asset.id) ? '取消选择' : '选择素材'"
              @click.stop="store.toggleAssetSelection(asset.id)"
            >
              <Check v-if="store.isAssetSelected(asset.id)" :size="14" stroke-width="3" />
            </button>

            <button
              type="button"
              class="absolute right-2 top-2 z-20 grid h-6 w-6 place-items-center rounded-full bg-white/90 text-slate-400 shadow-sm backdrop-blur-sm ring-1 ring-slate-200 transition hover:bg-red-50 hover:text-red-500 hover:ring-red-200 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="deletingAssetId === asset.id"
              title="删除素材"
              @click.stop="handleDeleteAsset(asset.id)"
            >
              <X :size="13" stroke-width="2.4" />
            </button>

            <div class="aspect-[4/3] overflow-hidden rounded-[14px] bg-slate-100">
              <img :src="asset.url" :alt="asset.filename" class="h-full w-full object-cover" loading="lazy" />
            </div>
            <div class="mt-2.5 min-w-0">
              <div class="truncate text-xs font-semibold text-slate-600" :title="asset.filename">{{ asset.filename }}</div>
              <div class="mt-2 flex items-center justify-between gap-2">
                <span class="asset-chip">
                  {{ assetTypeLabelMap[asset.assetType] }}
                </span>
                <n-popselect
                  :value="asset.assetType"
                  :options="assetTypeSelectOptions"
                  @update:value="(value) => handleUpdateAssetType(asset.id, value)"
                >
                  <button class="rounded-lg p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600" @click.stop>
                    <SlidersHorizontal :size="13" />
                  </button>
                </n-popselect>
              </div>
            </div>
          </article>
        </div>

        <div v-else class="rounded-2xl border border-dashed border-slate-200 bg-slate-50/80 px-5 py-8 text-center">
          <ImageIcon :size="24" class="mx-auto text-slate-300" />
          <div class="mt-3 text-sm font-semibold text-slate-500">素材库还是空的</div>
          <p class="mt-1 text-xs text-slate-400">上传产品图、Logo 或背景图，点击卡片即可选择参与生成。</p>
        </div>
      </div>

      <div class="mt-5 flex shrink-0 items-start gap-2 border-t border-slate-100 pt-4 text-xs leading-5 text-slate-400">
        <Info :size="15" class="mt-0.5 shrink-0" />
        <span>提示：点击素材卡片可选择/取消选择，只有选中的素材会参与生成。</span>
      </div>
    </div>
  </SectionCard>
</template>

<script setup lang="ts">
import type { UploadCustomRequestOptions } from "naive-ui";
import type { AssetType } from "@/types/poster";
import { computed, onMounted, ref } from "vue";
import { Check, HelpCircle, ImageIcon, Info, PlusCircle, SlidersHorizontal, Upload, X } from "lucide-vue-next";
import { NButton, NPopselect, NSelect, NUpload, NUploadDragger, useMessage } from "naive-ui";
import SectionCard from "@/components/SectionCard.vue";
import { usePosterGenerator } from "@/composables/usePosterGenerator";
import { assetTypeOptions } from "@/utils/constants";

const { store, assets } = usePosterGenerator();
const message = useMessage();
const uploadAssetType = ref<AssetType>("product");
const filterType = ref<AssetType | "all">("all");
const deletingAssetId = ref<string | null>(null);

const assetTypeSelectOptions = assetTypeOptions;
const assetTypeLabelMap = Object.fromEntries(assetTypeOptions.map((item) => [item.value, item.label])) as Record<AssetType, string>;
const filterOptions = [{ label: "全部类型", value: "all" }, ...assetTypeOptions];

const filteredAssets = computed(() => {
  if (filterType.value === "all") {
    return assets.value;
  }

  return assets.value.filter((asset) => asset.assetType === filterType.value);
});

onMounted(async () => {
  try {
    await store.loadAssets();
  } catch (error) {
    message.error(error instanceof Error ? error.message : "素材库加载失败");
  }
});

async function handleCustomUpload(options: UploadCustomRequestOptions) {
  const file = options.file.file;

  if (!file) {
    options.onError();
    return;
  }

  if (!isSupportedImage(file)) {
    message.error("仅支持 PNG、JPG、JPEG 图片");
    options.onError();
    return;
  }

  if (file.size > 20 * 1024 * 1024) {
    message.error("单个文件不能超过 20MB");
    options.onError();
    return;
  }

  try {
    await store.addAsset(file, uploadAssetType.value);
    options.onFinish();
    message.success("素材上传成功，已自动选中");
  } catch (error) {
    options.onError();
    message.error(error instanceof Error ? error.message : "素材上传失败");
  }
}

async function handleUpdateAssetType(assetId: string, nextType: AssetType) {
  try {
    await store.updateAssetType(assetId, nextType);
    message.success("素材类型已更新");
  } catch (error) {
    message.error(error instanceof Error ? error.message : "素材类型更新失败");
  }
}

async function handleDeleteAsset(assetId: string) {
  deletingAssetId.value = assetId;
  try {
    await store.removeAsset(assetId);
    message.success("素材删除成功");
  } catch (error) {
    message.error(error instanceof Error ? error.message : "素材删除失败");
  } finally {
    deletingAssetId.value = null;
  }
}

function isSupportedImage(file: File) {
  const allowedTypes = new Set(["image/png", "image/jpeg"]);
  const lowerName = file.name.toLowerCase();
  return allowedTypes.has(file.type) && [".png", ".jpg", ".jpeg"].some((extension) => lowerName.endsWith(extension));
}
</script>

<style scoped>
.asset-filter-select :deep(.n-base-selection) {
  min-height: 40px;
  border-radius: 12px !important;
}

.asset-filter-select :deep(.n-base-selection-label) {
  min-height: 40px !important;
  padding-left: 14px !important;
  font-weight: 600;
}

.asset-filter-select :deep(.n-base-selection-input) {
  text-align: left;
}
</style>
