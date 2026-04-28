<template>
  <main class="login-page" @mousemove="handleMouseMove">
    <div class="cursor-glow" :style="cursorGlowStyle" />
    <section class="character-stage">
      <div class="stage-grid" />
      <div class="brand-copy">
        <div class="brand-badge">
          <Sparkles :size="18" />
          AI Visual Platform
        </div>
        <h1>AI 视觉平台</h1>
        <p>上传素材，快速生成海报、主图、活动图等营销视觉内容。</p>
      </div>

      <div class="crew-wrap" :style="crewStyle">
        <div
          v-for="character in characters"
          :key="character.key"
          class="crew-character"
          :class="[`crew-${character.key}`, actionClass]"
          :style="character.style"
        >
          <div class="face" :style="{ top: character.faceTop }">
            <span class="eye">
              <span class="pupil" :style="pupilStyle(character.intensity)" />
            </span>
            <span class="eye">
              <span class="pupil" :style="pupilStyle(character.intensity)" />
            </span>
          </div>
          <span class="mouth" :class="mouthClass" />
        </div>
      </div>
    </section>

    <section class="login-panel">
      <div class="panel-bg" />
      <div class="login-card">
        <div class="orbit-logo" @click="triggerAction('thanks')">
          <div class="orbit outer" />
          <div class="orbit inner" />
          <div class="logo-core">
            <Sparkles :size="42" />
          </div>
        </div>

        <div class="card-title">
          <span>登录账号</span>
          <h2>进入创作空间</h2>
        </div>

        <n-form ref="formRef" :model="form" :rules="rules" label-placement="top" @submit.prevent>
          <n-form-item label="用户名 / 邮箱" path="username">
            <n-input
              v-model:value="form.username"
              size="large"
              placeholder="请输入用户名或邮箱"
              clearable
              @focus="triggerAction('surprise')"
              @input="triggerAction('surprise')"
            >
              <template #prefix>
                <UserRound :size="17" />
              </template>
            </n-input>
          </n-form-item>

          <n-form-item label="密码" path="password">
            <n-input
              v-model:value="form.password"
              size="large"
              type="password"
              show-password-on="click"
              placeholder="请输入密码"
              @focus="triggerAction('blink')"
              @input="triggerAction('blink')"
            >
              <template #prefix>
                <LockKeyhole :size="17" />
              </template>
            </n-input>
          </n-form-item>

          <div class="login-options">
            <n-checkbox v-model:checked="form.remember" @update:checked="triggerAction('smile')">
              记住我 30 天
            </n-checkbox>
            <button type="button" class="text-link" @click="triggerAction('tilt')">忘记密码？</button>
          </div>

          <n-button
            class="login-button"
            type="primary"
            size="large"
            block
            attr-type="button"
            :loading="auth.loading"
            @mouseenter="triggerAction('smile')"
            @click="handleLogin"
          >
            <span>立即登录</span>
            <span class="shine" />
          </n-button>

          <div class="divider"><span>or</span></div>

          <n-button class="google-button" size="large" block secondary @click="triggerAction('shake')">
            <template #icon>
              <Mail :size="17" />
            </template>
            使用邮箱快捷登录
          </n-button>

          <p class="signup-tip">
            还没有账号？
            <button type="button" @click="triggerAction('thanks')">联系管理员开通</button>
          </p>
        </n-form>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import type { FormInst, FormRules } from "naive-ui";
