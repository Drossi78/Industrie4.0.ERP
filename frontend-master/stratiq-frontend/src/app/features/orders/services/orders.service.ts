import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ListQuery, Paginated, TableQuery, Uuid } from '../../../shared/models/common.model';
import { toHttpParams } from '../../../shared/utils/list-query-params';
import {
  Order,
  OrderStatus,
  CreateOrderRequest,
  UpdateOrderRequest,
  CancelOrderRequest,
} from '../models/orders.model';
import { OrderItemView, OrderItemCreatePayload, OrderFulfillPayload } from '../models/order-detail.model';
import { StockMovementResponse } from '../../stock/models/stock-operation.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class OrdersService {
  private http = inject(HttpClient);
  private base = `${environment.apiUrl}/orders`;
  private orderItemsBase = `${environment.apiUrl}/order-items`;
  private movementsBase = `${environment.apiUrl}/stock-movements`;

  // Stock movements booked for an order (goods receipt/issue), incl. batch info for traceability.
  getMovements(orderId: Uuid): Observable<StockMovementResponse[]> {
    const params = new HttpParams().set('orderId', orderId);
    return this.http.get<StockMovementResponse[]>(this.movementsBase, { params });
  }

  getAll(query?: ListQuery | TableQuery): Observable<Paginated<Order>> {
    // When a filter is active the backend returns a plain array (not a Page).
    return this.http.get<Order[] | Paginated<Order>>(this.base, { params: toHttpParams(query) }).pipe(
      map(res => Array.isArray(res) ? this.toPage(res) : res),
    );
  }

  private toPage(orders: Order[]): Paginated<Order> {
    return {
      content: orders,
      totalElements: orders.length,
      totalPages: 1,
      number: 0,
      size: orders.length,
      first: true,
      last: true,
      numberOfElements: orders.length,
      empty: orders.length === 0,
    };
  }

  getById(id: Uuid): Observable<Order> {
    return this.http.get<Order>(`${this.base}/${id}`);
  }

  create(payload: CreateOrderRequest): Observable<Order> {
    return this.http.post<Order>(this.base, payload);
  }

  // Create just the order header (line items are added separately via addItem).
  // Matches the backend OrderCreateRequest contract.
  createHeader(payload: {
    customerId?: Uuid | null;
    orderNumber?: string | null;
    direction?: 'IN' | 'OUT' | null;
    orderDate?: string | null;
    expectedDate?: string | null;
    notes?: string | null;
  }): Observable<Order> {
    return this.http.post<Order>(this.base, payload);
  }

  update(id: Uuid, payload: UpdateOrderRequest): Observable<Order> {
    return this.http.patch<Order>(`${this.base}/${id}`, payload);
  }

  // Change an order's status (backend update is PUT and accepts a status field).
  updateStatus(id: Uuid, status: OrderStatus): Observable<Order> {
    return this.http.put<Order>(`${this.base}/${id}`, { status });
  }

  confirm(id: Uuid): Observable<Order> {
    return this.http.post<Order>(`${this.base}/${id}/confirm`, {});
  }

  cancel(id: Uuid, payload?: CancelOrderRequest): Observable<Order> {
    return this.http.post<Order>(`${this.base}/${id}/cancel`, payload ?? {});
  }

  delete(id: Uuid): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  // Fulfill an order: book stock movements for all items (direction IN or OUT).
  fulfill(orderId: Uuid, payload: OrderFulfillPayload): Observable<StockMovementResponse[]> {
    return this.http.post<StockMovementResponse[]>(`${this.base}/${orderId}/fulfill`, payload);
  }

  // Order line items (backend contract: itemId + itemName + unit + quantity + unitPrice).

  getOrderItems(orderId: Uuid): Observable<OrderItemView[]> {
    return this.http.get<OrderItemView[]>(`${this.base}/${orderId}/items`);
  }

  addOrderItem(payload: OrderItemCreatePayload): Observable<OrderItemView> {
    return this.http.post<OrderItemView>(this.orderItemsBase, payload);
  }

  removeOrderItem(itemId: Uuid): Observable<void> {
    return this.http.delete<void>(`${this.orderItemsBase}/${itemId}`);
  }

}
