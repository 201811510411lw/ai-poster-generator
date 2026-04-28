<template>
  <div class="flex h-[100dvh] min-w-0 flex-col overflow-hidden bg-[var(--page-bg)] text-slate-950" @click="closeUserMenu">
    <header class="z-30 h-16 shrink-0 border-b border-orange-100/80 bg-white/95 backdrop-blur-xl">
      <div class="flex h-16 w-full min-w-0 items-center justify-between gap-6 px-6 2xl:px-8">
        <div class="flex min-w-[280px] items-center gap-3">
          <div class="grid h-10 w-10 shrink-0 place-items-center rounded-2xl bg-[linear-gradient(135deg,#FFB347_0%,#F58220_52%,#F4511E_100%)] shadow-[0_10px_24px_rgba(245,130,32,0.26)] ring-1 ring-orange-200/70">
            <svg viewBox="0 0 48 48" class="h-6 w-6" fill="none" aria-hidden="true">
              <path
                d="M15.5 10.5h12.2l5.8 5.8v20.2a3 3 0 0 1-3 3h-15a3 3 0 0 1-3-3v-23a3 3 0 0 1 3-3Z"
                stroke="white"
                stroke-width="3.4"
                stroke-linejoin="round"
              />
              <path d="M27.5 10.8v6.5h6.5" stroke="white" stroke-width="3.4" stroke-linecap="round" stroke-linejoin="round" />
              <circle cx="19" cy="18.5" r="2.2" fill="white" />
              <circle cx="28.5" cy="27.5" r="2.2" fill="white" />
              <path d="M18 29 30 17" stroke="white" stroke-width="3.2" stroke-linecap="round" />
              <path d="M17 33h13" stroke="#FFD43B" stroke-width="3.4" stroke-linecap="round" />
              <path
                d="M36.5 29 38.8 33.2 43 35.5 38.8 37.8 36.5 42 34.2 37.8 30 35.5 34.2 33.2 36.5 29Z"
                fill="#FFD43B"
                stroke="white"
                stroke-width="2"
                stroke-linejoin="round"
              />
            </svg>
          </div>
          <div>
            <h1 class="text-[21px] font-extrabold leading-6 tracking-[-0.035em] text-[#2F1E12]">AI 海报生成器</h1>
            <p class="mt-0.5 text-[10px] font-bold uppercase tracking-[0.2em] text-[#9A7355]">AI Poster Generator</p>
          </div>
        </div>

        <nav class="hidden flex-1 justify-center lg:flex">
          <div class="flex items-center rounded-[20px] bg-orange-50/70 p-1 ring-1 ring-orange-100">
            <div
              v-for="step in workflowSteps"
              :key="step.label"
              class="flex h-10 items-center gap-2 rounded-2xl px-5 text-sm font-bold transition"
              :class="step.active ? 'bg-white text-[var(--primary)] shadow-sm ring-1 ring-orange-100' : 'text-[#8A6A52]'"
            >
              <span
                class="grid h-5 w-5 place-items-center rounded-full text-[11px] font-extrabold"
                :class="step.active ? 'bg-[var(--primary)] text-white' : 'bg-orange-100 text-[#9A7355]'"
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
          <button class="grid h-10 w-10 place-items-center rounded-full border border-orange-100 bg-white text-[#7A5434] transition hover:border-orange-200 hover:bg-orange-50 hover:text-[var(--primary)]">
            <Sun :size="17" />
          </button>
          <button
            ref="userButtonRef"
            class="flex h-11 items-center gap-3 rounded-full border border-orange-100 bg-white py-1 pl-1 pr-3 shadow-[0_6px_18px_rgba(107,74,47,0.045)] transition hover:border-orange-200 hover:bg-orange-50/50"
            type="button"
            @click.stop="toggleUserMenu"
          >
            <div class="grid h-9 w-9 place-items-center rounded-full bg-[linear-gradient(135deg,#FFF0DC,#FFE0B2)] text-sm font-extrabold text-[var(--primary)]">
              {{ avatarText }}
            </div>
            <div class="hidden text-left xl:block">
              <div class="text-sm font-bold leading-5 text-[#2F1E12]">{{ displayName }}</div>
              <div class="text-xs text-[#9A7355]">{{ accountName }}</div>
            </div>
            <ChevronDown
              :size="15"
              class="text-[#B08968] transition"
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
        class="fixed z-[9999] w-44 overflow-hidden rounded-2xl border border-orange-100 bg-white p-2 shadow-[0_18px_46px_rgba(107,74,47,0.16)]"
        :style="userMenuStyle"
        @click.stop
      >
        <button
          v-for="item in userMenuItems"
          :key="item.key"
          class="flex w-full items-center gap-2 rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-[#6B4A2F] transition hover:bg-orange-50 hover:text-[var(--primary)]"
          type="button"
          @click="handleUserMenuSelect(item.key)"
        >
          <component :is="item.icon" :size="16" />
          <span>{{ item.label }}</span>
        </button>
        <div class="my-1 h-px bg-orange-100" />
        <button
          class="flex w-full items-center gap-2 rounded-xl px-3 py-2.5 text-left text-sm font-semibold text-[#6B4A2F] transition hover:bg-red-50 hover:text-red-500"
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
import { useRouter } from "vue-router";
import { Bell, ChevronDown, LogOut, PlusSquare, Settings, Sun, UserRound } from "lucide-vue-next";
import { NButton, useMessage } from "naive-ui";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const message = useMessage();
const auth = useAuthStore();
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

const displayName = computed(() => auth.currentUser?.nickname || auth.currentUser?.username || "用户");
const accountName = computed(() => auth.currentUser?.username || "已登录账号");
const avatarText = computed(() => displayName.value.trim().slice(0, 1).toUpperCase() || "用");

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

async function handleUserMenuSelect(key: string) {
  const labelMap: Record<string, string> = {
    profile: "个人资料功能待接入",
    notifications: "消息通知功能待接入",
    settings: "偏好设置功能待接入",
  };

  userMenuOpen.value = false;

  if (key === "logout") {
    await auth.logoutWithRequest();
    message.success("已退出登录");
    await router.push("/login");
    return;
  }

  message.info(labelMap[key] || "功能待接入");
}
</script>
