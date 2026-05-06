import axios, { AxiosError, type AxiosRequestConfig } from "axios";
import { readStoredToken } from "@/utils/authStorage";

export const REQUEST_CANCELED_MESSAGE = "REQUEST_CANCELED";

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
    if (error.code === "ERR_CANCELED") {
      return Promise.reject(new Error(REQUEST_CANCELED_MESSAGE));
    }

    if (error.response?.status === 401) {
      window.dispatchEvent(new CustomEvent("auth:unauthorized"));
    }

    const message =
      error.response?.data?.message || error.message || "请求失败，请稍后重试";
    return Promise.reject(new Error(message));
  },
);

export async function get<T>(url: string, config?: AxiosRequestConfig) {
  const response = await request.get<ApiResponse<T>>(url, config);
  return unwrapResponse(response.data);
}

export async function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  const response = await request.post<ApiResponse<T>>(url, data, config);
  return unwrapResponse(response.data);
}

export async function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig) {
  const response = await request.put<ApiResponse<T>>(url, data, config);
  return unwrapResponse(response.data);
}

export async function del<T>(url: string, config?: AxiosRequestConfig) {
  const response = await request.delete<ApiResponse<T>>(url, config);
  return unwrapResponse(response.data);
}

export async function postForm<T>(url: string, formData: FormData, config?: AxiosRequestConfig) {
  const response = await request.post<ApiResponse<T>>(url, formData, {
    ...config,
    headers: {
      ...config?.headers,
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
