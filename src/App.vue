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
    primaryColor: "#F58220",
    primaryColorHover: "#EA6F10",
    primaryColorPressed: "#D85F08",
    primaryColorSuppl: "#FFB347",
    borderRadius: "12px",
    borderColor: "#F1E1D0",
    textColorBase: "#2F1E12",
    bodyColor: "#ffffff",
    cardColor: "#ffffff",
    modalColor: "#ffffff",
    popoverColor: "#ffffff",
  },
  Card: {
    borderRadius: "20px",
  },
  Input: {
    borderHover: "#FFB347",
    boxShadowFocus: "0 0 0 3px rgba(245, 130, 32, 0.14)",
  },
  Select: {
    peers: {
      InternalSelection: {
        borderHover: "#FFB347",
        boxShadowFocus: "0 0 0 3px rgba(245, 130, 32, 0.14)",
      },
    },
  },
  Button: {
    borderRadiusMedium: "12px",
    borderRadiusLarge: "12px",
  },
};
</script>
