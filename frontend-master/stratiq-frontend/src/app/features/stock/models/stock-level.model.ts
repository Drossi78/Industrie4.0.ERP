import { Uuid } from '../../../shared/models/common.model';

// Current stock level for a single item, summed across all warehouses.
export interface StockLevel {
  itemId: Uuid;
  itemName: string;
  totalIn: number;
  totalOut: number;
  balance: number;
}

// Stock level of one item broken down per warehouse.
export interface WarehouseStock {
  warehouseId: Uuid;
  warehouseName: string;
  totalIn: number;
  totalOut: number;
  balance: number;
}
