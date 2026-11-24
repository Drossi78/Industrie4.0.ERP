import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Uuid } from '../../../shared/models/common.model';
import { ProductionStep, CreateProductionStepRequest, UpdateProductionStepRequest } from '../models/production-step.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProductionStepsService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/production-steps`;

  getByItem(itemId: Uuid): Observable<ProductionStep[]> {
    return this.http.get<ProductionStep[]>(this.base, { params: new HttpParams().set('itemId', itemId) });
  }

  create(payload: CreateProductionStepRequest): Observable<ProductionStep> {
    return this.http.post<ProductionStep>(this.base, payload);
  }

  update(stepId: Uuid, payload: UpdateProductionStepRequest): Observable<ProductionStep> {
    return this.http.put<ProductionStep>(`${this.base}/${stepId}`, payload);
  }

  delete(stepId: Uuid): Observable<void> {
    return this.http.delete<void>(`${this.base}/${stepId}`);
  }
}
