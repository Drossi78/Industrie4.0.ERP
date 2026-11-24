import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ListQuery, Paginated, TableQuery, Uuid } from '../../../shared/models/common.model';
import { toHttpParams } from '../../../shared/utils/list-query-params';
import { Supplier, CreateSupplierRequest, UpdateSupplierRequest } from '../models/supplier.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SuppliersService {
  private http = inject(HttpClient);
  private baseSuppliers = `${environment.apiUrl}/suppliers`;

  getAll(query?: ListQuery | TableQuery): Observable<Paginated<Supplier>> {
    // When a name filter is active the backend returns a plain array (not a Page).
    return this.http.get<Supplier[] | Paginated<Supplier>>(this.baseSuppliers, { params: toHttpParams(query) }).pipe(
      map(res => Array.isArray(res) ? this.toPage(res) : res),
    );
  }

  private toPage(suppliers: Supplier[]): Paginated<Supplier> {
    return {
      content: suppliers,
      totalElements: suppliers.length,
      totalPages: 1,
      number: 0,
      size: suppliers.length,
      first: true,
      last: true,
      numberOfElements: suppliers.length,
      empty: suppliers.length === 0,
    };
  }

  getById(id: Uuid): Observable<Supplier> {
    return this.http.get<Supplier>(`${this.baseSuppliers}/${id}`);
  }

  create(payload: CreateSupplierRequest): Observable<Supplier> {
    return this.http.post<Supplier>(`${this.baseSuppliers}`, payload);
  }

  update(supplierId: Uuid, payload: UpdateSupplierRequest): Observable<Supplier> {
    return this.http.put<Supplier>(`${this.baseSuppliers}/${supplierId}`, payload);
  }

  delete(id: Uuid): Observable<void> {
    return this.http.delete<void>(`${this.baseSuppliers}/${id}`, {});
  }
}
