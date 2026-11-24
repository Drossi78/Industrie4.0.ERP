import { Injectable, computed, signal } from '@angular/core';
import type { LoginResponse } from '../models/auth.model';
import type { User } from '../models/user.model';
import type { PermissionCode } from '../../../shared/enums/permission-code.enum';

const ACCESS = 'stratiq.accessToken';
const REFRESH = 'stratiq.refreshToken';
const USER = 'stratiq.user';
const PERMISSIONS = 'stratiq.permissions';

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  private readonly accessToken = signal<string | null>(this.read(ACCESS));
  private readonly refreshToken = signal<string | null>(this.read(REFRESH));
  private readonly user = signal<User | null>(this.readUser());
  private readonly permissions = signal<string[]>(this.readPermissions());

  readonly isAuthenticated = computed(
    () => !!this.accessToken() && !!this.user(),
  );

  getAccessToken(): string | null {
    return this.accessToken();
  }

  getRefreshToken(): string | null {
    return this.refreshToken();
  }

  getUser(): User | null {
    return this.user();
  }

  getPermissions(): string[] {
    return this.permissions();
  }

  /** UI-only gate — the backend still enforces every permission server-side. */
  hasPermission(code: PermissionCode): boolean {
    return this.permissions().includes(code);
  }

  hasAnyPermission(...codes: PermissionCode[]): boolean {
    return codes.some(code => this.hasPermission(code));
  }

  applyLogin(response: LoginResponse): void {
    this.write(ACCESS, response.accessToken);
    this.write(REFRESH, response.refreshToken);
    this.write(USER, JSON.stringify(response.user));
    this.write(PERMISSIONS, JSON.stringify(response.permissions));
    this.accessToken.set(response.accessToken);
    this.refreshToken.set(response.refreshToken);
    this.user.set(response.user);
    this.permissions.set(response.permissions);
  }

  clear(): void {
    this.remove(ACCESS);
    this.remove(REFRESH);
    this.remove(USER);
    this.remove(PERMISSIONS);
    this.accessToken.set(null);
    this.refreshToken.set(null);
    this.user.set(null);
    this.permissions.set([]);
  }

  private read(key: string): string | null {
    try {
      return sessionStorage.getItem(key);
    } catch {
      return null;
    }
  }

  private readUser(): User | null {
    const raw = this.read(USER);
    if (!raw) {
      return null;
    }
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  }

  private readPermissions(): string[] {
    const raw = this.read(PERMISSIONS);
    if (!raw) {
      return [];
    }
    try {
      return JSON.parse(raw) as string[];
    } catch {
      return [];
    }
  }

  private write(key: string, value: string): void {
    try {
      sessionStorage.setItem(key, value);
    } catch {
      /* ignore */
    }
  }

  private remove(key: string): void {
    try {
      sessionStorage.removeItem(key);
    } catch {
      /* ignore */
    }
  }
}
