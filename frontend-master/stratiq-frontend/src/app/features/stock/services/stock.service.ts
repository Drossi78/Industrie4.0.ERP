import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Uuid } from '../../../shared/models/common.model';
import { StockLevel, WarehouseStock } from '../models/stock-level.model';
import {
  GoodsReceiptRequest,
  GoodsIssueRequest,
  StockTransferRequest,
  OrderFulfillRequest,
  StockMovementResponse,
  BatchStock,
} from '../models/stock-operation.model';
import { Reservation, CreateReservationRequest } from '../models/reservation.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class StockService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/stock`;
  private readonly ordersBase = `${environment.apiUrl}/orders`;

  // Overview: current balance for every item, summed across warehouses.
  getLevels(): Observable<StockLevel[]> {
    return this.http.get<StockLevel[]>(`${this.base}/levels`);
  }

  // Per-warehouse breakdown of one item's stock.
  getByWarehouse(itemId: Uuid): Observable<WarehouseStock[]> {
    return this.http.get<WarehouseStock[]>(`${this.base}/levels/${itemId}/warehouses`);
  }

  // Per-item stock levels within a single warehouse.
  getLevelsForWarehouse(warehouseId: Uuid): Observable<StockLevel[]> {
    return this.http.get<StockLevel[]>(`${this.base}/warehouses/${warehouseId}/levels`);
  }

  // Available batches (balance > 0) for an item in a warehouse, ordered FEFO/FIFO.
  getBatchStock(itemId: Uuid, warehouseId: Uuid): Observable<BatchStock[]> {
    const params = new HttpParams().set('itemId', itemId).set('warehouseId', warehouseId);
    return this.http.get<BatchStock[]>(`${this.base}/batches`, { params });
  }

  // Wareneingang: book an IN movement into a warehouse.
  goodsReceipt(payload: GoodsReceiptRequest): Observable<StockMovementResponse> {
    return this.http.post<StockMovementResponse>(`${this.base}/goods-receipt`, payload);
  }

  // Warenausgang: book OUT movement(s). Auto-allocates across batches (FEFO/FIFO),
  // so the response may contain one movement per consumed lot.
  goodsIssue(payload: GoodsIssueRequest): Observable<StockMovementResponse[]> {
    return this.http.post<StockMovementResponse[]>(`${this.base}/goods-issue`, payload);
  }

  // Umlagerung: move stock between two warehouses (returns the two movements).
  transfer(payload: StockTransferRequest): Observable<StockMovementResponse[]> {
    return this.http.post<StockMovementResponse[]>(`${this.base}/transfer`, payload);
  }

  // List reservations (defaults to ACTIVE on the backend when no status is given).
  getReservations(status?: string): Observable<Reservation[]> {
    const params = status ? new HttpParams().set('status', status) : undefined;
    return this.http.get<Reservation[]>(`${this.base}/reservations`, { params });
  }

  // Reserve stock (fails if available stock is insufficient).
  createReservation(payload: CreateReservationRequest): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.base}/reservations`, payload);
  }

  // Release an active reservation, freeing the blocked stock.
  releaseReservation(id: Uuid): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.base}/reservations/${id}/release`, {});
  }

  // Fulfill an order: book movements for all its items, linked to the order.
  fulfillOrder(orderId: Uuid, payload: OrderFulfillRequest): Observable<StockMovementResponse[]> {
    return this.http.post<StockMovementResponse[]>(`${this.ordersBase}/${orderId}/fulfill`, payload);
  }
}
