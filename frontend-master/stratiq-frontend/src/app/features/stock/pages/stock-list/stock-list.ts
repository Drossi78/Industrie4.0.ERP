import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { DataTable, ColumnDef } from '../../../../shared/components/data-table/data-table';
import { StockService } from '../../services/stock.service';
import { StockLevel } from '../../models/stock-level.model';
import { Reservation } from '../../models/reservation.model';
import { WarehousesService } from '../../../warehouses/services/warehouses.service';
import { Warehouse } from '../../../warehouses/models/warehouse.model';
import { Paginated, Uuid } from '../../../../shared/models/common.model';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';
import { OrdersService } from '../../../orders/services/orders.service';

type StockView = 'asset' | 'warehouse' | 'reservation';

@Component({
  selector: 'app-stock-list',
  imports: [TranslatePipe, DataTable, Navbar, ReactiveFormsModule],
  templateUrl: './stock-list.html',
  styleUrl: './stock-list.css',
})
export class StockList implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly stockService = inject(StockService);
  private readonly warehousesService = inject(WarehousesService);
  private readonly ordersService = inject(OrdersService);
  private readonly session = inject(AuthSessionService);

  readonly canEdit = computed(() => this.session.hasPermission(PermissionCode.WarehouseWrite));

  readonly view = signal<StockView>('asset');

  // --- Per-asset view ---
  readonly page = signal<Paginated<StockLevel> | null>(null);
  readonly loading = signal(true);
  // Item options for the transfer/reservation dropdowns, derived from the overview.
  readonly stockItems = signal<StockLevel[]>([]);

  // --- Per-warehouse view ---
  readonly warehouses = signal<Warehouse[]>([]);
  readonly warehousesLoading = signal(false);
  readonly expandedId = signal<Uuid | null>(null);
  readonly levelsLoadingId = signal<Uuid | null>(null);
  // Cache of already-loaded per-warehouse stock levels, keyed by warehouse id.
  readonly levelsCache = signal<Record<string, StockLevel[]>>({});

  // --- Reservation view ---
  readonly reservations = signal<Reservation[]>([]);
  readonly reservationsLoading = signal(false);
  readonly releasingId = signal<Uuid | null>(null);

  // --- Transfer modal ---
  readonly showBooking = signal(false);
  readonly booking = signal(false);
  readonly bookingError = signal<string | null>(null);
  // Current stock of the selected asset per warehouse (warehouseId -> balance),
  // shown in the warehouse dropdowns so allowed movements are obvious.
  readonly transferStock = signal<Record<string, number>>({});

  // --- Reservation modal ---
  readonly showReservation = signal(false);
  readonly reserving = signal(false);
  readonly reservationError = signal<string | null>(null);

  readonly transferForm = this.fb.nonNullable.group({
    itemId:          ['', Validators.required],
    fromWarehouseId: ['', Validators.required],
    toWarehouseId:   ['', Validators.required],
    quantity:        [null as number | null, [Validators.required, Validators.min(1)]],
    note:            [''],
  });

  readonly reservationForm = this.fb.nonNullable.group({
    itemId:      ['', Validators.required],
    warehouseId: ['', Validators.required],
    quantity:    [null as number | null, [Validators.required, Validators.min(1)]],
    note:        [''],
  });

  readonly columns: ColumnDef<StockLevel>[] = [
    {
      key: 'itemName',
      labelKey: 'stock.col.asset',
      value: row => row.itemName,
    },
    {
      key: 'totalIn',
      labelKey: 'stock.col.totalIn',
      value: row => row.totalIn.toLocaleString('de-DE'),
    },
    {
      key: 'totalOut',
      labelKey: 'stock.col.totalOut',
      value: row => row.totalOut.toLocaleString('de-DE'),
    },
    {
      key: 'balance',
      labelKey: 'stock.col.balance',
      value: row => row.balance.toLocaleString('de-DE'),
      badgeClass: row => this.balanceBadgeClass(row.balance),
    },
  ];

  ngOnInit(): void {
    this.loadAssetLevels();
  }

  switchView(view: StockView): void {
    this.view.set(view);
    if (view === 'warehouse' && this.warehouses().length === 0) {
      this.loadWarehouses();
    }
    if (view === 'reservation') {
      this.loadReservations();
    }
  }

  // --- Per-asset view ---

  loadAssetLevels(): void {
    this.loading.set(true);
    this.stockService.getLevels().subscribe({
      next: levels => {
        this.stockItems.set(levels);
        this.page.set(this.wrap(levels));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  // --- Per-warehouse view ---

  loadWarehouses(): void {
    this.warehousesLoading.set(true);
    this.warehousesService.getAll({ size: 100 }).subscribe({
      next: page => {
        this.warehouses.set(page.content);
        this.warehousesLoading.set(false);
      },
      error: () => this.warehousesLoading.set(false),
    });
  }

  toggleWarehouse(warehouseId: Uuid): void {
    if (this.expandedId() === warehouseId) {
      this.expandedId.set(null);
      return;
    }
    this.expandedId.set(warehouseId);
    // Load levels only if not already cached.
    if (!this.levelsCache()[warehouseId]) {
      this.loadWarehouseLevels(warehouseId);
    }
  }

  levelsFor(warehouseId: Uuid): StockLevel[] {
    return this.levelsCache()[warehouseId] ?? [];
  }

  balanceBadgeClass(balance: number): string {
    if (balance > 0) return 'fluent-badge-success';
    if (balance < 0) return 'fluent-badge-danger';
    return 'fluent-badge-neutral';
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('de-DE');
  }

  // --- Reservation view ---

  loadReservations(): void {
    this.reservationsLoading.set(true);
    this.stockService.getReservations().subscribe({
      next: list => {
        this.reservations.set(list);
        this.reservationsLoading.set(false);
      },
      error: () => this.reservationsLoading.set(false),
    });
  }

  releaseReservation(id: Uuid): void {
    this.releasingId.set(id);
    this.stockService.releaseReservation(id).subscribe({
      next: () => {
        this.releasingId.set(null);
        this.loadReservations();
        this.loadAssetLevels();
      },
      error: () => this.releasingId.set(null),
    });
  }

  // --- Transfer modal ---

  openBooking(): void {
    this.bookingError.set(null);
    this.transferStock.set({});
    this.transferForm.reset({ itemId: '', fromWarehouseId: '', toWarehouseId: '', quantity: null, note: '' });
    this.ensureDropdownData();
    this.showBooking.set(true);
  }

  closeBooking(): void {
    this.showBooking.set(false);
  }

  // Load the selected item's stock per warehouse for the transfer dropdowns.
  loadTransferStock(itemId: string): void {
    if (!itemId) {
      this.transferStock.set({});
      return;
    }
    this.stockService.getByWarehouse(itemId).subscribe({
      next: list => {
        const byWarehouse: Record<string, number> = {};
        for (const w of list) {
          byWarehouse[w.warehouseId] = w.balance;
        }
        this.transferStock.set(byWarehouse);
      },
      error: () => this.transferStock.set({}),
    });
  }

  warehouseStock(warehouseId: string): number {
    return this.transferStock()[warehouseId] ?? 0;
  }

  submitBooking(): void {
    this.bookingError.set(null);
    if (this.transferForm.invalid) { this.transferForm.markAllAsTouched(); return; }
    const v = this.transferForm.getRawValue();
    if (v.fromWarehouseId === v.toWarehouseId) {
      this.bookingError.set('stock.book.error.sameWarehouse');
      return;
    }
    this.booking.set(true);
    this.stockService.transfer({
      itemId: v.itemId,
      fromWarehouseId: v.fromWarehouseId,
      toWarehouseId: v.toWarehouseId,
      quantity: v.quantity!,
      note: v.note || null,
    }).subscribe({
      next: () => this.onBookingSuccess(),
      error: err => this.onBookingError(err),
    });
  }

  private onBookingSuccess(): void {
    this.booking.set(false);
    this.closeBooking();
    this.refreshLevels();
  }

  private onBookingError(err: unknown): void {
    this.booking.set(false);
    this.bookingError.set(this.extractError(err, 'stock.book.error.generic'));
  }

  // --- Reservation modal ---

  openReservation(): void {
    this.reservationError.set(null);
    this.reservationForm.reset({ itemId: '', warehouseId: '', quantity: null, note: '' });
    this.ensureDropdownData();
    this.showReservation.set(true);
  }

  closeReservation(): void {
    this.showReservation.set(false);
  }

  submitReservation(): void {
    this.reservationError.set(null);
    if (this.reservationForm.invalid) { this.reservationForm.markAllAsTouched(); return; }
    const v = this.reservationForm.getRawValue();
    this.reserving.set(true);
    this.stockService.createReservation({
      itemId: v.itemId,
      warehouseId: v.warehouseId,
      quantity: v.quantity!,
      note: v.note || null,
    }).subscribe({
      next: () => {
        this.reserving.set(false);
        this.closeReservation();
        this.loadReservations();
      },
      error: err => {
        this.reserving.set(false);
        this.reservationError.set(this.extractError(err, 'stock.reservation.error.generic'));
      },
    });
  }

  // --- Helpers ---

  private ensureDropdownData(): void {
    if (this.stockItems().length === 0) this.loadAssetLevels();
    if (this.warehouses().length === 0) this.loadWarehouses();
  }

  // Prefer the backend's message (e.g. insufficient stock), else a generic i18n key.
  private extractError(err: unknown, fallbackKey: string): string {
    if (err instanceof HttpErrorResponse && err.error?.message) {
      return err.error.message;
    }
    return fallbackKey;
  }

  private refreshLevels(): void {
    this.loadAssetLevels();
    this.levelsCache.set({});
    const expanded = this.expandedId();
    if (expanded) this.loadWarehouseLevels(expanded);
  }

  private loadWarehouseLevels(warehouseId: Uuid): void {
    this.levelsLoadingId.set(warehouseId);
    this.stockService.getLevelsForWarehouse(warehouseId).subscribe({
      next: levels => {
        this.levelsCache.update(cache => ({ ...cache, [warehouseId]: levels }));
        this.levelsLoadingId.set(null);
      },
      error: () => this.levelsLoadingId.set(null),
    });
  }

  // The backend returns a plain array; wrap it in a single-page Paginated
  // so the shared DataTable component can render it.
  private wrap(levels: StockLevel[]): Paginated<StockLevel> {
    return {
      content: levels,
      totalElements: levels.length,
      totalPages: 1,
      number: 0,
      size: levels.length,
      first: true,
      last: true,
      numberOfElements: levels.length,
      empty: levels.length === 0,
    };
  }
}
