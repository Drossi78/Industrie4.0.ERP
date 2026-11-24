import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { CustomersService } from '../../services/customers.service';
import { Customer } from '../../models/customer.model';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-customer-detail',
  imports: [ReactiveFormsModule, TranslatePipe, Navbar],
  templateUrl: './customer-detail.html',
})
export class CustomerDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly customersService = inject(CustomersService);
  private readonly session = inject(AuthSessionService);
  readonly i18n = inject(I18nService);

  readonly canEdit = computed(() => this.session.hasPermission(PermissionCode.CustomerWrite));
  readonly canDelete = computed(() => this.session.hasPermission(PermissionCode.CustomerDelete));

  private customerId = '';

  readonly customer = signal<Customer | null>(null);
  readonly loading = signal(true);
  readonly editing = signal(false);
  readonly saving = signal(false);
  readonly deleting = signal(false);
  readonly serverError = signal<string | null>(null);

  readonly form = new FormGroup({
    name: new FormControl('', { validators: [Validators.required], nonNullable: true }),
    email: new FormControl<string | null>(null),
    phone: new FormControl<string | null>(null),
    taxId: new FormControl<string | null>(null),
    street: new FormControl<string | null>(null),
    houseNumber: new FormControl<string | null>(null),
    zipCode: new FormControl<string | null>(null),
    city: new FormControl<string | null>(null),
    state: new FormControl<string | null>(null),
    country: new FormControl<string | null>(null),
    notes: new FormControl<string | null>(null),
  });

  ngOnInit(): void {
    this.customerId = this.route.snapshot.paramMap.get('id') ?? '';
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.customersService.getById(this.customerId).subscribe({
      next: customer => {
        this.customer.set(customer);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  startEditing(): void {
    const c = this.customer();
    if (!c) return;
    this.form.setValue({
      name: c.name,
      email: c.email ?? null,
      phone: c.phone ?? null,
      taxId: c.taxId ?? null,
      street: c.street ?? null,
      houseNumber: c.houseNumber ?? null,
      zipCode: c.zipCode ?? null,
      city: c.city ?? null,
      state: c.state ?? null,
      country: c.country ?? null,
      notes: c.notes ?? null,
    });
    this.form.markAsPristine();
    this.serverError.set(null);
    this.editing.set(true);
  }

  cancelEditing(): void {
    this.form.markAsPristine();
    this.editing.set(false);
  }

  saveEditing(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.serverError.set(null);
    const v = this.form.getRawValue();
    this.customersService.update(this.customerId, {
      name: v.name,
      email: v.email ?? null,
      phone: v.phone ?? null,
      taxId: v.taxId ?? null,
      street: v.street ?? null,
      houseNumber: v.houseNumber ?? null,
      zipCode: v.zipCode ?? null,
      city: v.city ?? null,
      state: v.state ?? null,
      country: v.country ?? null,
      notes: v.notes ?? null,
    }).subscribe({
      next: updated => {
        this.customer.set(updated);
        this.saving.set(false);
        this.form.markAsPristine();
        this.editing.set(false);
      },
      error: err => {
        this.saving.set(false);
        this.serverError.set(this.extractError(err, 'customers.error.saveFailed'));
      },
    });
  }

  cityLine(c: Customer): string {
    return [c.zipCode, c.city].filter(v => v).join(' ') || '—';
  }

  confirmDelete(): void {
    if (!window.confirm(this.i18n.t('customers.confirm.delete'))) return;
    this.deleting.set(true);
    this.serverError.set(null);
    this.customersService.delete(this.customerId).subscribe({
      next: () => {
        void this.router.navigate(['/management']);
      },
      error: err => {
        this.deleting.set(false);
        this.serverError.set(this.extractError(err, 'customers.error.deleteFailed'));
      },
    });
  }

  private extractError(err: unknown, fallbackKey: string): string {
    if (err instanceof HttpErrorResponse && err.error?.message) {
      return err.error.message;
    }
    return fallbackKey;
  }
}
