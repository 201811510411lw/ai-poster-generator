<template>
  <div class="flex h-[100dvh] min-w-0 flex-col overflow-hidden bg-[var(--page-bg)] text-slate-950" @click="closeUserMenu">
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
          <button
            ref="userButtonRef"
            class="flex h-11 items-center gap-3 rounded-full border border-slate-200 bg-white py-1 pl-1 pr-3 shadow-[0_6px_18px_rgba(15,23,42,0.035)] transition hover:border-violet-200 hover:bg-violet-50/40"
            type="button"
            @click.stop="toggleUserMenu"
          >
            <div class="grid h-9 w-9 place-items-center rounded-full bg-[linear-gradient(135deg,#EEF2FF,#F5F3FF)] text-sm font-extrabold text-[var(--primary)]">
              设
            </div>
            <div class="hidden text-left xl:block">
              <div class="text-sm font-bold leading-5 text-slate-800">设计师</div>
              <div class="text-xs text-slate-400">视觉设计师</div>
            </div>
            <ChevronDown
              :size="15"
              class="text-slate-400 transition"
              :class="{ 'rotate-180 text-[var(--primary)]': userMenuOpen }"
            />
          </button>
        </div>
      </div>
    </header>

    <main class="min-h-0 w-full min-w-0 flex-1 overflow-hidden px-8 py-5 2xl:px-10">
      <slot />
    </main>

    <Teleport to="body">
      <div
        v-if="userMenuOpen"
        class="fixed inset-0 z-[9998]"
        @click="closeUserMenu"
      />
      <div
        v-if="userMenuOpen"
        class="fixed z-[9999] w-44 overflow-hidden rounded-2xl border border-slate-200 bg-white p-2 shadow-[0_18px_46px_rgba(15,23,42,0.16)]"
        :style="userMenuStyle"
        @click.stop
      >
        <button
          v-for="item in userMenuItems"
          :key="item.key"
          class="flex w-full items-center gap-2 rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-slate-600 transition hover:bg-violet-50 hover:text-[var(--primary)]"
          type="button"
          @click="handleUserMenuSelect(item.key)"
        >
          <component :is="item.icon" :size="16" />
          <span>{{ item.label }}</span>
        </button>
        <div class="my-1 h-px bg-slate-100" />
        <button
          class="flex w-full items-center gap-2 rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-slate-600 transition hover:bg-red-50 hover:text-red-500"
          type="button"
          @click="handleUserMenuSelect('logout')"
        >
          <LogOut :size="16" />
          <span>退出登录</span>
        </button>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { Bell, ChevronDown, LogOut, PlusSquare, Settings, Sparkles, Sun, UserRound } from "lucide-vue-next";
import { NButton, useMessage } from "naive-ui";

const message = useMessage();
const userMenuOpen = ref(false);
const userButtonRef = ref<HTMLButtonElement | null>(null);
const userMenuPosition = ref({ top: 64, right: 24 });

const workflowSteps = [
  { index: 1, label: "基础设置", active: true },
  { index: 2, label: "素材管理", active: false },
  { index: 3, label: "生成设置", active: false },
  { index: 4, label: "生成结果", active: false },
];

const userMenuItems = [
  { label: "个人资料", key: "profile", icon: UserRound },
  { label: "消息通知", key: "notifications", icon: Bell },
  { label: "偏好设置", key: "settings", icon: Settings },
];

const userMenuStyle = computed(() => ({
  top: `${userMenuPosition.value.top}px`,
  right: `${userMenuPosition.value.right}px`,
}));

function updateUserMenuPosition() {
  const rect = userButtonRef.value?.getBoundingClientRect();
  if (!rect) return;

  userMenuPosition.value = {
    top: rect.bottom + 10,
    right: window.innerWidth - rect.right,
  };
}

function toggleUserMenu() {
  updateUserMenuPosition();
  userMenuOpen.value = !userMenuOpen.value;
}

function closeUserMenu() {
  userMenuOpen.value = false;
}

function handleUserMenuSelect(key: string) {
  const labelMap: Record<string, string> = {
    profile: "个人资料功能待接入",
    notifications: "消息通知功能待接入",
    settings: "偏好设置功能待接入",
    logout: "退出登录功能待接入",
  };

  userMenuOpen.value = false;
  message.info(labelMap[key] || "功能待接入");
}
</script>
