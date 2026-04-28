import { get, post } from "@/api/request";
import type { AuthUser, LoginPayload, LoginResponse, LoginResult } from "@/types/auth";

const sleep = (ms: number) => new Promise((resolve) => window.setTimeout(resolve, ms));
const useMockAuth = import.meta.env.VITE_USE_MOCK_AUTH !== "false";

export async function login(payload: LoginPayload): Promise<LoginResponse> {
  if (useMockAuth) {
    return mockLogin(payload);
  }

  const result = await post<LoginResult>("/api/auth/login", payload);
  return {
    success: true,
    token: result.token,
    user: result.user,
    message: "登录成功",
  };
}

export async function fetchCurrentUser(): Promise<AuthUser> {
  if (useMockAuth) {
    const raw = window.localStorage.getItem("ai-poster-generator-user") || window.sessionStorage.getItem("ai-poster-generator-user");
    if (!raw) {
      throw new Error("登录已失效，请重新登录");
    }
    return JSON.parse(raw) as AuthUser;
  }

  return get<AuthUser>("/api/auth/me");
}

export async function logoutRequest() {
  if (useMockAuth) {
    return;
  }

  await post<void>("/api/auth/logout");
}

async function mockLogin(payload: LoginPayload): Promise<LoginResponse> {
  await sleep(700);

  const username = payload.username.trim();
  if (!username || !payload.password) {
    throw new Error("请输入用户名和密码");
  }

  return {
    success: true,
    token: `mock_token_${Date.now()}`,
    user: {
      id: "demo-user",
      username,
      nickname: username.includes("@") ? username.split("@")[0] : username,
      role: "USER",
    },
    message: "登录成功",
  };
}
