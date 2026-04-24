<template>
  <n-config-provider :theme-overrides="themeOverrides">
    <n-dialog-provider>
      <n-notification-provider>
        <n-message-provider>
          <router-view />
        </n-message-provider>
      </n-notification-provider>
    </n-dialog-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import type { GlobalThemeOverrides } from "naive-ui";
import {
  NConfigProvider,
  NDialogProvider,
  NMessageProvider,
  NNotificationProvider,
} from "naive-ui";

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: "#6C5CE7",
    primaryColorHover: "#5B50DF",
    primaryColorPressed: "#4F46E5",
    primaryColorSuppl: "#8B5CF6",
    borderRadius: "12px",
    borderColor: "#E5E7EB",
    textColorBase: "#111827",
    bodyColor: "#ffffff",
    cardColor: "#ffffff",
    modalColor: "#ffffff",
    popoverColor: "#ffffff",
  },
  Card: {
    borderRadius: "20px",
  },
  Input: {
    borderHover: "#A78BFA",
    boxShadowFocus: "0 0 0 3px rgba(108, 92, 231, 0.14)",
  },
  Select: {
    peers: {
      InternalSelection: {
        borderHover: "#A78BFA",
        boxShadowFocus: "0 0 0 3px rgba(108, 92, 231, 0.14)",
      },
    },
  },
  Button: {
    borderRadiusMedium: "12px",
    borderRadiusLarge: "12px",
  },
};
</script>
