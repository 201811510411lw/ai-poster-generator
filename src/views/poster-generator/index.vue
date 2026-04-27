<template>
  <AppShell>
    <div class="grid h-full min-h-0 min-w-0 grid-cols-[320px_minmax(0,1fr)_400px] gap-5 overflow-hidden 2xl:grid-cols-[340px_minmax(0,1fr)_420px]">
      <div class="h-full min-h-0 min-w-0 overflow-hidden">
        <AssetUploadPanel />
      </div>
      <div class="flex h-full min-h-0 min-w-0 flex-col overflow-hidden pr-1">
        <div class="flex h-full min-h-0 flex-col gap-5 overflow-hidden">
          <TopConfigBar />
          <GenerateFormPanel />
        </div>
      </div>
      <div class="h-full min-h-0 min-w-0 overflow-hidden">
        <ResultPreviewPanel />
      </div>
    </div>
  </AppShell>
</template>

<script setup lang="ts">
import AppShell from "@/layouts/AppShell.vue";
import AssetUploadPanel from "./components/AssetUploadPanel.vue";
import GenerateFormPanel from "./components/GenerateFormPanel.vue";
import ResultPreviewPanel from "./components/ResultPreviewPanel.vue";
import TopConfigBar from "./components/TopConfigBar.vue";
</script>
