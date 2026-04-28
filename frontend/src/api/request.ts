import axios, { AxiosError } from "axios";
import { readStoredToken } from "@/utils/authStorage";

export interface ApiResponse<T> {
  success: boolean;
  code?: string | number;
  message?: string;
  data: T;
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "";

const request = axios.create({
  baseURL: apiBaseUrl,
  timeout: 120000,
});

request.interceptors.request.use((config) => {
  const token = readStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => response,
  (error: AxiosError<{ message?: string }>) => {
    if (error.response?.status === 401) {
      window.dispatchEvent(new CustomEvent("auth:unauthorized"));
    }

    const message =
      error.response?.data?.message || error.message || "请求失败，请稍后重试";
    return Promise.reject(new Error(message));
  },
);

export async function get<T>(url: string) {
  const response = await request.get<ApiResponse<T>>(url);
  return unwrapResponse(response.data);
}

export async function post<T>(url: string, data?: unknown) {
  const response = await request.post<ApiResponse<T>>(url, data);
  return unwrapResponse(response.data);
}

export async function postForm<T>(url: string, formData: FormData) {
  const response = await request.post<ApiResponse<T>>(url, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
  return unwrapResponse(response.data);
}

function unwrapResponse<T>(response: ApiResponse<T>) {
  if (response.success === false) {
    throw new Error(response.message || "请求失败，请稍后重试");
  }

  return response.data;
}

export default request;
