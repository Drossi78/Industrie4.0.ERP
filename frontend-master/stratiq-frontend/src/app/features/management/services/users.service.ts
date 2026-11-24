import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ListQuery, Paginated, Uuid } from '../../../shared/models/common.model';
import { toHttpParams } from '../../../shared/utils/list-query-params';
import { AppUser, AppRole, CreateUserRequest, UpdateUserRequest } from '../models/app-user.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/users`;
  private readonly rolesBase = `${environment.apiUrl}/roles`;

  getAll(query?: ListQuery): Observable<Paginated<AppUser>> {
    return this.http.get<Paginated<AppUser>>(this.base, { params: toHttpParams(query) });
  }

  getActive(): Observable<AppUser[]> {
    return this.http.get<AppUser[] | Paginated<AppUser>>(`${this.base}?active=true`).pipe(
      map(res => Array.isArray(res) ? res : res.content),
    );
  }

  getRoles(): Observable<AppRole[]> {
    return this.http.get<AppRole[]>(this.rolesBase);
  }

  getById(id: Uuid): Observable<AppUser> {
    return this.http.get<AppUser>(`${this.base}/${id}`);
  }

  create(payload: CreateUserRequest): Observable<AppUser> {
    return this.http.post<AppUser>(this.base, payload);
  }

  update(id: Uuid, payload: UpdateUserRequest): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.base}/${id}`, payload);
  }

  delete(id: Uuid): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
