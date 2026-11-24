import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { DataTable, ColumnDef, FilterDef } from '../../../../shared/components/data-table/data-table';
import { OrdersService } from '../../services/orders.service';
import { CustomersService } from '../../../customers/services/customers.service';
import { Customer } from '../../../customers/models/customer.model';
import { Order, OrderStatus } from '../../models/order.model';
import { Paginated, TableQuery } from '../../../../shared/models/common.model';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-orders-list',
  imports: [TranslatePipe, DataTable, Navbar, ReactiveFormsModule],
  templateUrl: './orders-list.html',
  styleUrl: './orders-list.css',
})
export class OrdersList implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly ordersService = inject(OrdersService);
  private readonly customersService = inject(CustomersService);
  private readonly i18n = inject(I18nService);
  private readonly session = inject(AuthSessionService);

  readonly canCreate = computed(() => this.session.hasPermission(PermissionCode.OrderWrite));

  readonly page = signal<Paginated<Order> | null>(null);
  readonly loading = signal(true);

  readonly customers = signal<Customer[]>([]);

  // --- Create modal ---
  readonly showCreate = signal(false);
  readonly creating = signal(false);
  readonly createError = signal<string | null>(null);

  readonly createForm = this.fb.nonNullable.group({
    customerId:   [''],
    orderNumber:  [''],
    direction:    ['OUT', Validators.required],
    expectedDate: [''],
    notes:        [''],
  });

  readonly filterDefs = computed<FilterDef[]>(() => [
    {
      key: 'status',
      labelKey: 'orders.filter.status',
      type: 'select',
      options: [
        { value: OrderStatus.DRAFT, labelKey: 'orders.status.draft' },
        { value: OrderStatus.CONFIRMED, labelKey: 'orders.status.confirmed' },
        { value: OrderStatus.COMPLETED, labelKey: 'orders.status.completed' },
        { value: OrderStatus.CANCELLED, labelKey: 'orders.status.cancelled' },
      ],
    },
    {
      key: 'direction',
      labelKey: 'orders.filter.direction',
      type: 'select',
      options: [
        { value: 'IN', labelKey: 'orders.direction.in' },
        { value: 'OUT', labelKey: 'orders.direction.out' },
      ],
    },
    {
      key: 'customerId',
      labelKey: 'orders.filter.customer',
      type: 'select',
      // Customer names are raw data; the translate pipe falls back to the literal.
      options: this.customers()
        .filter(c => c.name)
        .map(c => ({ value: c.id, labelKey: c.name })),
    },
  ]);

  readonly columns: ColumnDef<Order>[] = [
    {
      key: 'orderNumber',
      labelKey: 'orders.col.orderNumber',
      sortField: 'orderNumber',
      value: row => row.orderNumber ?? '—',
      subValue: row => row.notes ?? null,
    },
    {
      key: 'status',
      labelKey: 'orders.col.status',
      sortField: 'status',
      value: row => this.i18n.t(this.statusKey(row.status)),
      badgeClass: row => this.statusBadgeClass(row.status),
    },
    {
      key: 'direction',
      labelKey: 'orders.col.direction',
      sortField: 'direction',
      value: row => this.i18n.t(row.direction === 'IN' ? 'orders.direction.in' : 'orders.direction.out'),
      badgeClass: row => row.direction === 'IN' ? 'fluent-badge-success' : 'fluent-badge',
    },
    {
      key: 'totalAmount',
      labelKey: 'orders.col.amount',
      sortField: 'totalAmount',
      value: row => row.totalAmount.toLocaleString('de-DE', { style: 'currency', currency: 'EUR' }),
    },
    {
      key: 'orderDate',
      labelKey: 'orders.col.orderDate',
      sortField: 'orderDate',
      value: row => new Date(row.orderDate).toLocaleDateString('de-DE'),
    },
    {
      key: 'expectedDate',
      labelKey: 'orders.col.expectedDate',
      value: row => row.expectedDate ? new Date(row.expectedDate).toLocaleDateString('de-DE') : '—',
    },
  ];

  ngOnInit(): void {
    this.customersService.getAll({ size: 500 }).subscribe({
      next: page => this.customers.set(page.content),
      error: () => this.customers.set([]),
    });
    this.load({ size: 20, filters: {} });
  }

  onQuery(query: TableQuery): void {
    this.load(query);
  }

  openOrder(order: Order): void {
    void this.router.navigate(['/orders', order.id]);
  }

  openCreate(): void {
    this.createError.set(null);
    this.createForm.reset({ customerId: '', orderNumber: '', expectedDate: '', notes: '' });
    this.showCreate.set(true);
  }

  closeCreate(): void {
    this.showCreate.set(false);
  }

  submitCreate(): void {
    this.createError.set(null);
    const v = this.createForm.getRawValue();
    this.creating.set(true);
    this.ordersService.createHeader({
      customerId: v.customerId || null,
      orderNumber: v.orderNumber || null,
      direction: v.direction as 'IN' | 'OUT',
      // <input type="date"> yields YYYY-MM-DD; the backend expects a LocalDateTime.
      expectedDate: v.expectedDate ? `${v.expectedDate}T00:00:00` : null,
      notes: v.notes || null,
    }).subscribe({
      next: () => {
        this.creating.set(false);
        this.closeCreate();
        this.load({ size: 20, filters: {} });
      },
      error: () => {
        this.creating.set(false);
        this.createError.set('orders.new.error');
      },
    });
  }

  private load(query: TableQuery): void {
    this.loading.set(true);
    this.ordersService.getAll(query).subscribe({
      next: page => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private statusBadgeClass(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.CONFIRMED: return 'fluent-badge';
      case OrderStatus.COMPLETED: return 'fluent-badge-success';
      case OrderStatus.CANCELLED: return 'fluent-badge-danger';
      default:                    return 'fluent-badge-neutral';
    }
  }

  private statusKey(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.DRAFT:     return 'orders.status.draft';
      case OrderStatus.CONFIRMED: return 'orders.status.confirmed';
      case OrderStatus.COMPLETED: return 'orders.status.completed';
      case OrderStatus.CANCELLED: return 'orders.status.cancelled';
    }
  }
}
