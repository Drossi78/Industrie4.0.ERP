import { IsoDateTime, Uuid } from '../../../shared/models/common.model';
import { ProductionStepStatus } from '../../../shared/enums/production-step-status.enum';

export { ProductionStepStatus };

export interface OrderProductionStep {
  id: Uuid;
  orderId: Uuid;
  orderItemId: Uuid;
  templateStepId: Uuid;
  name: string;
  description?: string | null;
  stepOrder: number;
  status: ProductionStepStatus;
  startedAt?: IsoDateTime | null;
  completedAt?: IsoDateTime | null;
  assignedTo?: Uuid | null;
  createdAt: IsoDateTime;
}

export interface OrderProduction {
  orderId: Uuid;
  orderItemId: Uuid;
  itemId: Uuid;
  initialized: boolean;
  initializedAt?: IsoDateTime | null;
  steps: OrderProductionStep[] | null;
  overallStatus: ProductionStepStatus;
}

export interface UpdateOrderProductionStepRequest {
  status: ProductionStepStatus;
  assignedTo?: Uuid | null;
}
