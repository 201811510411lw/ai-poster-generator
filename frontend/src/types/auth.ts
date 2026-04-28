export interface LoginPayload {
  username: string;
  password: string;
  remember: boolean;
}

export interface AuthUser {
  id: string | number;
  username: string;
  nickname: string;
  avatar?: string;
  role?: string;
}

export interface LoginResponse {
  success: boolean;
  token: string;
  user: AuthUser;
  message?: string;
}

export interface LoginResult {
  token: string;
  user: AuthUser;
}
