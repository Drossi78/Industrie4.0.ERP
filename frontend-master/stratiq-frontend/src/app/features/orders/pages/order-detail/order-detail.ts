import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { OrdersService } from '../../services/orders.service';
import { Order, OrderStatus } from '../../models/order.model';
import { OrderItemView } from '../../models/order-detail.model';
import { UnitOfMeasure } from '../../../../shared/enums/unit-of-measure.enum';
import { StockService } from '../../../stock/services/stock.service';
import { StockLevel, WarehouseStock } from '../../../stock/models/stock-level.model';
import { StockMovementResponse } from '../../../stock/models/stock-operation.model';
import { WarehousesService } from '../../../warehouses/services/warehouses.service';
import { Warehouse } from '../../../warehouses/models/warehouse.model';
import { Uuid } from '../../../../shared/models/common.model';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-order-detail',
  imports: [TranslatePipe, Navbar, ReactiveFormsModule],
  templateUrl: './order-detail.html',
  styleUrl: './order-detail.css',
})
export class OrderDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly ordersService = inject(OrdersService);
  private readonly stockService = inject(StockService);
  private readonly warehousesService = inject(WarehousesService);
  private readonly session = inject(AuthSessionService);
  private readonly i18n = inject(I18nService);

  readonly canEdit = computed(() => this.session.hasPermission(PermissionCode.OrderWrite));

  private orderId = '';

  readonly order = signal<Order | null>(null);
  readonly items = signal<OrderItemView[]>([]);
  readonly movements = signal<StockMovementResponse[]>([]);
  readonly loading = signal(true);

  readonly stockItems = signal<StockLevel[]>([]);
  readonly warehouses = signal<Warehouse[]>([]);
  readonly units = Object.values(UnitOfMeasure);
  readonly statuses = [OrderStatus.DRAFT, OrderStatus.CONFIRMED, OrderStatus.COMPLETED, OrderStatus.CANCELLED];

  // Per-asset stock distribution across warehouses, shown in the fulfill modal.
  readonly distribution = signal<Record<string, WarehouseStock[]>>({});
  readonly updatingStatus = signal(false);
  readonly statusError = signal<string | null>(null);

  // Line-item removal
  readonly removingId = signal<Uuid | null>(null);
  readonly deleteError = signal<string | null>(null);

  // Add-item form
  readonly adding = signal(false);
  readonly addError = signal<string | null>(null);
  readonly addForm = this.fb.nonNullable.group({
    itemId:    ['', Validators.required],
    unit:      ['piece', Validators.required],
    quantity:  [null as number | null, [Validators.required, Validators.min(0.001)]],
    unitPrice: [null as number | null, [Validators.required, Validators.min(0)]],
  });

  // Fulfill modal
  readonly showFulfill = signal(false);
  readonly fulfilling = signal(false);
  readonly fulfillError = signal<string | null>(null);
  readonly fulfillDone = signal<number | null>(null);
  readonly fulfillForm = this.fb.nonNullable.group({
    warehouseId: [''],
    direction:   ['OUT', Validators.required],
  });

  // Per-item batch metadata entered for a goods receipt (direction IN), keyed by itemId.
  readonly batchLines = signal<Record<string, { batchNumber: string; expiryDate: string }>>({});

  readonly itemsTotal = computed(() =>
    this.items().reduce((sum, i) => sum + (i.lineTotal ?? 0), 0),
  );

  // A completed order is read-only: only viewing, no changes.
  readonly locked = computed(() => this.order()?.status === OrderStatus.COMPLETED);

  ngOnInit(): void {
    this.orderId = this.route.snapshot.paramMap.get('id') ?? '';
    this.load();
    this.stockService.getLevels().subscribe({ next: levels => this.stockItems.set(levels) });
    this.warehousesService.getAll({ size: 100 }).subscribe({ next: p => this.warehouses.set(p.content) });
  }

  load(): void {
    this.loading.set(true);
    this.ordersService.getById(this.orderId).subscribe({
      next: o => { this.order.set(o); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
    this.loadItems();
    this.loadMovements();
  }

  loadItems(): void {
    this.ordersService.getOrderItems(this.orderId).subscribe({
      next: list => this.items.set(list),
    });
  }

  // Stock movements booked for this order (goods receipt/issue), with batch info.
  loadMovements(): void {
    this.ordersService.getMovements(this.orderId).subscribe({
      next: list => this.movements.set(list),
      error: () => this.movements.set([]),
    });
  }

  submitAdd(): void {
    this.addError.set(null);
    if (this.addForm.invalid) { this.addForm.markAllAsTouched(); return; }
    const v = this.addForm.getRawValue();
    const item = this.stockItems().find(i => i.itemId === v.itemId);
    this.adding.set(true);
    this.ordersService.addOrderItem({
      orderId: this.orderId,
      itemId: v.itemId,
      itemName: item?.itemName ?? 'Item',
      unit: v.unit,
      quantity: v.quantity!,
      unitPrice: v.unitPrice!,
    }).subscribe({
      next: () => {
        this.adding.set(false);
        this.addForm.reset({ itemId: '', unit: 'piece', quantity: null, unitPrice: null });
        this.loadItems();
      },
      error: err => {
        this.adding.set(false);
        this.addError.set(this.extractError(err, 'orders.detail.add.error'));
      },
    });
  }

  removeItem(itemId: Uuid): void {
    if (!confirm(this.i18n.t('orders.detail.remove.confirm'))) return;
    this.deleteError.set(null);
    this.removingId.set(itemId);
    this.ordersService.removeOrderItem(itemId).subscribe({
      next: () => {
        this.removingId.set(null);
        this.loadItems();
      },
      error: err => {
        this.removingId.set(null);
        this.deleteError.set(this.extractError(err, 'orders.detail.remove.error'));
      },
    });
  }

  openFulfill(): void {
    this.fulfillError.set(null);
    this.fulfillDone.set(null);
    // Direction is fixed by the order (chosen at creation), not chosen here.
    const direction = this.order()?.direction ?? 'OUT';
    this.fulfillForm.reset({ warehouseId: '', direction });
    this.batchLines.set({});
    this.loadDistribution();
    this.showFulfill.set(true);
  }

  // Batch-input helpers for the goods-receipt (IN) rows.
  batchNumberOf(itemId: Uuid): string {
    return this.batchLines()[itemId]?.batchNumber ?? '';
  }

  expiryOf(itemId: Uuid): string {
    return this.batchLines()[itemId]?.expiryDate ?? '';
  }

  setBatchNumber(itemId: Uuid, value: string): void {
    this.batchLines.update(m => ({ ...m, [itemId]: { batchNumber: value, expiryDate: m[itemId]?.expiryDate ?? '' } }));
  }

  setExpiry(itemId: Uuid, value: string): void {
    this.batchLines.update(m => ({ ...m, [itemId]: { batchNumber: m[itemId]?.batchNumber ?? '', expiryDate: value } }));
  }

  closeFulfill(): void {
    this.showFulfill.set(false);
    // Stock changed after a successful fulfillment; refresh the items' line data is unaffected,
    // but reload the order so an updated status (if changed elsewhere) shows.
    if (this.fulfillDone() !== null) this.load();
  }

  submitFulfill(): void {
    this.fulfillError.set(null);
    if (this.fulfillForm.invalid) { this.fulfillForm.markAllAsTouched(); return; }
    const v = this.fulfillForm.getRawValue();
    // Goods receipt (IN) needs a target warehouse; goods issue (OUT) auto-allocates.
    if (v.direction === 'IN' && !v.warehouseId) {
      this.fulfillError.set('orders.detail.fulfill.warehouseRequired');
      return;
    }
    // For a goods receipt, attach only the items where a batch number was entered.
    const lines = this.batchLines();
    const batchLines = v.direction === 'IN'
      ? this.items()
          .filter(it => (lines[it.itemId]?.batchNumber ?? '').trim() !== '')
          .map(it => ({
            itemId: it.itemId,
            batchNumber: lines[it.itemId].batchNumber.trim(),
            expiryDate: lines[it.itemId].expiryDate ? `${lines[it.itemId].expiryDate}T00:00:00` : null,
          }))
      : null;

    this.fulfilling.set(true);
    this.ordersService.fulfill(this.orderId, {
      warehouseId: v.direction === 'IN' ? v.warehouseId : null,
      direction: v.direction as 'IN' | 'OUT',
      batchLines,
    }).subscribe({
      next: booked => {
        // Fulfillment succeeded -> mark the order completed and stay on the detail page so the
        // booked movements (with their batches) are visible. Show a done state with the count.
        this.fulfilling.set(false);
        this.fulfillDone.set(booked.length);
        this.ordersService.updateStatus(this.orderId, OrderStatus.COMPLETED).subscribe({
          next: o => this.order.set(o),
          // Stock was already booked; keep going even if the status update fails.
          error: () => {},
        });
        this.loadMovements();
      },
      error: err => {
        this.fulfilling.set(false);
        this.fulfillError.set(this.extractError(err, 'orders.detail.fulfill.error'));
      },
    });
  }

  private loadDistribution(): void {
    this.distribution.set({});
    for (const it of this.items()) {
      this.stockService.getByWarehouse(it.itemId).subscribe({
        next: list => this.distribution.update(d => ({ ...d, [it.itemId]: list })),
      });
    }
  }

  distFor(itemId: Uuid): WarehouseStock[] {
    return this.distribution()[itemId] ?? [];
  }

  distTotal(itemId: Uuid): number {
    return this.distFor(itemId).reduce((sum, w) => sum + (w.balance ?? 0), 0);
  }

  changeStatus(status: string): void {
    const o = this.order();
    if (!o || !status || status === o.status) return;
    this.statusError.set(null);
    this.updatingStatus.set(true);

    const request$ = status === OrderStatus.CONFIRMED
      ? this.ordersService.confirm(this.orderId)
      : status === OrderStatus.CANCELLED
      ? this.ordersService.cancel(this.orderId)
      : this.ordersService.updateStatus(this.orderId, status as OrderStatus);

    request$.subscribe({
      next: updated => { this.order.set(updated); this.updatingStatus.set(false); },
      error: err => {
        this.updatingStatus.set(false);
        this.statusError.set(this.extractError(err, 'orders.detail.status.error'));
      },
    });
  }

  statusKey(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.DRAFT:     return 'orders.status.draft';
      case OrderStatus.CONFIRMED: return 'orders.status.confirmed';
      case OrderStatus.COMPLETED: return 'orders.status.completed';
      case OrderStatus.CANCELLED: return 'orders.status.cancelled';
    }
  }

  statusBadgeClass(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.CONFIRMED: return 'fluent-badge';
      case OrderStatus.COMPLETED: return 'fluent-badge-success';
      case OrderStatus.CANCELLED: return 'fluent-badge-danger';
      default:                    return 'fluent-badge-neutral';
    }
  }

  formatDate(iso: string | null): string {
    return iso ? new Date(iso).toLocaleDateString('de-DE') : '—';
  }

  formatCurrency(value: number): string {
    return value.toLocaleString('de-DE', { style: 'currency', currency: 'EUR' });
  }

  private extractError(err: unknown, fallbackKey: string): string {
    if (err instanceof HttpErrorResponse && err.error?.message) {
      return err.error.message;
    }
    return fallbackKey;
  }
}
