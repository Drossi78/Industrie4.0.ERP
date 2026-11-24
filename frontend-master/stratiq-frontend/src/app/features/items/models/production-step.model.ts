import { IsoDateTime, Uuid } from '../../../shared/models/common.model';

/**
 * Produktionsschritt-Vorlage je Artikel (item-scoped),
 * analog zu Reihenfolge/Metadaten in der Datenbank.
 */
export interface ProductionStep {
  id: Uuid;
  itemId: Uuid;
  name: string;
  description?: string | null;
  stepOrder: number;
  isDeleted: boolean;
  notes?: string | null;
  createdAt: IsoDateTime;
}

export interface CreateProductionStepRequest {
  itemId: Uuid;
  name: string;
  description?: string | null;
  stepOrder: number;
}

export interface UpdateProductionStepRequest {
  name?: string;
  description?: string | null;
  stepOrder?: number;
}
