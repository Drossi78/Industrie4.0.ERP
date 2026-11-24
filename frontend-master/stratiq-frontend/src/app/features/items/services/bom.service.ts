import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Uuid } from '../../../shared/models/common.model';
import { BomLine, CreateBomLineRequest } from '../models/bom.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BomService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/bom`;

  getByItem(itemId: Uuid): Observable<BomLine[]> {
    return this.http.get<BomLine[]>(this.base, { params: new HttpParams().set('itemId', itemId) });
  }

  create(payload: CreateBomLineRequest): Observable<BomLine> {
    return this.http.post<BomLine>(this.base, payload);
  }

  delete(id: Uuid): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
