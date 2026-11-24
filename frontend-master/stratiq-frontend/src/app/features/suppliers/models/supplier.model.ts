import { IsoDateTime, Uuid } from '../../../shared/models/common.model';

export interface Supplier {
  id: Uuid;
  name: string;
  email: string | null;
  phone: string | null;
  taxId: string | null;
  street: string | null;
  houseNumber: string | null;
  zipCode: string | null;
  city: string | null;
  state: string | null;
  country: string | null;
  notes: string | null;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
  deletedAt: IsoDateTime | null;
}

export interface CreateSupplierRequest {
  name: string;
  email?: string | null;
  phone?: string | null;
  taxId?: string | null;
  street?: string | null;
  houseNumber?: string | null;
  zipCode?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  notes?: string | null;
}

export interface UpdateSupplierRequest {
  name?: string | null;
  email?: string | null;
  phone?: string | null;
  taxId?: string | null;
  street?: string | null;
  houseNumber?: string | null;
  zipCode?: string | null;
  city?: string | null;
  state?: string | null;
  country?: string | null;
  notes?: string | null;
}
