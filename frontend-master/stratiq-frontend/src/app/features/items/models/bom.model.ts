import { IsoDateTime, Uuid } from '../../../shared/models/common.model';

// A single BOM line: how much of `componentId` (a PURCHASED item) is needed
// to produce one unit of `itemId` (an OWN_PRODUCT item).
export interface BomLine {
  id: Uuid;
  createdAt: IsoDateTime;
  itemId: Uuid;
  itemName: string;
  componentId: Uuid;
  componentName: string;
  quantity: number;
  unit: string;
  notes: string | null;
}

export interface CreateBomLineRequest {
  itemId: Uuid;
  componentId: Uuid;
  quantity: number;
  unit: string;
  notes?: string | null;
}
