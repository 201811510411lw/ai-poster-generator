import type { AuthUser } from "@/types/auth";

export const TOKEN_KEY = "ai-poster-generator-token";
export const USER_KEY = "ai-poster-generator-user";

function getStoredItem(key: string) {
  return window.localStorage.getItem(key) || window.sessionStorage.getItem(key);
}

export function clearStoredAuth() {
  window.localStorage.removeItem(TOKEN_KEY);
  window.localStorage.removeItem(USER_KEY);
  window.sessionStorage.removeItem(TOKEN_KEY);
  window.sessionStorage.removeItem(USER_KEY);
}

export function writeStoredAuth(token: string, user: AuthUser, remember: boolean) {
  clearStoredAuth();

  const storage = remember ? window.localStorage : window.sessionStorage;
  storage.setItem(TOKEN_KEY, token);
  storage.setItem(USER_KEY, JSON.stringify(user));
}

export function readStoredToken() {
  return getStoredItem(TOKEN_KEY);
}

export function readStoredUser(): AuthUser | null {
  const raw = getStoredItem(USER_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    clearStoredAuth();
    return null;
  }
}

export function hasStoredAuth() {
  return Boolean(readStoredToken() && readStoredUser());
}
