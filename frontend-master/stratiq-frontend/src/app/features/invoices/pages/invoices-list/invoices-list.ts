import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { DataTable, ColumnDef, FilterDef } from '../../../../shared/components/data-table/data-table';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { InvoicesService } from '../../services/invoices.service';
import { Invoice, InvoiceStatus } from '../../models/invoice.model';
import { OrdersService } from '../../../orders/services/orders.service';
import { Order } from '../../../orders/models/order.model';
import { Paginated, TableQuery, Uuid } from '../../../../shared/models/common.model';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-invoices-list',
  imports: [TranslatePipe, DataTable, Navbar, ReactiveFormsModule],
  templateUrl: './invoices-list.html',
})
export class InvoicesList implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly invoicesService = inject(InvoicesService);
  private readonly ordersService = inject(OrdersService);
  private readonly i18n = inject(I18nService);
  private readonly session = inject(AuthSessionService);

  readonly canCreate = computed(() => this.session.hasPermission(PermissionCode.OrderWrite));

  readonly page = signal<Paginated<Invoice> | null>(null);
  readonly loading = signal(true);

  // Invoices don't carry the order number, only the order id — join client-side.
  readonly orders = signal<Order[]>([]);

  // --- Create modal ---
  readonly showCreate = signal(false);
  readonly creating = signal(false);
  readonly createError = signal<string | null>(null);

  readonly createForm = this.fb.nonNullable.group({
    orderId: [''],
    dueDate: [''],
    notes: [''],
  });

  readonly filterDefs = computed<FilterDef[]>(() => [
    {
      key: 'status',
      labelKey: 'invoices.filter.status',
      type: 'select',
      options: [
        { value: InvoiceStatus.ISSUED, labelKey: 'invoices.status.issued' },
        { value: InvoiceStatus.PAID, labelKey: 'invoices.status.paid' },
        { value: InvoiceStatus.CANCELLED, labelKey: 'invoices.status.cancelled' },
      ],
    },
  ]);

  readonly columns: ColumnDef<Invoice>[] = [
    {
      key: 'invoiceNumber',
      labelKey: 'invoices.col.invoiceNumber',
      sortField: 'invoiceNumber',
      value: row => row.invoiceNumber,
    },
    {
      key: 'order',
      labelKey: 'invoices.col.order',
      value: row => this.orderNumber(row.orderId),
    },
    {
      key: 'status',
      labelKey: 'invoices.col.status',
      sortField: 'status',
      value: row => this.i18n.t(this.statusKey(row.status)),
      badgeClass: row => this.statusBadgeClass(row.status),
    },
    {
      key: 'issueDate',
      labelKey: 'invoices.col.issueDate',
      sortField: 'issueDate',
      value: row => new Date(row.issueDate).toLocaleDateString('de-DE'),
    },
    {
      key: 'dueDate',
      labelKey: 'invoices.col.dueDate',
      value: row => row.dueDate ? new Date(row.dueDate).toLocaleDateString('de-DE') : '—',
    },
    {
      key: 'totalAmount',
      labelKey: 'invoices.col.amount',
      sortField: 'totalAmount',
      value: row => row.totalAmount.toLocaleString('de-DE', { style: 'currency', currency: 'EUR' }),
    },
  ];

  ngOnInit(): void {
    this.ordersService.getAll({ size: 500 }).subscribe({
      next: page => this.orders.set(page.content),
      error: () => this.orders.set([]),
    });
    this.load({ size: 20, filters: {} });
  }

  onQuery(query: TableQuery): void {
    this.load(query);
  }

  openInvoice(invoice: Invoice): void {
    void this.router.navigate(['/invoices', invoice.id]);
  }

  openCreate(): void {
    this.createError.set(null);
    this.createForm.reset({ orderId: '', dueDate: '', notes: '' });
    this.showCreate.set(true);
  }

  closeCreate(): void {
    this.showCreate.set(false);
  }

  submitCreate(): void {
    this.createError.set(null);
    const v = this.createForm.getRawValue();
    if (!v.orderId) {
      this.createError.set('invoices.error.actionFailed');
      return;
    }
    this.creating.set(true);
    this.invoicesService.create({
      orderId: v.orderId,
      // <input type="date"> yields YYYY-MM-DD; the backend expects a LocalDateTime.
      dueDate: v.dueDate ? `${v.dueDate}T00:00:00` : null,
      notes: v.notes || null,
    }).subscribe({
      next: () => {
        this.creating.set(false);
        this.closeCreate();
        this.load({ size: 20, filters: {} });
      },
      error: () => {
        this.creating.set(false);
        this.createError.set('invoices.error.actionFailed');
      },
    });
  }

  private load(query: TableQuery): void {
    this.loading.set(true);
    this.invoicesService.getAll(query).subscribe({
      next: page => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private orderNumber(orderId: Uuid): string {
    return this.orders().find(o => o.id === orderId)?.orderNumber ?? orderId;
  }

  private statusBadgeClass(status: InvoiceStatus): string {
    switch (status) {
      case InvoiceStatus.PAID: return 'fluent-badge-success';
      case InvoiceStatus.CANCELLED: return 'fluent-badge-danger';
      default: return 'fluent-badge-neutral';
    }
  }

  private statusKey(status: InvoiceStatus): string {
    switch (status) {
      case InvoiceStatus.ISSUED: return 'invoices.status.issued';
      case InvoiceStatus.PAID: return 'invoices.status.paid';
      case InvoiceStatus.CANCELLED: return 'invoices.status.cancelled';
    }
  }
}
