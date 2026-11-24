import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { DataTable, ColumnDef } from '../../../../shared/components/data-table/data-table';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { ProductionService } from '../../services/production.service';
import { ProductionOrderSummary } from '../../models/production-dashboard.model';
import { Paginated } from '../../../../shared/models/common.model';

@Component({
  selector: 'app-production-list',
  imports: [TranslatePipe, DataTable, Navbar],
  templateUrl: './production-list.html',
})
export class ProductionList implements OnInit {
  private readonly productionService = inject(ProductionService);
  private readonly router = inject(Router);

  readonly page = signal<Paginated<ProductionOrderSummary> | null>(null);
  readonly loading = signal(true);

  readonly columns: ColumnDef<ProductionOrderSummary>[] = [
    {
      key: 'orderNumber',
      labelKey: 'production.col.order',
      value: row => row.orderNumber ?? '—',
    },
    {
      key: 'customerName',
      labelKey: 'production.col.customer',
      value: row => row.customerName || '—',
    },
    {
      key: 'orderDate',
      labelKey: 'production.col.date',
      value: row => new Date(row.orderDate).toLocaleDateString('de-DE'),
    },
    {
      key: 'progress',
      labelKey: 'production.col.progress',
      value: row => `${row.completedSteps} / ${row.totalSteps} (${row.progressPercent}%)`,
    },
  ];

  ngOnInit(): void {
    this.load();
  }

  onRowClick(row: ProductionOrderSummary): void {
    void this.router.navigate(['/production', row.id]);
  }

  private load(): void {
    this.loading.set(true);
    this.productionService.getOrders().subscribe({
      next: orders => {
        this.page.set(this.toPage(orders));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  private toPage(orders: ProductionOrderSummary[]): Paginated<ProductionOrderSummary> {
    return {
      content: orders,
      totalElements: orders.length,
      totalPages: 1,
      number: 0,
      size: orders.length,
      first: true,
      last: true,
      numberOfElements: orders.length,
      empty: orders.length === 0,
    };
  }
}
