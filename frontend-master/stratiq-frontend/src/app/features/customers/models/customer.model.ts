import { IsoDateTime, Uuid } from '../../../shared/models/common.model';

export interface Customer {
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
}

export interface CreateCustomerRequest {
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

export interface UpdateCustomerRequest {
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
