<template>
  <SectionCard title="生成结果" class="h-full">
    <template #header-extra>
      <n-button secondary size="small" class="font-bold" :loading="historyLoading" @click="refreshHistory">
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

          <div v-if="generationStatus === 'idle' || generationStatus === 'canceled'" class="flex h-full items-center justify-center px-8 text-center">
            <div>
              <div class="mx-auto grid h-14 w-14 place-items-center rounded-2xl bg-white text-slate-400 shadow-sm ring-1 ring-slate-200">
                <ImagePlus :size="24" />
              </div>
              <h3 class="mt-4 text-lg font-extrabold tracking-[-0.02em] text-slate-800">等待你的第一张成品</h3>
              <p class="mt-2 text-sm leading-6 text-slate-500">上传素材、填写参数后点击生成</p>
            </div>
          </div>

          <div v-else-if="isGenerationRequestActive" class="flex h-full items-center justify-center">
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

          <img v-else-if="generatedImageUrl" :src="generatedImageUrl" alt="generated poster" class="h-full w-full object-cover" />

          <div v-else class="flex h-full items-center justify-center px-8 text-center">
            <div>
              <div class="mx-auto grid h-14 w-14 place-items-center rounded-2xl bg-white text-slate-400 shadow-sm ring-1 ring-slate-200">
                <ImagePlus :size="24" />
              </div>
              <h3 class="mt-4 text-lg font-extrabold tracking-[-0.02em] text-slate-800">暂无成品图片</h3>
              <p class="mt-2 text-sm leading-6 text-slate-500">请重新生成或选择历史结果</p>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-4 grid grid-cols-4 gap-3">
        <button
          v-for="action in actions"
          :key="action.label"
          class="action-button disabled:cursor-not-allowed disabled:opacity-45"
          type="button"
          :disabled="action.disabled"
          @click="action.handler"
        >
          <component :is="action.icon" :size="17" />
          <span>{{ action.label }}</span>
        </button>
      </div>

      <div class="mt-5 border-t border-slate-100 pt-4">
        <div class="mb-3 flex items-center justify-between">
          <div>
            <h3 class="text-sm font-extrabold text-slate-900">历史生成结果</h3>
            <p class="mt-1 text-[11px] text-slate-400">点击历史缩略图可切换到该生成结果</p>
          </div>
          <button
            class="grid h-8 w-8 place-items-center rounded-full bg-slate-50 text-slate-500 transition hover:bg-violet-50 hover:text-[var(--primary)] disabled:cursor-not-allowed disabled:opacity-50"
            type="button"
            :disabled="historyLoading"
            @click="refreshHistory"
          >
            <RefreshCw :size="15" :class="{ 'animate-spin': historyLoading }" />
          </button>
        </div>

        <div v-if="historyLoading && !historyResults.length" class="flex gap-3 overflow-hidden">
          <div
            v-for="item in 5"
            :key="item"
            class="h-20 w-14 shrink-0 overflow-hidden rounded-xl border border-slate-200 bg-slate-100 shadow-sm"
          >
            <div class="h-full w-full animate-pulse bg-[linear-gradient(135deg,#EEF2F7,#F8FAFC_48%,#F5F3FF)]" />
          </div>
        </div>

        <div v-else-if="historyResults.length" class="flex gap-3 overflow-x-auto pb-1">
          <button
            v-for="item in historyResults"
            :key="item.taskId"
            class="group relative h-20 w-14 shrink-0 overflow-hidden rounded-xl border border-slate-200 bg-slate-100 shadow-sm transition hover:-translate-y-0.5 hover:border-[var(--primary)] hover:shadow-md disabled:cursor-not-allowed disabled:opacity-50 disabled:hover:translate-y-0 disabled:hover:border-slate-200 disabled:hover:shadow-sm"
            :class="{ 'border-[var(--primary)] ring-2 ring-orange-100': item.imageUrl === generatedImageUrl }"
            type="button"
            :title="item.title || '历史海报'"
            :disabled="isGenerationRequestActive"
            @click="selectHistoryResult(item)"
          >
            <img :src="item.imageUrl" alt="history poster" class="h-full w-full object-cover" />
            <div class="absolute inset-x-0 bottom-0 bg-slate-900/60 px-1.5 py-1 text-[10px] font-bold text-white opacity-0 transition group-hover:opacity-100">
              查看
            </div>
          </button>
        </div>

        <div v-else class="rounded-2xl border border-dashed border-slate-200 bg-slate-50/70 px-4 py-5 text-center text-xs text-slate-400">
          暂无历史结果，生成海报后会自动出现在这里
        </div>
      </div>

      <div class="mt-4 flex items-center justify-between border-t border-slate-100 pt-4 text-xs text-slate-400">
        <span>每次生成消耗 2 积分，当前剩余 98 积分</span>
        <button class="font-bold text-[var(--primary)] hover:text-[var(--primary-hover)]" type="button" @click="message.info('积分充值功能待接入')">获取更多积分</button>
      </div>
    </div>
  </SectionCard>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { Download, History, ImagePlus, RefreshCw, Share2, SlidersHorizontal } from "lucide-vue-next";
