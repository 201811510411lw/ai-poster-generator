<template>
  <SectionCard title="素材管理" class="h-full">
    <template #header-extra>
      <button class="grid h-7 w-7 place-items-center rounded-full text-slate-400 transition hover:bg-slate-50 hover:text-[var(--primary)]">
        <HelpCircle :size="16" />
      </button>
    </template>

    <div class="flex h-full min-h-0 flex-col overflow-hidden">
      <n-upload
        class="shrink-0"
        multiple
        directory-dnd
        :max="10"
        :show-file-list="false"
        accept=".png,.jpg,.jpeg"
        :custom-request="handleCustomUpload"
      >
        <n-upload-dragger>
          <div class="rounded-[20px] border border-dashed border-violet-200/80 bg-[linear-gradient(180deg,#ffffff,#fbfaff)] px-5 py-7 text-center transition hover:border-[var(--primary)] hover:bg-violet-50/40">
            <div class="mx-auto grid h-12 w-12 place-items-center rounded-2xl bg-white text-slate-500 shadow-sm ring-1 ring-slate-200">
              <Upload :size="23" />
            </div>
            <div class="mt-4 text-sm font-bold text-slate-700">拖拽或点击上传素材</div>
            <p class="mt-2 text-xs leading-5 text-slate-400">支持 JPG / PNG 格式，最大 20MB</p>
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
        >
          <span>全部素材</span>
          <span class="ml-1 font-extrabold">({{ assets.length }})</span>
        </button>
        <n-select
          v-model:value="filterType"
          size="small"
          class="asset-filter-select w-full"
          :options="filterOptions"
          placeholder="按类型筛选"
        />
      </div>

      <div class="workspace-scroll mt-4 min-h-0 flex-1 overflow-y-auto pr-2">
        <div v-if="filteredAssets.length" class="grid grid-cols-2 gap-4">
          <article
            v-for="asset in filteredAssets"
            :key="asset.id"
            class="group rounded-2xl border border-slate-200/80 bg-white p-2.5 transition hover:-translate-y-0.5 hover:border-violet-200 hover:shadow-[0_14px_28px_rgba(79,70,229,0.10)]"
          >
            <div class="relative aspect-[4/3] overflow-hidden rounded-[14px] bg-slate-100">
              <img :src="asset.url" :alt="asset.filename" class="h-full w-full object-cover" />
              <n-button
                class="absolute right-2 top-2 opacity-0 transition group-hover:opacity-100"
                size="tiny"
                circle
                tertiary
                type="error"
                @click.stop="store.removeAsset(asset.id)"
              >
                <template #icon>
                  <Trash2 :size="13" />
                </template>
              </n-button>
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
                  @update:value="(value) => store.updateAssetType(asset.id, value)"
                >
                  <button class="rounded-lg p-1 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600">
                    <SlidersHorizontal :size="13" />
                  </button>
                </n-popselect>
              </div>
            </div>
          </article>
        </div>

        <div v-else class="rounded-2xl border border-dashed border-slate-200 bg-slate-50/80 px-5 py-8 text-center">
          <ImageIcon :size="24" class="mx-auto text-slate-300" />
          <div class="mt-3 text-sm font-semibold text-slate-500">还没有可展示素材</div>
          <p class="mt-1 text-xs text-slate-400">上传产品图、Logo 或背景图开始创作。</p>
        </div>
      </div>

      <div class="mt-5 flex shrink-0 items-start gap-2 border-t border-slate-100 pt-4 text-xs leading-5 text-slate-400">
        <Info :size="15" class="mt-0.5 shrink-0" />
        <span>提示：拖拽素材可调整顺序，影响生成效果。</span>
      </div>
    </div>
  </SectionCard>
</template>

<script setup lang="ts">
import type { UploadCustomRequestOptions } from "naive-ui";
import type { AssetType } from "@/types/poster";
import { computed, ref } from "vue";
import { HelpCircle, ImageIcon, Info, PlusCircle, SlidersHorizontal, Trash2, Upload } from "lucide-vue-next";
import { NButton, NPopselect, NSelect, NUpload, NUploadDragger, useMessage } from "naive-ui";
import SectionCard from "@/components/SectionCard.vue";
import { usePosterGenerator } from "@/composables/usePosterGenerator";
import { assetTypeOptions } from "@/utils/constants";

const { store, assets } = usePosterGenerator();
const message = useMessage();
const filterType = ref<AssetType | "all">("all");

const assetTypeSelectOptions = assetTypeOptions;
const assetTypeLabelMap = Object.fromEntries(assetTypeOptions.map((item) => [item.value, item.label]));
const filterOptions = [{ label: "按类型筛选", value: "all" }, ...assetTypeOptions];

const filteredAssets = computed(() => {
  if (filterType.value === "all") {
    return assets.value;
  }

  return assets.value.filter((asset) => asset.assetType === filterType.value);
});

async function handleCustomUpload(options: UploadCustomRequestOptions) {
  const file = options.file.file;

  if (!file) {
    options.onError();
    return;
  }

  if (file.size > 20 * 1024 * 1024) {
    message.error("单个文件不能超过 20MB");
    options.onError();
    return;
  }

  try {
    await store.addAsset(file, "product");
    options.onFinish();
    message.success("素材上传成功");
  } catch (error) {
    options.onError();
    message.error(error instanceof Error ? error.message : "素材上传失败");
  }
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
