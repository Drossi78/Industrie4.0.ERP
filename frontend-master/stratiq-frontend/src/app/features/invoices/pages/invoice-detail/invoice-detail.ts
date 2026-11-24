import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { InvoicesService } from '../../services/invoices.service';
import { Invoice, InvoiceStatus } from '../../models/invoice.model';
import { OrdersService } from '../../../orders/services/orders.service';
import { Order } from '../../../orders/models/order.model';
import { OrderItemView } from '../../../orders/models/order-detail.model';
import { CustomersService } from '../../../customers/services/customers.service';
import { Customer } from '../../../customers/models/customer.model';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-invoice-detail',
  imports: [TranslatePipe, Navbar, RouterLink],
  templateUrl: './invoice-detail.html',
})
export class InvoiceDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly invoicesService = inject(InvoicesService);
  private readonly ordersService = inject(OrdersService);
  private readonly customersService = inject(CustomersService);
  private readonly session = inject(AuthSessionService);
  readonly i18n = inject(I18nService);

  readonly canUpdate = computed(() => this.session.hasPermission(PermissionCode.OrderWrite));

  private invoiceId = '';

  readonly invoice = signal<Invoice | null>(null);
  readonly order = signal<Order | null>(null);
  readonly customer = signal<Customer | null>(null);
  readonly orderItems = signal<OrderItemView[]>([]);
  readonly loading = signal(true);
  readonly updating = signal(false);
  readonly actionError = signal<string | null>(null);

  ngOnInit(): void {
    this.invoiceId = this.route.snapshot.paramMap.get('id') ?? '';
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.invoicesService.getById(this.invoiceId).subscribe({
      next: invoice => {
        this.invoice.set(invoice);
        this.loading.set(false);
        this.loadOrderContext(invoice.orderId);
      },
      error: () => this.loading.set(false),
    });
  }

  private loadOrderContext(orderId: string): void {
    this.ordersService.getById(orderId).subscribe({
      next: order => {
        this.order.set(order);
        if (order.customerId) {
          this.customersService.getById(order.customerId).subscribe({
            next: customer => this.customer.set(customer),
            error: () => this.customer.set(null),
          });
        }
      },
      error: () => this.order.set(null),
    });
    this.ordersService.getOrderItems(orderId).subscribe({
      next: items => this.orderItems.set(items),
      error: () => this.orderItems.set([]),
    });
  }

  markPaid(): void {
    this.actionError.set(null);
    this.updating.set(true);
    this.invoicesService.update(this.invoiceId, {
      status: InvoiceStatus.PAID,
      paidAt: new Date().toISOString(),
    }).subscribe({
      next: updated => {
        this.invoice.set(updated);
        this.updating.set(false);
      },
      error: () => {
        this.updating.set(false);
        this.actionError.set('invoices.error.actionFailed');
      },
    });
  }

  cancelInvoice(): void {
    if (!window.confirm(this.i18n.t('invoices.detail.confirmCancel'))) return;
    this.actionError.set(null);
    this.updating.set(true);
    this.invoicesService.update(this.invoiceId, {
      status: InvoiceStatus.CANCELLED,
      cancelledAt: new Date().toISOString(),
    }).subscribe({
      next: updated => {
        this.invoice.set(updated);
        this.updating.set(false);
      },
      error: () => {
        this.updating.set(false);
        this.actionError.set('invoices.error.actionFailed');
      },
    });
  }

  print(): void {
    window.print();
  }

  statusKey(status: InvoiceStatus): string {
    switch (status) {
      case InvoiceStatus.ISSUED: return 'invoices.status.issued';
      case InvoiceStatus.PAID: return 'invoices.status.paid';
      case InvoiceStatus.CANCELLED: return 'invoices.status.cancelled';
    }
  }

  statusBadgeClass(status: InvoiceStatus): string {
    switch (status) {
      case InvoiceStatus.PAID: return 'fluent-badge-success';
      case InvoiceStatus.CANCELLED: return 'fluent-badge-danger';
      default: return 'fluent-badge-neutral';
    }
  }

  formatDate(iso: string | null): string {
    return iso ? new Date(iso).toLocaleDateString('de-DE') : '—';
  }

  formatCurrency(value: number): string {
    return value.toLocaleString('de-DE', { style: 'currency', currency: 'EUR' });
  }
}
