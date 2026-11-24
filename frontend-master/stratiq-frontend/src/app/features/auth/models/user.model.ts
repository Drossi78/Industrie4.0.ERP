import { IsoDateTime, Uuid } from '../../../shared/models/common.model';

export type RoleName = 'admin' | 'edit' | 'read_only';

export interface User {
  id: Uuid;
  email: string;
  firstName: string;
  lastName: string;
  role: RoleName;
  isActive: boolean;
  createdAt: IsoDateTime;
  updatedAt: IsoDateTime;
  deletedAt?: IsoDateTime | null;
}
