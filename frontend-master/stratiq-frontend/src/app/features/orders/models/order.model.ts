import { IsoDateTime, Uuid } from '../../../shared/models/common.model';
import { OrderStatus } from '../../../shared/enums/order-status.enum';

export { OrderStatus };

export type OrderType = 'SALES' | 'PURCHASE';

// Stock direction of an order, chosen at creation: IN = goods receipt, OUT = goods issue.
export type OrderDirection = 'IN' | 'OUT';

export interface Order {
  id: Uuid;
  orderNumber: string | null;
  status: OrderStatus;
  direction: OrderDirection;
  customerId: Uuid | null;
  orderDate: IsoDateTime;
  expectedDate: IsoDateTime | null;
  confirmedAt: IsoDateTime | null;
  cancelledAt: IsoDateTime | null;
  totalAmount: number;
  notes: string | null;
  createdAt: IsoDateTime;
}

export interface CreateOrderRequest {
  type: OrderType;
  customerId?: Uuid | null;
  supplierId?: Uuid | null;
  warehouseId: Uuid;
  orderDate?: IsoDateTime;
  expectedDate?: IsoDateTime | null;
  notes?: string;
  currency?: string;
}

export interface UpdateOrderRequest {
  customerId?: Uuid | null;
  supplierId?: Uuid | null;
  warehouseId?: Uuid;
  orderDate?: IsoDateTime;
  expectedDate?: IsoDateTime | null;
  notes?: string;
  currency?: string;
}

export interface CancelOrderRequest {
  reason?: string;
}
