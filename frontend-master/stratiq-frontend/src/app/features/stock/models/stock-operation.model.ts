import { Uuid } from '../../../shared/models/common.model';

// Wareneingang: book a positive IN movement.
// batchNumber (optional) books the receipt onto a lot, created on first use;
// expiryDate is only applied when a new batch is created.
export interface GoodsReceiptRequest {
  itemId: Uuid;
  warehouseId: Uuid;
  quantity: number;
  batchNumber?: string | null;
  expiryDate?: string | null;
  note?: string | null;
  orderId?: Uuid | null;
}

// Warenausgang: book positive OUT movement(s).
// batchId (optional) takes stock from a specific lot; otherwise the backend
// allocates automatically across batches FEFO-then-FIFO, possibly spanning several.
export interface GoodsIssueRequest {
  itemId: Uuid;
  warehouseId: Uuid;
  quantity: number;
  batchId?: Uuid | null;
  note?: string | null;
  orderId?: Uuid | null;
}

// Derived available stock of a single batch for an item in a warehouse (balance > 0),
// ordered FEFO-then-FIFO. Used to populate the batch picker for a manual goods issue.
export interface BatchStock {
  batchId: Uuid;
  batchNumber: string;
  expiryDate: string | null;
  balance: number;
}

// Umlagerung: move stock between two warehouses.
export interface StockTransferRequest {
  itemId: Uuid;
  fromWarehouseId: Uuid;
  toWarehouseId: Uuid;
  quantity: number;
  note?: string | null;
}

// Fulfill an order: book movements for all its items in the given direction.
export interface OrderFulfillRequest {
  warehouseId: Uuid;
  direction: 'IN' | 'OUT';
}

// Mirrors the backend StockMovementResponse.
export interface StockMovementResponse {
  id: Uuid;
  createdAt: string;
  note: string | null;
  quantity: number;
  type: string;
  itemId: Uuid | null;
  orderId: Uuid | null;
  warehouseId: Uuid | null;
  batchId: Uuid | null;
  batchNumber: string | null;
  expiryDate: string | null;
}
