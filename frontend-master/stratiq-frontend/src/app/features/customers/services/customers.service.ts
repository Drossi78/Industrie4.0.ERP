import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ListQuery, Paginated, Uuid } from '../../../shared/models/common.model';
import { toHttpParams } from '../../../shared/utils/list-query-params';
import { Customer, CreateCustomerRequest, UpdateCustomerRequest } from '../models/customer.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CustomersService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/customers`;

  getAll(query?: ListQuery): Observable<Paginated<Customer>> {
    return this.http.get<Paginated<Customer>>(this.base, { params: toHttpParams(query) });
  }

  getById(id: Uuid): Observable<Customer> {
    return this.http.get<Customer>(`${this.base}/${id}`);
  }

  create(payload: CreateCustomerRequest): Observable<Customer> {
    return this.http.post<Customer>(this.base, payload);
  }

  update(id: Uuid, payload: UpdateCustomerRequest): Observable<Customer> {
    return this.http.put<Customer>(`${this.base}/${id}`, payload);
  }

  delete(id: Uuid): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