import { NButton, NResult, NSpin, useMessage } from "naive-ui";
import SectionCard from "@/components/SectionCard.vue";
import { usePosterGenerator } from "@/composables/usePosterGenerator";
import type { PosterHistoryItem } from "@/types/poster";

const {
  store,
  generationStatus,
  generatedImageUrl,
  errorMessage,
  width,
  height,
  outputFormat,
  history,
  historyLoading,
  isGenerationRequestActive,
} = usePosterGenerator();
const message = useMessage();

const hasGeneratedImage = computed(() => !!generatedImageUrl.value);
const historyResults = computed(() => history.value.filter((item) => item.status === "success" && !!item.imageUrl).slice(0, 12));
const currentSizeLabel = computed(() => `竖版海报 ${width.value}x${height.value}`);
const canvasStyle = computed(() => ({
  aspectRatio: `${width.value || 1080} / ${height.value || 1920}`,
}));

const actions = computed(() => [
  { label: "下载", icon: Download, handler: handleDownload, disabled: !hasGeneratedImage.value || isGenerationRequestActive.value },
  { label: "重新生成", icon: RefreshCw, handler: handleRegenerate, disabled: isGenerationRequestActive.value },
  { label: "继续优化", icon: SlidersHorizontal, handler: handleContinueOptimize, disabled: !hasGeneratedImage.value || isGenerationRequestActive.value },
  { label: "分享", icon: Share2, handler: handleShare, disabled: !hasGeneratedImage.value || isGenerationRequestActive.value },
]);

onMounted(() => {
  void store.loadHistory();
});

async function refreshHistory() {
  await store.loadHistory(true);
  message.success("历史结果已刷新");
}

function selectHistoryResult(item: PosterHistoryItem) {
  if (isGenerationRequestActive.value) {
    message.warning("当前正在生成，请先停止生成或等待完成后再切换历史结果");
    return;
  }

  if (!item.imageUrl) return;

  generatedImageUrl.value = item.imageUrl;
  width.value = item.width;
  height.value = item.height;
  generationStatus.value = "success";
  message.success("已切换到历史生成结果");
}

async function handleRegenerate() {
  try {
    await store.generate();
  } catch (error) {
    message.error(error instanceof Error ? error.message : "生成失败");
  }
}

function handleContinueOptimize() {
  if (!generatedImageUrl.value) {
    message.warning("请先生成海报");
    return;
  }

  message.info("已保留当前结果。你可以调整基础设置、素材或生成设置后点击重新生成。");
}

async function handleShare() {
  if (!generatedImageUrl.value) {
    message.warning("请先生成海报");
    return;
  }

  try {
    if (navigator.share) {
      await navigator.share({
        title: "AI 海报生成结果",
        text: "查看我的 AI 海报生成结果",
        url: generatedImageUrl.value,
      });
      return;
    }

    await navigator.clipboard.writeText(generatedImageUrl.value);
    message.success("图片链接已复制");
  } catch {
    message.info("当前浏览器不支持直接分享，请手动复制图片链接");
  }
}

function handleDownload() {
  if (!generatedImageUrl.value) {
    message.warning("请先生成海报");
    return;
  }

  const link = document.createElement("a");
  link.href = generatedImageUrl.value;
  link.download = `poster_${Date.now()}.${outputFormat.value}`;
  link.rel = "noopener";
  document.body.appendChild(link);
  link.click();
  link.remove();
}
</script>
