import { Uuid } from '../../../shared/models/common.model';

// Order line item as returned by the backend (OrderItemResponse).
export interface OrderItemView {
  id: Uuid;
  createdAt: string;
  orderId: Uuid;
  itemId: Uuid;
  itemName: string;
  unit: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  notes: string | null;
  supplierId: Uuid | null;
}

// Payload for creating a line item (POST /api/v1/order-items).
export interface OrderItemCreatePayload {
  orderId: Uuid;
  itemId: Uuid;
  itemName: string;
  unit: string;
  quantity: number;
  unitPrice: number;
  notes?: string | null;
  supplierId?: Uuid | null;
}

// Per-item batch metadata for a goods receipt (direction IN). Optional.
export interface OrderFulfillBatchLine {
  itemId: Uuid;
  batchNumber?: string | null;
  expiryDate?: string | null;
}

// Payload for fulfilling an order (POST /api/v1/orders/{id}/fulfill).
// warehouseId is the target for IN; for OUT it is ignored (auto-allocated across warehouses).
// batchLines carry per-item batch number/expiry, only used for direction IN.
export interface OrderFulfillPayload {
  warehouseId?: Uuid | null;
  direction: 'IN' | 'OUT';
  batchLines?: OrderFulfillBatchLine[] | null;
}
