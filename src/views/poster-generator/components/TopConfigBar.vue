<template>
  <SectionCard title="基础设置" class="shrink-0">
    <template #header-extra>
      <ChevronUp :size="16" class="mt-1 text-slate-300" />
    </template>

    <div class="grid gap-4 xl:grid-cols-[150px_150px_minmax(220px,1fr)]">
      <div>
        <label class="field-label">主色调</label>
        <div class="color-control">
          <span class="h-8 w-8 rounded-lg shadow-inner ring-1 ring-black/5" :style="{ backgroundColor: mainColor }" />
          <n-input v-model:value="mainColor" size="small" class="min-w-0 flex-1" />
          <Edit3 :size="15" class="text-slate-400" />
        </div>
      </div>

      <div>
        <label class="field-label">辅助色</label>
        <div class="color-control">
          <span class="h-8 w-8 rounded-lg shadow-inner ring-1 ring-black/5" :style="{ backgroundColor: subColor }" />
          <n-input v-model:value="subColor" size="small" class="min-w-0 flex-1" />
          <Edit3 :size="15" class="text-slate-400" />
        </div>
      </div>

      <n-form-item label="品牌/活动描述">
        <n-input
          v-model:value="brandDescription"
          maxlength="50"
          show-count
          placeholder="例如：天然营养，好喝更健康"
        />
      </n-form-item>
    </div>

    <n-form-item label="风格描述" class="mt-3">
      <n-input
        v-model:value="styleDescription"
        maxlength="100"
        show-count
        placeholder="清新自然，简约现代，突出产品品质感"
      />
    </n-form-item>
  </SectionCard>
</template>

<script setup lang="ts">
import { ChevronUp, Edit3 } from "lucide-vue-next";
import { NFormItem, NInput } from "naive-ui";
import SectionCard from "@/components/SectionCard.vue";
import { usePosterGenerator } from "@/composables/usePosterGenerator";

const { mainColor, subColor, brandDescription, styleDescription } = usePosterGenerator();
</script>
