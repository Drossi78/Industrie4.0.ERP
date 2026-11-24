import { IsoDateTime, Uuid } from '../../../shared/models/common.model';
import { InvoiceStatus } from '../../../shared/enums/invoice-status.enum';

export { InvoiceStatus };

export interface Invoice {
  id: Uuid;
  invoiceNumber: string;
  orderId: Uuid;
  status: InvoiceStatus;
  issueDate: IsoDateTime;
  dueDate: IsoDateTime | null;
  paidAt: IsoDateTime | null;
  cancelledAt: IsoDateTime | null;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  notes: string | null;
  createdAt: IsoDateTime;
}

export interface CreateInvoiceRequest {
  orderId: Uuid;
  dueDate?: IsoDateTime | null;
  notes?: string | null;
}

export interface UpdateInvoiceRequest {
  status?: InvoiceStatus;
  dueDate?: IsoDateTime | null;
  paidAt?: IsoDateTime | null;
  cancelledAt?: IsoDateTime | null;
  subtotal?: number;
  taxAmount?: number;
  totalAmount?: number;
  notes?: string | null;
}
