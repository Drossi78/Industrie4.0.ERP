import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ListQuery, Paginated, TableQuery, Uuid } from '../../../shared/models/common.model';
import { toHttpParams } from '../../../shared/utils/list-query-params';
import { Item, CreateItemRequest, UpdateItemRequest } from '../models/items.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ItemsService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/items`;

  getAll(query?: ListQuery | TableQuery): Observable<Paginated<Item>> {
    return this.http.get<Item[] | Paginated<Item>>(this.base, { params: toHttpParams(query) }).pipe(
      map(res => Array.isArray(res) ? this.toPage(res) : res),
    );
  }

  private toPage(items: Item[]): Paginated<Item> {
    return {
      content: items,
      totalElements: items.length,
      totalPages: 1,
      number: 0,
      size: items.length,
      first: true,
      last: true,
      numberOfElements: items.length,
      empty: items.length === 0,
    };
  }

  getById(id: Uuid): Observable<Item> {
    return this.http.get<Item>(`${this.base}/${id}`);
  }

  create(payload: CreateItemRequest): Observable<Item> {
    return this.http.post<Item>(this.base, payload);
  }

  update(id: Uuid, payload: UpdateItemRequest): Observable<Item> {
    return this.http.put<Item>(`${this.base}/${id}`, payload);
  }

  delete(id: Uuid): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
