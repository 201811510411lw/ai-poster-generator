<template>
  <section class="section-card flex min-h-0 flex-col overflow-hidden rounded-[20px] border border-slate-200 bg-white shadow-[0_8px_24px_rgba(15,23,42,0.04)]">
    <div v-if="title || description" class="shrink-0 border-b border-slate-100 px-6 py-5">
      <div class="flex items-start justify-between gap-4">
        <div class="min-w-0">
          <div v-if="eyebrow" class="mb-1 text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400">
            {{ eyebrow }}
          </div>
          <h2 class="flex items-center gap-2 text-[18px] font-bold tracking-[-0.02em] text-slate-950">
            {{ title }}
          </h2>
          <p v-if="description" class="mt-1 max-w-2xl text-xs leading-5 text-slate-500">{{ description }}</p>
        </div>
        <slot name="header-extra" />
      </div>
    </div>
    <div class="min-h-0 flex-1 p-6">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title?: string;
  description?: string;
  eyebrow?: string;
}>();
</script>
