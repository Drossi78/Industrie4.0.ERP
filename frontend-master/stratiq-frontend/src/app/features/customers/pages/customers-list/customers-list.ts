import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { DataTable, ColumnDef } from '../../../../shared/components/data-table/data-table';
import { CustomersService } from '../../services/customers.service';
import { Customer } from '../../models/customer.model';
import { Paginated, ListQuery, TableQuery } from '../../../../shared/models/common.model';

@Component({
  selector: 'app-customers-list',
  imports: [TranslatePipe, DataTable, RouterLink],
  templateUrl: './customers-list.html',
  styleUrl: './customers-list.css',
})
export class CustomersList implements OnInit {
  private readonly customersService = inject(CustomersService);

  readonly page = signal<Paginated<Customer> | null>(null);
  readonly loading = signal(true);

  readonly columns: ColumnDef<Customer>[] = [
    {
      key: 'name',
      labelKey: 'customers.col.name',
      sortField: 'name',
      value: row => row.name,
      subValue: row => row.email ?? null,
    },
    {
      key: 'phone',
      labelKey: 'customers.col.phone',
      value: row => row.phone ?? '—',
    },
    {
      key: 'city',
      labelKey: 'customers.col.city',
      sortField: 'city',
      value: row => [row.city, row.country].filter(Boolean).join(', ') || '—',
    },
    {
      key: 'taxId',
      labelKey: 'customers.col.taxId',
      value: row => row.taxId ?? '—',
    },
    {
      key: 'createdAt',
      labelKey: 'customers.col.createdAt',
      sortField: 'createdAt',
      value: row => new Date(row.createdAt).toLocaleDateString('de-DE'),
    },
  ];

  ngOnInit(): void {
    this.load({ size: 20 });
  }

  onQuery(query: TableQuery): void {
    this.load(query);
  }

  private load(query: ListQuery): void {
    this.loading.set(true);
    this.customersService.getAll(query).subscribe({
      next: page => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
