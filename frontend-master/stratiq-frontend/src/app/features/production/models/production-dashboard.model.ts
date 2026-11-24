import { IsoDateTime, Uuid } from '../../../shared/models/common.model';
import { ProductionStepStatus } from '../../../shared/enums/production-step-status.enum';

export interface ProductionOrderSummary {
  id: Uuid;
  orderNumber: string | null;
  customerName: string;
  orderDate: IsoDateTime;
  totalSteps: number;
  completedSteps: number;
  progressPercent: number;
}

export interface ProductionStepAssignee {
  id: Uuid;
  email: string;
}

export interface ProductionOrderLineStep {
  id: Uuid;
  name: string;
  stepOrder: number;
  status: ProductionStepStatus;
  // Nested object here (unlike the plain-id PATCH /production/steps/{id} response).
  assignedTo: ProductionStepAssignee | null;
  startedAt: IsoDateTime | null;
  completedAt: IsoDateTime | null;
}

export interface ProductionOrderLine {
  id: Uuid;
  itemName: string;
  quantity: number;
  totalSteps: number;
  completedSteps: number;
  steps: ProductionOrderLineStep[];
}

export interface ProductionOrderDetail {
  id: Uuid;
  orderNumber: string | null;
  customerName: string;
  // Backend still names the order-line array "assets" (legacy naming).
  assets: ProductionOrderLine[];
}
