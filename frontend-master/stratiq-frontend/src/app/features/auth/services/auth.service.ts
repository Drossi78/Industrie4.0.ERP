import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import {
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  RegisterUserRequest,
  PasswordChangeRequest,
  PasswordResetRequestRequest,
  PasswordResetRequest,
  User,
} from '../models/auth.model';
import { environment } from '../../../../environments/environment';

/** Antwort des Spring-`AuthController`. */
interface BackendLoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  permissions: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private base = `${environment.authUrl}/auth`;

  register(payload: RegisterUserRequest): Observable<User> {
    return this.http.post<User>(`${this.base}/register`, payload);
  }

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http
      .post<BackendLoginResponse>(`${this.base}/login`, {
        username: payload.username,
        password: payload.password,
      })
      .pipe(map((r) => this.mapBackendLogin(r)));
  }

  private mapBackendLogin(r: BackendLoginResponse): LoginResponse {
    const now = new Date().toISOString();
    return {
      accessToken: r.accessToken,
      refreshToken: r.refreshToken,
      expiresIn: r.expiresIn,
      user: {
        id: '00000000-0000-4000-8000-000000000001',
        email: r.username,
        firstName: r.username,
        lastName: '',
        role: 'read_only',
        isActive: true,
        createdAt: now,
        updatedAt: now,
      },
      permissions: r.permissions ?? [],
    };
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.base}/logout`, {});
  }

  refresh(payload: RefreshTokenRequest): Observable<RefreshTokenResponse> {
    return this.http.post<RefreshTokenResponse>(`${this.base}/refresh`, payload);
  }

  changePassword(payload: PasswordChangeRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/change-password`, payload);
  }

  requestPasswordReset(payload: PasswordResetRequestRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/password/reset-request`, payload);
  }

  resetPassword(payload: PasswordResetRequest): Observable<void> {
    return this.http.post<void>(`${this.base}/password/reset`, payload);
  }
}
