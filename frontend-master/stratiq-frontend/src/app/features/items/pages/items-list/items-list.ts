import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { DataTable, ColumnDef, FilterDef } from '../../../../shared/components/data-table/data-table';
import { ItemsService } from '../../services/items.service';
import { CategoriesService } from '../../services/categories.service';
import { Category } from '../../models/category.model';
import { Router } from '@angular/router';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { ItemCreateDialog } from '../../components/item-create-dialog/item-create-dialog';
import { Item } from '../../models/items.model';
import { Paginated, TableQuery } from '../../../../shared/models/common.model';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-items-list',
  imports: [TranslatePipe, DataTable, Navbar, ItemCreateDialog],
  templateUrl: './items-list.html',
  styleUrl: './items-list.css',
})
export class ItemsList implements OnInit {
  private readonly itemsService = inject(ItemsService);
  private readonly categoriesService = inject(CategoriesService);
  private readonly i18n = inject(I18nService);
  private readonly router = inject(Router);
  private readonly session = inject(AuthSessionService);

  readonly canCreate = computed(() => this.session.hasPermission(PermissionCode.InventoryWrite));
  readonly canDelete = computed(() => this.session.hasPermission(PermissionCode.InventoryDelete));

  readonly page = signal<Paginated<Item> | null>(null);
  readonly loading = signal(true);
  readonly showCreateDialog = signal(false);
  readonly selection = signal<string[]>([]);
  readonly deleting = signal(false);
  readonly deleteFeedback = signal<{ type: 'success' | 'error'; count: number } | null>(null);

  private lastQuery: TableQuery = { size: 20 };
  private feedbackTimer: ReturnType<typeof setTimeout> | null = null;

  readonly categories = signal<Category[]>([]);

  readonly filterDefs = computed<FilterDef[]>(() => [
    {
      key: 'kind',
      labelKey: 'items.filter.type',
      type: 'select',
      options: [
        { value: 'OWN_PRODUCT', labelKey: 'items.kind.ownProduct' },
        { value: 'PURCHASED', labelKey: 'items.kind.purchased' },
      ],
    },
    {
      key: 'categoryId',
      labelKey: 'items.filter.category',
      type: 'select',
      // Category names are raw data; the translate pipe falls back to the literal.
      options: this.categories()
        .filter(c => c.name)
        .map(c => ({ value: c.id, labelKey: c.name })),
    },
  ]);

  readonly columns: ColumnDef<Item>[] = [
    {
      key: 'name',
      labelKey: 'items.col.name',
      sortField: 'name',
      value: row => row.name,
      subValue: row => row.description ?? null,
    },
    {
      key: 'kind',
      labelKey: 'items.col.kind',
      sortField: 'kind',
      value: row => this.i18n.t(row.kind === 'OWN_PRODUCT' ? 'items.kind.ownProduct' : 'items.kind.purchased'),
      badgeClass: () => 'fluent-badge',
    },
    {
      key: 'category',
      labelKey: 'items.col.category',
      value: row => row.categoryName ?? '—',
    },
    {
      key: 'unit',
      labelKey: 'items.col.unit',
      sortField: 'unit',
      value: row => row.unit,
    },
    {
      key: 'price',
      labelKey: 'items.col.price',
      sortField: 'price',
      value: row => row.price === null ? '—' : row.price.toLocaleString('de-DE', { style: 'currency', currency: 'EUR' }),
    },
    {
      key: 'createdAt',
      labelKey: 'items.col.createdAt',
      sortField: 'createdAt',
      value: row => new Date(row.createdAt).toLocaleDateString('de-DE'),
    },
  ];

  ngOnInit(): void {
    this.categoriesService.getAll().subscribe({
      next: cats => this.categories.set(cats),
      error: () => this.categories.set([]),
    });
    this.load({ size: 20, filters: {} });
  }

  onQuery(query: TableQuery): void {
    this.lastQuery = query;
    this.load(query);
  }

  onRowClick(row: Item): void {
    void this.router.navigate(['/items', row.id]);
  }

  onSelectionChange(ids: string[]): void {
    this.selection.set(ids);
  }

  onCreated(): void {
    this.showCreateDialog.set(false);
    this.load({ ...this.lastQuery, page: 0 });
  }

  confirmDeleteSelected(): void {
    if (!window.confirm(this.i18n.t('items.confirm.deleteSelected'))) return;
    this.deleting.set(true);
    const ids = this.selection();
    let remaining = ids.length;
    let succeeded = 0;
    let failed = 0;
    ids.forEach(id => {
      this.itemsService.delete(id).subscribe({
        next: () => {
          succeeded++;
          remaining--;
          if (remaining === 0) this.finishDelete(succeeded, failed);
        },
        error: () => {
          failed++;
          remaining--;
          if (remaining === 0) this.finishDelete(succeeded, failed);
        },
      });
    });
  }

  private finishDelete(succeeded: number, failed: number): void {
    this.deleting.set(false);
    this.selection.set([]);
    this.showFeedback(failed > 0 ? 'error' : 'success', succeeded);
    this.load({ ...this.lastQuery, page: 0 });
  }

  private showFeedback(type: 'success' | 'error', count: number): void {
    if (this.feedbackTimer) clearTimeout(this.feedbackTimer);
    this.deleteFeedback.set({ type, count });
    this.feedbackTimer = setTimeout(() => this.deleteFeedback.set(null), 3500);
  }

  private load(query: TableQuery): void {
    this.lastQuery = query;
    this.loading.set(true);
    this.itemsService.getAll(query).subscribe({
      next: page => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
