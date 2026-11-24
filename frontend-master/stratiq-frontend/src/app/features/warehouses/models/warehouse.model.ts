import { IsoDateTime, Uuid } from '../../../shared/models/common.model';

export interface Warehouse {
  id: Uuid;
  name: string;
  description?: string | null;
  street?: string | null;
  houseNumber?: string | null;
  zipCode?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  isActive: boolean;
  createdAt: IsoDateTime;
}

export interface CreateWarehouseRequest {
  name: string;
  description?: string | null;
  street?: string | null;
  houseNumber?: string | null;
  zipCode?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  isActive?: boolean;
}

export type UpdateWarehouseRequest = Partial<CreateWarehouseRequest>;
