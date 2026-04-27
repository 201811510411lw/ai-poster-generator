<template>
  <SectionCard title="生成设置" class="min-h-0 flex-1">
    <n-form label-placement="top" class="flex h-full min-h-0 flex-col gap-5">
      <div class="grid gap-4 xl:grid-cols-[112px_96px_minmax(360px,1fr)]">
        <n-form-item label="物料类型">
          <n-select :value="materialType" :options="designerMaterialOptions" @update:value="handleMaterialChange" />
        </n-form-item>
        <n-form-item label="输出格式">
          <n-select v-model:value="outputFormat" :options="formatOptions" />
        </n-form-item>
        <n-form-item label="尺寸设置">
          <div class="grid w-full grid-cols-4 gap-3">
            <button
              v-for="preset in sizePresets"
              :key="preset.key"
              type="button"
              class="size-pill"
              :class="{ 'size-pill-active': isPresetActive(preset) }"
              @click="applySizePreset(preset)"
            >
              <span>{{ preset.label }}</span>
              <small>{{ preset.width }}x{{ preset.height }}</small>
            </button>
          </div>
        </n-form-item>
      </div>

      <div v-if="isCustomSize" class="grid gap-4 xl:grid-cols-2">
        <n-form-item label="自定义宽度">
          <n-input-number v-model:value="width" :min="1" class="w-full" placeholder="单位 px" />
        </n-form-item>
        <n-form-item label="自定义高度">
          <n-input-number v-model:value="height" :min="1" class="w-full" placeholder="单位 px" />
        </n-form-item>
      </div>

      <div class="grid gap-4 xl:grid-cols-2">
        <n-form-item label="主标题">
          <n-input v-model:value="title" placeholder="例如：纯天然好牛奶" />
        </n-form-item>
        <n-form-item label="副标题">
          <n-input v-model:value="subtitle" placeholder="例如：精选优质牧场，全家营养好选择" />
        </n-form-item>
      </div>

      <n-form-item label="活动说明（选填）">
        <n-input v-model:value="activityDescription" placeholder="例如：限时特惠，第二件半价" />
      </n-form-item>

      <n-form-item label="设计要求（选填）">
        <n-input
          v-model:value="designRequirement"
          type="textarea"
          :autosize="{ minRows: 3, maxRows: 5 }"
          maxlength="200"
          show-count
          placeholder="例如：突出产品，画面简洁大气，重点突出牛奶的纯净和营养"
        />
      </n-form-item>

      <div class="mt-auto grid gap-3 border-t border-slate-100 pt-5 xl:grid-cols-[1fr_128px]">
        <n-button
          class="gradient-button h-12 w-full text-[15px] font-bold"
          type="primary"
          size="large"
          :loading="generationStatus === 'generating'"
          :disabled="!canGenerate"
          @click="handleGenerate"
        >
          <template #icon>
            <Sparkles :size="17" />
          </template>
          {{ buttonText }}
        </n-button>
        <n-button class="h-12 font-bold" size="large" secondary @click="handleReset">
          <template #icon>
            <RotateCcw :size="16" />
          </template>
          重置设置
        </n-button>
      </div>
    </n-form>
  </SectionCard>
</template>

<script setup lang="ts">
import type { MaterialType } from "@/types/poster";
import { computed, ref } from "vue";
import { RotateCcw, Sparkles } from "lucide-vue-next";
import { NButton, NForm, NFormItem, NInput, NInputNumber, NSelect, useMessage } from "naive-ui";
import SectionCard from "@/components/SectionCard.vue";
import { usePosterGenerator } from "@/composables/usePosterGenerator";

interface SizePreset {
  key: MaterialType;
  label: string;
  width: number;
  height: number;
}

const {
  store,
  materialType,
  outputFormat,
  width,
  height,
  title,
  subtitle,
  activityDescription,
  designRequirement,
  generationStatus,
  canGenerate,
} = usePosterGenerator();
const message = useMessage();
const isCustomSize = ref(materialType.value === "custom");

const designerMaterialOptions: Array<{ label: string; value: MaterialType }> = [
  { label: "海报", value: "poster" },
  { label: "易拉宝", value: "rollup" },
  { label: "社媒图", value: "tv" },
  { label: "电商主图", value: "main" },
];

const formatOptions = [
  { label: "PNG", value: "png" },
  { label: "JPG", value: "jpg" },
];

const sizePresets: SizePreset[] = [
  { key: "poster", label: "竖版海报", width: 1080, height: 1920 },
  { key: "tv", label: "横版海报", width: 1920, height: 1080 },
  { key: "main", label: "正方形", width: 1080, height: 1080 },
  { key: "custom", label: "自定义尺寸", width: width.value, height: height.value },
];

const buttonText = computed(() => (generationStatus.value === "generating" ? "正在生成…" : "立即生成海报"));

function handleMaterialChange(value: MaterialType) {
  isCustomSize.value = false;
  store.setMaterialType(value);
}

function isPresetActive(preset: SizePreset) {
  if (preset.key === "custom") {
    return isCustomSize.value;
  }

  return !isCustomSize.value && width.value === preset.width && height.value === preset.height;
}

function applySizePreset(preset: SizePreset) {
  if (preset.key === "custom") {
    isCustomSize.value = true;
    store.setMaterialType("custom");
    return;
  }

  isCustomSize.value = false;
  store.setMaterialType(preset.key);
  if (preset.key === "main") {
    width.value = preset.width;
    height.value = preset.height;
  }
}

function handleReset() {
  store.setMaterialType("poster");
  outputFormat.value = "png";
  title.value = "";
  subtitle.value = "";
  activityDescription.value = "";
  designRequirement.value = "";
  isCustomSize.value = false;
  message.info("生成设置已重置");
}

async function handleGenerate() {
  try {
    await store.generate();
    message.success("生成完成");
  } catch (error) {
    message.error(error instanceof Error ? error.message : "生成失败");
  }
}
</script>