import { computed, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { LockKeyhole, Mail, Sparkles, UserRound } from "lucide-vue-next";
import { NButton, NCheckbox, NForm, NFormItem, NInput, useMessage } from "naive-ui";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const message = useMessage();
const auth = useAuthStore();
const formRef = ref<FormInst | null>(null);

const form = reactive({
  username: "",
  password: "",
  remember: true,
});

const rules: FormRules = {
  username: [{ required: true, message: "请输入用户名或邮箱", trigger: ["blur", "input"] }],
  password: [{ required: true, message: "请输入密码", trigger: ["blur", "input"] }],
};

const mouseOffset = reactive({ x: 0, y: 0 });
const action = ref("idle");
let actionTimer = 0;

const characters = [
  { key: "orange", intensity: 0.8, faceTop: "34%", style: { left: "0px", bottom: "0px" } },
  { key: "blue", intensity: 1, faceTop: "14%", style: { left: "116px", bottom: "0px" } },
  { key: "black", intensity: 1.12, faceTop: "17%", style: { left: "246px", bottom: "4px" } },
  { key: "yellow", intensity: 0.92, faceTop: "26%", style: { left: "326px", bottom: "0px" } },
];

const actionClass = computed(() => (action.value === "idle" ? "" : `is-${action.value}`));
const mouthClass = computed(() => {
  if (action.value === "surprise") return "mouth-surprise";
  if (["smile", "thanks"].includes(action.value)) return "mouth-smile";
  return "";
});

const crewStyle = computed(() => ({
  transform: `translateX(-50%) rotate(${mouseOffset.x * 4}deg) translateY(${mouseOffset.y * 8}px)`,
}));

const cursorGlowStyle = computed(() => ({
  transform: `translate(${mouseOffset.x * 90}px, ${mouseOffset.y * 70}px)`,
}));

function handleMouseMove(event: MouseEvent) {
  const x = event.clientX / window.innerWidth - 0.5;
  const y = event.clientY / window.innerHeight - 0.5;
  mouseOffset.x = Math.max(-0.5, Math.min(0.5, x));
  mouseOffset.y = Math.max(-0.5, Math.min(0.5, y));
}

function pupilStyle(intensity: number) {
  return {
    transform: `translate(calc(-50% + ${mouseOffset.x * 12 * intensity}px), calc(-50% + ${mouseOffset.y * 12 * intensity}px))`,
  };
}

function triggerAction(nextAction: string) {
  window.clearTimeout(actionTimer);
  action.value = nextAction;
  actionTimer = window.setTimeout(() => {
    action.value = "idle";
  }, 900);
}

function getRedirectPath() {
  const redirect = route.query.redirect;
  return typeof redirect === "string" && redirect !== "/login" ? redirect : "/poster-generator";
}

async function handleLogin() {
  try {
    await formRef.value?.validate();
    await auth.login({ ...form });
    triggerAction("thanks");
    message.success("登录成功");
    await router.push(getRedirectPath());
  } catch (error) {
    triggerAction("shake");
    message.error(error instanceof Error ? error.message : "登录失败，请重试");
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.75fr) minmax(380px, 0.85fr);
  overflow: hidden;
  background: #f6f6f8;
  color: #1f2937;
}

.cursor-glow {
  position: absolute;
  left: 35%;
  top: 18%;
  width: 460px;
  height: 460px;
  border-radius: 999px;
  background: radial-gradient(circle, rgba(245, 130, 32, 0.28), transparent 64%);
  filter: blur(8px);
  pointer-events: none;
  transition: transform 180ms ease-out;
}

.character-stage {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at 16% 18%, rgba(255, 211, 61, 0.32), transparent 30%),
    radial-gradient(circle at 74% 28%, rgba(91, 59, 255, 0.16), transparent 30%),
    linear-gradient(135deg, #fffaf3 0%, #f8fafc 52%, #eef2ff 100%);
}

.stage-grid {
  position: absolute;
  inset: 0;
  opacity: 0.46;
  background-image:
    linear-gradient(rgba(47, 30, 18, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(47, 30, 18, 0.06) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: radial-gradient(circle at 50% 52%, black 0%, transparent 72%);
  animation: gridShift 14s linear infinite;
}

.brand-copy {
  position: absolute;
  left: clamp(32px, 7vw, 92px);
  top: clamp(36px, 8vw, 96px);
  max-width: 720px;
  z-index: 2;
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(245, 130, 32, 0.18);
  color: #8a4b13;
  font-weight: 800;
  box-shadow: 0 12px 28px rgba(245, 130, 32, 0.08);
  backdrop-filter: blur(12px);
}

.brand-copy h1 {
  margin: 24px 0 14px;
  max-width: none;
  font-size: clamp(52px, 6.2vw, 88px);
  line-height: 1.02;
  letter-spacing: -0.07em;
  color: #26190f;
  white-space: nowrap;
}

.brand-copy p {
  max-width: 520px;
  font-size: 16px;
  line-height: 1.8;
  color: rgba(47, 30, 18, 0.62);
}

.crew-wrap {
  position: absolute;
  left: 50%;
  bottom: clamp(70px, 14vh, 135px);
  width: 436px;
  height: 360px;
  transform-origin: 50% 85%;
  transition: transform 240ms ease-out;
}

.crew-character {
  position: absolute;
  box-shadow: 0 24px 44px rgba(47, 30, 18, 0.14);
  transform-origin: 50% 86%;
  animation: enterBounce 860ms cubic-bezier(0.22, 1.2, 0.36, 1) both;
  transition: transform 360ms cubic-bezier(0.22, 1.4, 0.36, 1);
}

.crew-orange {
  width: 190px;
  height: 110px;
  border-radius: 120px 120px 28px 28px;
  background: #ff8b2b;
  animation-delay: 0ms;
  z-index: 4;
}

.crew-blue {
  width: 150px;
  height: 330px;
  border-radius: 22px;
  background: #5b3bff;
  animation-delay: 80ms;
  z-index: 2;
}

.crew-black {
  width: 94px;
  height: 235px;
  border-radius: 18px;
  background: #14161c;
  animation-delay: 160ms;
  z-index: 3;
}

.crew-yellow {
  width: 110px;
  height: 185px;
  border-radius: 999px;
  background: #ffd33d;
  animation-delay: 240ms;
  z-index: 1;
}

.face {
  position: absolute;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 18px;
}

.eye {
  position: relative;
  width: 18px;
  height: 18px;
  overflow: hidden;
  border-radius: 999px;
  background: #fff;
  transition: height 120ms ease;
}

.pupil {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: #111827;
  transition: transform 80ms linear;
}

.mouth {
  position: absolute;
  left: 50%;
  top: 62%;
  width: 20px;
  height: 6px;
  border-radius: 999px;
  background: rgba(17, 24, 39, 0.35);
  transform: translateX(-50%);
  transition: all 180ms ease;
}

.crew-black .mouth {
  background: rgba(255, 255, 255, 0.52);
}

.mouth-smile {
  width: 26px;
  height: 14px;
  border: 3px solid rgba(17, 24, 39, 0.35);
  border-top: 0;
  background: transparent;
  border-radius: 0 0 999px 999px;
}

.crew-black .mouth-smile {
  border-color: rgba(255, 255, 255, 0.52);
}

.mouth-surprise {
  width: 12px;
  height: 16px;
  border-radius: 999px;
}

.is-blink .eye {
  height: 5px;
}

.is-smile,
.is-thanks {
  transform: translateY(-8px) rotate(-4deg);
}

.is-surprise {
  transform: translateY(-12px) scale(1.03);
}

.is-tilt {
  transform: rotate(8deg);
}

.is-shake {
  animation: shake 420ms ease-in-out both;
}

.login-panel {
  position: relative;
  min-height: 100vh;
  padding: clamp(18px, 3vw, 34px);
  display: flex;
  align-items: stretch;
  justify-content: center;
  background: rgba(252, 252, 254, 0.92);
}

.panel-bg {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 18% 12%, rgba(91, 59, 255, 0.12), transparent 34%),
    radial-gradient(circle at 86% 46%, rgba(56, 189, 248, 0.16), transparent 38%),
    linear-gradient(rgba(17, 24, 39, 0.045) 1px, transparent 1px),
    linear-gradient(90deg, rgba(17, 24, 39, 0.045) 1px, transparent 1px);
  background-size: auto, auto, 28px 28px, 28px 28px;
  animation: gridShift 12s linear infinite;
}

.login-card {
  position: relative;
  z-index: 1;
  width: min(100%, 430px);
  min-height: calc(100vh - clamp(36px, 6vw, 68px));
  padding: clamp(24px, 4vw, 36px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  border: 1px solid rgba(17, 24, 39, 0.06);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 26px 70px rgba(17, 24, 39, 0.14), inset 0 1px 0 rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(14px);
  animation: fadeUp 540ms ease-out both;
}

.orbit-logo {
  position: relative;
  width: clamp(180px, 26vw, 260px);
  height: clamp(180px, 26vw, 260px);
  margin: 0 auto 18px;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.orbit {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  border: 3px dashed rgba(17, 24, 39, 0.18);
}

.orbit.outer {
  animation: spin 55s linear infinite reverse;
}

.orbit.inner {
  inset: 13px;
  border-color: rgba(49, 126, 179, 0.28);
  animation: spin 35s linear infinite;
}

.logo-core {
  position: relative;
  width: 86px;
  height: 86px;
  display: grid;
  place-items: center;
  border-radius: 28px;
  color: #317eb3;
  background: linear-gradient(135deg, #effaff, #fff7ed);
  box-shadow: 0 18px 42px rgba(49, 126, 179, 0.18);
  animation: breathe 3.2s ease-in-out infinite;
}

.card-title {
  text-align: center;
  margin-bottom: 24px;
}

.card-title span {
  display: block;
  font-size: 28px;
  line-height: 1.1;
  font-weight: 900;
  letter-spacing: -0.04em;
  background: linear-gradient(90deg, #88b7d5, #5a91bf, #2260a3);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.card-title h2 {
  margin: 12px 0 0;
  font-size: clamp(34px, 5vw, 52px);
  line-height: 1;
  letter-spacing: -0.07em;
  color: #333;
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: -2px 0 18px;
}

.text-link,
.signup-tip button {
  border: 0;
  padding: 0;
  background: transparent;
  color: rgba(47, 30, 18, 0.72);
  font-weight: 800;
  cursor: pointer;
}

.login-button {
  position: relative;
  height: 46px;
  overflow: hidden;
  border-radius: 999px !important;
  font-weight: 900;
  background: linear-gradient(135deg, #111827 0%, #1f2937 48%, rgba(245, 130, 32, 0.92) 100%) !important;
  box-shadow: 0 14px 32px rgba(47, 30, 18, 0.2);
}

.login-button span:not(.shine) {
  position: relative;
  z-index: 1;
}

.shine {
  position: absolute;
  top: -40%;
  left: 0;
  width: 42%;
  height: 180%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.35), transparent);
  transform: translateX(-120%) skewX(-18deg);
  animation: shine 2.8s ease-in-out infinite;
}

.divider {
  position: relative;
  margin: 18px 0;
  text-align: center;
  color: rgba(17, 24, 39, 0.38);
  font-size: 12px;
  font-weight: 800;
}

.divider::before {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  height: 1px;
  background: rgba(17, 24, 39, 0.08);
}

.divider span {
  position: relative;
  padding: 0 12px;
  background: rgba(255, 255, 255, 0.92);
}

.google-button {
  height: 46px;
  border-radius: 999px !important;
  font-weight: 800;
}

.signup-tip {
  margin: 18px 0 0;
  text-align: center;
  color: rgba(17, 24, 39, 0.52);
  font-size: 13px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes gridShift {
  to { background-position: 180px 120px, -120px 160px, 0 0, 0 0; }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes breathe {
  50% { transform: scale(1.06); }
}

@keyframes shine {
  0% { transform: translateX(-120%) skewX(-18deg); opacity: 0; }
  35% { opacity: 0.35; }
  100% { transform: translateX(160%) skewX(-18deg); opacity: 0; }
}

@keyframes enterBounce {
  from { opacity: 0; transform: translateY(180px) scale(0.96); }
  70% { opacity: 1; transform: translateY(-12px) scale(1.02); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes shake {
  0%, 100% { transform: rotate(0); }
  25% { transform: rotate(-10deg); }
  50% { transform: rotate(10deg); }
  75% { transform: rotate(-7deg); }
}

@media (max-width: 980px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .character-stage {
    min-height: 46vh;
  }

  .login-panel {
    min-height: auto;
  }

  .login-card {
    min-height: auto;
    border-radius: 28px;
  }

  .crew-wrap {
    left: 50%;
    width: 436px;
    max-width: 86vw;
    height: 300px;
    bottom: 24px;
  }

  .brand-copy {
    position: relative;
    left: auto;
    top: auto;
    padding: 28px 28px 0;
  }

  .brand-copy h1 {
    font-size: 42px;
    letter-spacing: -0.06em;
    white-space: normal;
  }
}
</style>
