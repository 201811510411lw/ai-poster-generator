<template>
  <AppShell @workflow-step-click="handleWorkflowStepClick">
    <div class="grid h-full min-h-0 min-w-0 grid-cols-[320px_minmax(0,1fr)_400px] gap-5 overflow-hidden 2xl:grid-cols-[340px_minmax(0,1fr)_420px]">
      <div
        ref="assetsSectionRef"
        class="h-full min-h-0 min-w-0 overflow-hidden rounded-[var(--radius-card)] transition"
        :class="{ 'section-focus-ring': focusedSection === 'assets' }"
      >
        <AssetUploadPanel />
      </div>
      <div class="flex h-full min-h-0 min-w-0 flex-col overflow-hidden pr-1">
        <div class="flex h-full min-h-0 flex-col gap-5 overflow-hidden">
          <div
            ref="basicSectionRef"
            class="min-h-0 shrink-0 rounded-[var(--radius-card)] transition"
            :class="{ 'section-focus-ring': focusedSection === 'basic' }"
          >
            <TopConfigBar />
          </div>
          <div
            ref="generateSectionRef"
            class="flex min-h-0 flex-1 flex-col overflow-hidden rounded-[var(--radius-card)] transition"
            :class="{ 'section-focus-ring': focusedSection === 'generate' }"
          >
            <GenerateFormPanel />
          </div>
        </div>
      </div>
      <div
        ref="resultSectionRef"
        class="h-full min-h-0 min-w-0 overflow-hidden rounded-[var(--radius-card)] transition"
        :class="{ 'section-focus-ring': focusedSection === 'result' }"
      >
        <ResultPreviewPanel />
      </div>
    </div>
  </AppShell>
</template>

<script setup lang="ts">
import { ref } from "vue";
import AppShell from "@/layouts/AppShell.vue";
import AssetUploadPanel from "./components/AssetUploadPanel.vue";
import GenerateFormPanel from "./components/GenerateFormPanel.vue";
import ResultPreviewPanel from "./components/ResultPreviewPanel.vue";
import TopConfigBar from "./components/TopConfigBar.vue";

type WorkflowStepKey = "basic" | "assets" | "generate" | "result";

const assetsSectionRef = ref<HTMLElement | null>(null);
const basicSectionRef = ref<HTMLElement | null>(null);
const generateSectionRef = ref<HTMLElement | null>(null);
const resultSectionRef = ref<HTMLElement | null>(null);
const focusedSection = ref<WorkflowStepKey | null>(null);
let focusTimer: number | undefined;

const sectionRefMap: Record<WorkflowStepKey, typeof basicSectionRef> = {
  basic: basicSectionRef,
  assets: assetsSectionRef,
  generate: generateSectionRef,
  result: resultSectionRef,
};

function handleWorkflowStepClick(key: WorkflowStepKey) {
  const target = sectionRefMap[key].value;
  if (!target) return;

  target.scrollIntoView({ behavior: "smooth", block: "nearest", inline: "center" });
  focusedSection.value = key;

  if (focusTimer) {
    window.clearTimeout(focusTimer);
  }

  focusTimer = window.setTimeout(() => {
    focusedSection.value = null;
  }, 1000);
}
</script>

<style scoped>
.section-focus-ring {
  box-shadow: 0 0 0 3px rgba(245, 130, 32, 0.22), 0 18px 42px rgba(245, 130, 32, 0.16);
}
</style>
