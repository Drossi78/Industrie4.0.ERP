import { Uuid } from '../../../shared/models/common.model';

// A stock reservation blocking available stock for an item in a warehouse.
export type ReservationStatus = 'ACTIVE' | 'RELEASED' | 'FULFILLED';

export interface Reservation {
  id: Uuid;
  createdAt: string;
  itemId: Uuid | null;
  itemName: string | null;
  warehouseId: Uuid | null;
  warehouseName: string | null;
  quantity: number;
  status: ReservationStatus;
  note: string | null;
  orderId: Uuid | null;
  releasedAt: string | null;
}

export interface CreateReservationRequest {
  itemId: Uuid;
  warehouseId: Uuid;
  quantity: number;
  note?: string | null;
  orderId?: Uuid | null;
}
