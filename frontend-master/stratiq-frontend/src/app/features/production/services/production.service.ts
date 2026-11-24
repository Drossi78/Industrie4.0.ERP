import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Uuid } from '../../../shared/models/common.model';
import { ProductionStepStatus } from '../../../shared/enums/production-step-status.enum';
import { OrderProductionStep } from '../../orders/models/order-production.model';
import { ProductionOrderDetail, ProductionOrderSummary } from '../models/production-dashboard.model';
import { environment } from '../../../../environments/environment';

export interface UpdateStepRequest {
  status?: ProductionStepStatus;
  assignedTo?: Uuid | null;
  warehouseId?: Uuid | null;
}

@Injectable({ providedIn: 'root' })
export class ProductionService {
  private readonly http = inject(HttpClient);
  private readonly ordersBase = `${environment.apiUrl}/production/orders`;
  private readonly stepsBase = `${environment.apiUrl}/production/steps`;

  getOrders(): Observable<ProductionOrderSummary[]> {
    return this.http.get<ProductionOrderSummary[]>(this.ordersBase);
  }

  getOrderDetail(orderId: Uuid): Observable<ProductionOrderDetail> {
    return this.http.get<ProductionOrderDetail>(`${this.ordersBase}/${orderId}`);
  }

  updateStep(stepId: Uuid, payload: UpdateStepRequest): Observable<OrderProductionStep> {
    return this.http.patch<OrderProductionStep>(`${this.stepsBase}/${stepId}`, payload);
  }
}
