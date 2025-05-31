import { User } from '../user.model';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface JwtResponse {
  id: number;
  username: string;
  email: string;
  roles: string[];
  token: string;
  tokenType?: string; // Usually 'Bearer'
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

// General message response from backend (e.g. for registration success)
export interface MessageResponse {
  message: string;
} 