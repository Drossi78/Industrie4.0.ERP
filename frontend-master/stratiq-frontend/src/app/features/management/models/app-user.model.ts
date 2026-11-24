import { IsoDateTime, Uuid } from '../../../shared/models/common.model';

export interface AppUser {
  id: Uuid;
  email: string;
  roleId: Uuid;
  roleName: string;
  preferredLang: string;
  isActive: boolean;
  createdAt: IsoDateTime;
}

export interface CreateUserRequest {
  email: string;
  password: string;
  roleId: Uuid;
  preferredLang: string;
}

export interface UpdateUserRequest {
  email?: string;
  roleId?: Uuid;
  preferredLang?: string;
  isActive?: boolean;
}

export interface AppRole {
  id: Uuid;
  name: string;
  description: string;
}
