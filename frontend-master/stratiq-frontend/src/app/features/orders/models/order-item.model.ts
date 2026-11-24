import { IsoDateTime, Uuid } from '../../../shared/models/common.model';
import { UnitOfMeasure } from '../../../shared/enums/unit-of-measure.enum';

export { UnitOfMeasure };

export interface OrderItem {
  id: Uuid;
  orderId: Uuid;
  itemId: Uuid;
  itemSku: string;
  itemName: string;
  unit: UnitOfMeasure;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  notes?: string;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
}

export interface CreateOrderItemRequest {
  itemId: Uuid;
  quantity: number;
  unitPrice: number;
  notes?: string;
}

export interface UpdateOrderItemRequest {
  quantity?: number;
  unitPrice?: number;
  notes?: string;
}
