import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { DataTable, ColumnDef, FilterDef } from '../../../../shared/components/data-table/data-table';
import { SuppliersService } from '../../services/suppliers.services';
import { Supplier } from '../../models/supplier.model';
import { Paginated, TableQuery } from '../../../../shared/models/common.model';

@Component({
  selector: 'app-suppliers-list',
  imports: [TranslatePipe, DataTable, RouterLink],
  templateUrl: './suppliers-list.html',
  styleUrl: './suppliers-list.css',
})
export class SuppliersList implements OnInit {
  private readonly suppliersService = inject(SuppliersService);

  readonly page = signal<Paginated<Supplier> | null>(null);
  readonly loading = signal(true);

  readonly filterDefs: FilterDef[] = [
    { key: 'name', labelKey: 'suppliers.filter.name', type: 'text' },
  ];

  readonly columns: ColumnDef<Supplier>[] = [
    {
      key: 'name',
      labelKey: 'suppliers.col.name',
      sortField: 'name',
      value: row => row.name,
      subValue: row => row.email ?? null,
    },
    {
      key: 'phone',
      labelKey: 'suppliers.col.phone',
      value: row => row.phone ?? '—',
    },
    {
      key: 'city',
      labelKey: 'suppliers.col.city',
      sortField: 'city',
      value: row => [row.city, row.country].filter(Boolean).join(', ') || '—',
    },
    {
      key: 'taxId',
      labelKey: 'suppliers.col.taxId',
      value: row => row.taxId ?? '—',
    },
    {
      key: 'createdAt',
      labelKey: 'suppliers.col.createdAt',
      sortField: 'createdAt',
      value: row => new Date(row.createdAt).toLocaleDateString('de-DE'),
    },
  ];

  ngOnInit(): void {
    this.load({ size: 20, filters: {} });
  }

  onQuery(query: TableQuery): void {
    this.load(query);
  }

  private load(query: TableQuery): void {
    this.loading.set(true);
    this.suppliersService.getAll(query).subscribe({
      next: page => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
