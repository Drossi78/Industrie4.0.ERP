import { User } from './user.model';

/** Entspricht dem Spring-`LoginRequest` (Feld `username`). */
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: User;
  /** Flat list of permission codenames the user's role grants, e.g. "inventory:write". */
  permissions: string[];
}
