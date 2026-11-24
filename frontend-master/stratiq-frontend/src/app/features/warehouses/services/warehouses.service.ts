import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ListQuery, Paginated, Uuid } from '../../../shared/models/common.model';
import { toHttpParams } from '../../../shared/utils/list-query-params';
import { CreateWarehouseRequest, UpdateWarehouseRequest, Warehouse } from '../models/warehouses.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WarehousesService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/warehouses`;

  getAll(query?: ListQuery): Observable<Paginated<Warehouse>> {
    // The backend returns a flat array when filtering by city or active (not a Page).
    return this.http.get<Warehouse[] | Paginated<Warehouse>>(this.base, { params: toHttpParams(query) }).pipe(
      map(res => Array.isArray(res) ? this.toPage(res) : res),
    );
  }

  private toPage(warehouses: Warehouse[]): Paginated<Warehouse> {
    return {
      content: warehouses,
      totalElements: warehouses.length,
      totalPages: 1,
      number: 0,
      size: warehouses.length,
      first: true,
      last: true,
      numberOfElements: warehouses.length,
      empty: warehouses.length === 0,
    };
  }

  getById(id: Uuid): Observable<Warehouse> {
    return this.http.get<Warehouse>(`${this.base}/${id}`);
  }

  create(payload: CreateWarehouseRequest): Observable<Warehouse> {
    return this.http.post<Warehouse>(this.base, payload);
  }

  update(id: Uuid, payload: UpdateWarehouseRequest): Observable<Warehouse> {
    return this.http.put<Warehouse>(`${this.base}/${id}`, payload);
  }

  delete(id: Uuid): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
