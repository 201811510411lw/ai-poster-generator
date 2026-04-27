<template>
  <div class="flex h-[100dvh] min-w-0 flex-col overflow-hidden bg-[var(--page-bg)] text-slate-950">
    <header class="z-30 h-16 shrink-0 border-b border-slate-200/70 bg-white/95 backdrop-blur-xl">
      <div class="flex h-16 w-full min-w-0 items-center justify-between gap-6 px-6 2xl:px-8">
        <div class="flex min-w-[260px] items-center gap-3">
          <div class="grid h-10 w-10 place-items-center rounded-2xl bg-[linear-gradient(135deg,#5B50DF,#A78BFA)] shadow-[0_10px_24px_rgba(108,92,231,0.22)]">
            <Sparkles :size="22" class="text-white" />
          </div>
          <div>
            <h1 class="text-[21px] font-extrabold leading-6 tracking-[-0.035em] text-slate-950">AI 海报生成器</h1>
            <p class="mt-0.5 text-[10px] font-bold uppercase tracking-[0.2em] text-slate-400">AI Poster Generator</p>
          </div>
        </div>

        <nav class="hidden flex-1 justify-center lg:flex">
          <div class="flex items-center rounded-[20px] bg-slate-50/90 p-1 ring-1 ring-slate-100">
            <div
              v-for="step in workflowSteps"
              :key="step.label"
              class="flex h-10 items-center gap-2 rounded-2xl px-5 text-sm font-bold transition"
              :class="step.active ? 'bg-white text-[var(--primary)] shadow-sm ring-1 ring-violet-100' : 'text-slate-500'"
            >
              <span
                class="grid h-5 w-5 place-items-center rounded-full text-[11px] font-extrabold"
                :class="step.active ? 'bg-[var(--primary)] text-white' : 'bg-slate-200 text-slate-500'"
              >
                {{ step.index }}
              </span>
              {{ step.label }}
            </div>
          </div>
        </nav>

        <div class="flex min-w-[320px] items-center justify-end gap-3">
          <n-button secondary class="h-10 px-4 font-bold">
            <template #icon>
              <PlusSquare :size="15" />
            </template>
            新建项目
          </n-button>
          <button class="grid h-10 w-10 place-items-center rounded-full border border-slate-200 bg-white text-slate-600 transition hover:border-violet-200 hover:bg-violet-50 hover:text-[var(--primary)]">
            <Sun :size="17" />
          </button>
          <n-dropdown trigger="click" :options="userMenuOptions" @select="handleUserMenuSelect">
            <button class="flex h-11 items-center gap-3 rounded-full border border-slate-200 bg-white py-1 pl-1 pr-3 shadow-[0_6px_18px_rgba(15,23,42,0.035)] transition hover:border-violet-200 hover:bg-violet-50/40">
              <div class="grid h-9 w-9 place-items-center rounded-full bg-[linear-gradient(135deg,#EEF2FF,#F5F3FF)] text-sm font-extrabold text-[var(--primary)]">
                设
              </div>
              <div class="hidden text-left xl:block">
                <div class="text-sm font-bold leading-5 text-slate-800">设计师</div>
                <div class="text-xs text-slate-400">视觉设计师</div>
              </div>
              <ChevronDown :size="15" class="text-slate-400" />
            </button>
          </n-dropdown>
        </div>
      </div>
    </header>

    <main class="min-h-0 w-full min-w-0 flex-1 overflow-hidden px-8 py-5 2xl:px-10">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { h } from "vue";
import type { Component } from "vue";
import { Bell, ChevronDown, LogOut, PlusSquare, Settings, Sparkles, Sun, UserRound } from "lucide-vue-next";
import { NButton, NDropdown, useMessage } from "naive-ui";

const message = useMessage();

const workflowSteps = [
  { index: 1, label: "基础设置", active: true },
  { index: 2, label: "素材管理", active: false },
  { index: 3, label: "生成设置", active: false },
  { index: 4, label: "生成结果", active: false },
];

function renderIcon(icon: Component) {
  return () => h(icon, { size: 16, strokeWidth: 2 });
}

const userMenuOptions = [
  {
    label: "个人资料",
    key: "profile",
    icon: renderIcon(UserRound),
  },
  {
    label: "消息通知",
    key: "notifications",
    icon: renderIcon(Bell),
  },
  {
    label: "偏好设置",
    key: "settings",
    icon: renderIcon(Settings),
  },
  {
    type: "divider",
    key: "divider",
  },
  {
    label: "退出登录",
    key: "logout",
    icon: renderIcon(LogOut),
  },
];

function handleUserMenuSelect(key: string | number) {
  const labelMap: Record<string, string> = {
    profile: "个人资料功能待接入",
    notifications: "消息通知功能待接入",
    settings: "偏好设置功能待接入",
    logout: "退出登录功能待接入",
  };

  message.info(labelMap[String(key)] || "功能待接入");
}
</script>
