import { get, post } from "@/api/request";
import type { AuthUser, LoginPayload, LoginResponse, LoginResult } from "@/types/auth";

export async function login(payload: LoginPayload): Promise<LoginResponse> {
  const result = await post<LoginResult>("/api/auth/login", payload);
  return {
    success: true,
    token: result.token,
    user: result.user,
    message: "登录成功",
  };
}

export async function fetchCurrentUser(): Promise<AuthUser> {
  return get<AuthUser>("/api/auth/me");
}

export async function logoutRequest() {
  await post<void>("/api/auth/logout");
}
