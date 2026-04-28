import { computed, ref } from "vue";
import { defineStore } from "pinia";
import { fetchCurrentUser, login as loginRequest, logoutRequest } from "@/api/auth";
import type { AuthUser, LoginPayload } from "@/types/auth";
import {
  clearStoredAuth,
  readStoredToken,
  readStoredUser,
  writeStoredAuth,
} from "@/utils/authStorage";

export const useAuthStore = defineStore("auth", () => {
  const token = ref(readStoredToken());
  const currentUser = ref<AuthUser | null>(readStoredUser());
  const loading = ref(false);
  const userLoading = ref(false);

  const isLoggedIn = computed(() => Boolean(token.value && currentUser.value));

  async function login(payload: LoginPayload) {
    loading.value = true;
    try {
      const response = await loginRequest(payload);
      token.value = response.token;
      currentUser.value = response.user;
      writeStoredAuth(response.token, response.user, payload.remember);

      return response;
    } finally {
      loading.value = false;
    }
  }

  async function loadCurrentUser() {
    if (!token.value) return null;

    userLoading.value = true;
    try {
      const user = await fetchCurrentUser();
      currentUser.value = user;
      return user;
    } catch (error) {
      logout();
      throw error;
    } finally {
      userLoading.value = false;
    }
  }

  async function logoutWithRequest() {
    try {
      await logoutRequest();
    } finally {
      logout();
    }
  }

  function logout() {
    token.value = null;
    currentUser.value = null;
    clearStoredAuth();
  }

  return {
    token,
    currentUser,
    loading,
    userLoading,
    isLoggedIn,
    login,
    loadCurrentUser,
    logout,
    logoutWithRequest,
  };
});
