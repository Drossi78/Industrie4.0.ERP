import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ListQuery, Paginated, TableQuery, Uuid } from '../../../shared/models/common.model';
import { toHttpParams } from '../../../shared/utils/list-query-params';
import { Invoice, CreateInvoiceRequest, UpdateInvoiceRequest } from '../models/invoice.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InvoicesService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/invoices`;

  getAll(query?: ListQuery | TableQuery): Observable<Paginated<Invoice>> {
    // When a filter is active the backend returns a plain array (not a Page).
    return this.http.get<Invoice[] | Paginated<Invoice>>(this.base, { params: toHttpParams(query) }).pipe(
      map(res => Array.isArray(res) ? this.toPage(res) : res),
    );
  }

  private toPage(invoices: Invoice[]): Paginated<Invoice> {
    return {
      content: invoices,
      totalElements: invoices.length,
      totalPages: 1,
      number: 0,
      size: invoices.length,
      first: true,
      last: true,
      numberOfElements: invoices.length,
      empty: invoices.length === 0,
    };
  }

  getById(id: Uuid): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.base}/${id}`);
  }

  create(payload: CreateInvoiceRequest): Observable<Invoice> {
    return this.http.post<Invoice>(this.base, payload);
  }

  update(id: Uuid, payload: UpdateInvoiceRequest): Observable<Invoice> {
    return this.http.put<Invoice>(`${this.base}/${id}`, payload);
  }

  delete(id: Uuid): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
