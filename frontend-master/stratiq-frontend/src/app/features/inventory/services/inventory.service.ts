import { EnvironmentInjector, Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InventoryItem, CreateInventoryItem, UpdateInventoryItem } from '../models/inventory.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/inventory`;

  getAll(): Observable<InventoryItem[]> {
    return this.http.get<InventoryItem[]>(`${this.base}/get-all`);
  }

  getById(id: string): Observable<InventoryItem> {
    return this.http.get<InventoryItem>(`${this.base}/${id}/data`);
  }

  create(payload: CreateInventoryItem): Observable<InventoryItem> {
    return this.http.post<InventoryItem>(`${this.base}/new`, payload);
  }

  update(id: string, payload: UpdateInventoryItem): Observable<InventoryItem> {
    return this.http.put<InventoryItem>(`${this.base}/${id}/edit`, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}/delete`);
  }
}