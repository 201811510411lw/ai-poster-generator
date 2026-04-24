<template>
  <div class="flex h-[100dvh] min-w-0 flex-col overflow-hidden bg-[var(--page-bg)] text-slate-950">
    <header class="z-30 h-[76px] shrink-0 border-b border-slate-200 bg-white/95 backdrop-blur">
      <div class="flex h-[76px] w-full min-w-0 items-center justify-between gap-6 px-6">
        <div class="flex min-w-[260px] items-center gap-3">
          <div class="grid h-10 w-10 place-items-center rounded-2xl bg-[linear-gradient(135deg,#4F46E5,#8B5CF6)] shadow-sm">
            <Sparkles :size="22" class="text-white" />
          </div>
          <div>
            <h1 class="text-[22px] font-bold tracking-[-0.03em] text-slate-950">AI 海报生成器</h1>
            <p class="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400">AI Poster Generator</p>
          </div>
        </div>

        <nav class="hidden flex-1 justify-center lg:flex">
          <div class="flex rounded-2xl bg-slate-50 p-1.5 ring-1 ring-slate-100">
            <div
              v-for="step in workflowSteps"
              :key="step.label"
              class="flex items-center gap-2 rounded-xl px-5 py-2.5 text-sm font-medium text-slate-500 first:bg-white first:text-[var(--primary)] first:shadow-sm"
            >
              <span
                class="grid h-5 w-5 place-items-center rounded-full text-[11px] font-bold"
                :class="step.active ? 'bg-[var(--primary)] text-white' : 'bg-slate-200 text-slate-500'"
              >
                {{ step.index }}
              </span>
              {{ step.label }}
            </div>
          </div>
        </nav>

        <div class="flex min-w-[320px] items-center justify-end gap-3">
          <n-button secondary class="h-11 px-4">
            <template #icon>
              <PlusSquare :size="16" />
            </template>
            新建项目
          </n-button>
          <button class="grid h-11 w-11 place-items-center rounded-full border border-slate-200 bg-white text-slate-600 transition hover:border-slate-300 hover:bg-slate-50">
            <Sun :size="18" />
          </button>
          <div class="flex items-center gap-3 rounded-full border border-slate-200 bg-white py-1.5 pl-1.5 pr-3">
            <div class="grid h-9 w-9 place-items-center rounded-full bg-[linear-gradient(135deg,#EEF2FF,#F5F3FF)] text-sm font-bold text-[var(--primary)]">
              设
            </div>
            <div class="hidden text-left xl:block">
              <div class="text-sm font-semibold leading-5 text-slate-800">设计师</div>
              <div class="text-xs text-slate-400">视觉设计师</div>
            </div>
            <ChevronDown :size="15" class="text-slate-400" />
          </div>
        </div>
      </div>
    </header>

    <main class="min-h-0 w-full min-w-0 flex-1 overflow-hidden px-8 py-4">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ChevronDown, PlusSquare, Sparkles, Sun } from "lucide-vue-next";
import { NButton } from "naive-ui";

const workflowSteps = [
  { index: 1, label: "基础设置", active: true },
  { index: 2, label: "素材管理", active: false },
  { index: 3, label: "生成设置", active: false },
  { index: 4, label: "生成结果", active: false },
];
</script>
