<template>
  <section class="section-card flex min-h-0 flex-col overflow-hidden rounded-[var(--radius-card)] border border-[var(--line-soft)] bg-[var(--card-bg)] shadow-[var(--shadow-card)]">
    <div v-if="title || description" class="shrink-0 px-6 pb-4 pt-5">
      <div class="flex items-start justify-between gap-4">
        <div class="min-w-0">
          <div v-if="eyebrow" class="mb-1 text-[11px] font-bold uppercase tracking-[0.18em] text-slate-400">
            {{ eyebrow }}
          </div>
          <h2 class="flex items-center gap-2 text-[18px] font-extrabold tracking-[-0.025em] text-slate-950">
            {{ title }}
          </h2>
          <p v-if="description" class="mt-1 max-w-2xl text-xs leading-5 text-slate-500">{{ description }}</p>
        </div>
        <div class="shrink-0 pt-0.5">
          <slot name="header-extra" />
        </div>
      </div>
    </div>
    <div class="min-h-0 flex-1 px-6 pb-6 pt-2">
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
