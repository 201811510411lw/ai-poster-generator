<template>
  <SectionCard title="生成结果" class="h-full">
    <template #header-extra>
      <n-button secondary size="small" class="font-bold">
        <template #icon>
          <History :size="14" />
        </template>
        生成历史
      </n-button>
    </template>

    <div class="workspace-scroll flex h-full min-h-0 flex-col overflow-y-auto pr-2">
      <div class="flex min-h-0 flex-1 items-center justify-center rounded-[22px] bg-[linear-gradient(180deg,#F8FAFC,#F1F5F9)] px-7 py-6 shadow-inner">
        <div class="poster-canvas" :style="canvasStyle">
          <div class="absolute right-3 top-3 z-10 rounded-lg bg-slate-900/55 px-2.5 py-1 text-[11px] font-bold text-white shadow-sm backdrop-blur">
            {{ currentSizeLabel }}
          </div>

          <div v-if="generationStatus === 'idle'" class="flex h-full items-center justify-center px-8 text-center">
            <div>
              <div class="mx-auto grid h-14 w-14 place-items-center rounded-2xl bg-white text-slate-400 shadow-sm ring-1 ring-slate-200">
                <ImagePlus :size="24" />
              </div>
              <h3 class="mt-4 text-lg font-extrabold tracking-[-0.02em] text-slate-800">等待你的第一张成品</h3>
              <p class="mt-2 text-sm leading-6 text-slate-500">上传素材、填写参数后点击生成</p>
            </div>
          </div>

          <div v-else-if="generationStatus === 'generating'" class="flex h-full items-center justify-center">
            <n-spin size="large">
              <template #description>
                <span class="text-slate-500">AI 正在生成海报...</span>
              </template>
            </n-spin>
          </div>

          <div v-else-if="generationStatus === 'error'" class="flex h-full items-center justify-center px-8">
            <n-result status="error" title="生成失败" :description="errorMessage || '请调整描述后重试'">
              <template #footer>
                <n-button type="primary" @click="store.generate()">重试</n-button>
              </template>
            </n-result>
          </div>

          <img v-else :src="generatedImageUrl || undefined" alt="generated poster" class="h-full w-full object-cover" />
        </div>
      </div>

      <div class="mt-4 grid grid-cols-4 gap-3">
        <button v-for="action in actions" :key="action.label" class="action-button" type="button" @click="action.handler">
          <component :is="action.icon" :size="17" />
          <span>{{ action.label }}</span>
        </button>
      </div>

      <div class="mt-5 border-t border-slate-100 pt-4">
        <div class="mb-3 flex items-center justify-between">
          <h3 class="text-sm font-extrabold text-slate-900">更多生成结果</h3>
          <button class="grid h-8 w-8 place-items-center rounded-full bg-slate-50 text-slate-500 transition hover:bg-violet-50 hover:text-[var(--primary)]">
            <ChevronRight :size="16" />
          </button>
        </div>
        <div class="flex gap-3 overflow-hidden">
          <div
            v-for="item in candidateResults"
            :key="item"
            class="h-20 w-14 shrink-0 overflow-hidden rounded-xl border border-slate-200 bg-slate-100 shadow-sm"
          >
            <img v-if="generatedImageUrl" :src="generatedImageUrl" alt="candidate poster" class="h-full w-full object-cover" />
            <div v-else class="h-full w-full bg-[linear-gradient(135deg,#EEF2F7,#F8FAFC_48%,#F5F3FF)]" />
          </div>
        </div>
      </div>

      <div class="mt-4 flex items-center justify-between border-t border-slate-100 pt-4 text-xs text-slate-400">
        <span>每次生成消耗 2 积分，当前剩余 98 积分</span>
        <button class="font-bold text-[var(--primary)] hover:text-[var(--primary-hover)]">获取更多积分</button>
      </div>
    </div>
  </SectionCard>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { ChevronRight, Download, History, ImagePlus, RefreshCw, Share2, SlidersHorizontal } from "lucide-vue-next";
import { NButton, NResult, NSpin, useMessage } from "naive-ui";
import SectionCard from "@/components/SectionCard.vue";
import { usePosterGenerator } from "@/composables/usePosterGenerator";

const { store, generationStatus, generatedImageUrl, errorMessage, width, height, outputFormat } =
  usePosterGenerator();
const message = useMessage();
const candidateResults = ["v1", "v2", "v3", "v4", "v5"];

const currentSizeLabel = computed(() => `竖版海报 ${width.value}x${height.value}`);
const canvasStyle = computed(() => ({
  aspectRatio: `${width.value || 1080} / ${height.value || 1920}`,
}));

const actions = [
  { label: "下载", icon: Download, handler: handleDownload },
  { label: "重新生成", icon: RefreshCw, handler: () => store.generate() },
  { label: "继续优化", icon: SlidersHorizontal, handler: () => message.info("可以继续调整左侧参数后重新生成") },
  { label: "分享", icon: Share2, handler: () => message.info("分享能力待接入") },
];

function handleDownload() {
  if (!generatedImageUrl.value) {
    message.warning("请先生成海报");
    return;
  }

  const link = document.createElement("a");
  link.href = generatedImageUrl.value;
  link.download = `poster_${Date.now()}.${outputFormat.value}`;
  link.click();
}
</script>
