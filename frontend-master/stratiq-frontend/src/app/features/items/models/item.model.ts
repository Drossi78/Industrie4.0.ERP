import { IsoDateTime, Uuid } from '../../../shared/models/common.model';

export type ItemKind = 'OWN_PRODUCT' | 'PURCHASED';

export interface Item {
  id: Uuid;
  name: string;
  kind: ItemKind;
  categoryId: Uuid | null;
  categoryName: string | null;
  unit: string;
  price: number | null;
  description: string | null;
  notes: string | null;
  isDeleted: boolean;
  createdAt: IsoDateTime;
}

export interface CreateItemRequest {
  name: string;
  kind: ItemKind;
  categoryId?: Uuid | null;
  unit: string;
  price?: number | null;
  description?: string | null;
  notes?: string | null;
}

export interface UpdateItemRequest {
  name?: string;
  kind?: ItemKind;
  categoryId?: Uuid | null;
  unit?: string;
  price?: number | null;
  description?: string | null;
  notes?: string | null;
}
