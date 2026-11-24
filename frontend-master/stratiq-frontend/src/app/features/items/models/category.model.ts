import { IsoDateTime, Uuid } from '../../../shared/models/common.model';

export interface Category {
  id: Uuid;
  name: string;
  description: string | null;
  createdAt: IsoDateTime;
}
