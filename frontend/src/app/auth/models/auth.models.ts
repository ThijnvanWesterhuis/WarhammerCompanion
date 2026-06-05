export type Role = 'USER' | 'ADMIN';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  role: Role;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  role: Role;
}
