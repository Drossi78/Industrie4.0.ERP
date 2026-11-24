import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { WarehousesService } from '../../services/warehouses.service';
import { Warehouse } from '../../models/warehouse.model';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-warehouse-detail',
  imports: [ReactiveFormsModule, TranslatePipe, Navbar],
  templateUrl: './warehouse-detail.html',
})
export class WarehouseDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly warehousesService = inject(WarehousesService);
  private readonly session = inject(AuthSessionService);
  readonly i18n = inject(I18nService);

  readonly canEdit = computed(() => this.session.hasPermission(PermissionCode.WarehouseWrite));
  readonly canDelete = computed(() => this.session.hasPermission(PermissionCode.WarehouseDelete));

  private warehouseId = '';

  readonly warehouse = signal<Warehouse | null>(null);
  readonly loading = signal(true);
  readonly editing = signal(false);
  readonly saving = signal(false);
  readonly deleting = signal(false);
  readonly serverError = signal<string | null>(null);

  readonly form = new FormGroup({
    name: new FormControl('', { validators: [Validators.required], nonNullable: true }),
    description: new FormControl<string | null>(null),
    street: new FormControl<string | null>(null),
    houseNumber: new FormControl<string | null>(null),
    zipCode: new FormControl<string | null>(null),
    city: new FormControl<string | null>(null),
    state: new FormControl<string | null>(null),
    country: new FormControl<string | null>(null),
    isActive: new FormControl(true, { nonNullable: true }),
  });

  ngOnInit(): void {
    this.warehouseId = this.route.snapshot.paramMap.get('id') ?? '';
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.warehousesService.getById(this.warehouseId).subscribe({
      next: warehouse => {
        this.warehouse.set(warehouse);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  startEditing(): void {
    const w = this.warehouse();
    if (!w) return;
    this.form.setValue({
      name: w.name,
      description: w.description ?? null,
      street: w.street ?? null,
      houseNumber: w.houseNumber ?? null,
      zipCode: w.zipCode ?? null,
      city: w.city ?? null,
      state: w.state ?? null,
      country: w.country ?? null,
      isActive: w.isActive,
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
    this.warehousesService.update(this.warehouseId, {
      name: v.name,
      description: v.description ?? null,
      street: v.street ?? null,
      houseNumber: v.houseNumber ?? null,
      zipCode: v.zipCode ?? null,
      city: v.city ?? null,
      state: v.state ?? null,
      country: v.country ?? null,
      isActive: v.isActive,
    }).subscribe({
      next: updated => {
        this.warehouse.set(updated);
        this.saving.set(false);
        this.form.markAsPristine();
        this.editing.set(false);
      },
      error: err => {
        this.saving.set(false);
        this.serverError.set(this.extractError(err, 'warehouses.error.saveFailed'));
      },
    });
  }

  cityLine(w: Warehouse): string {
    return [w.zipCode, w.city].filter(v => v).join(' ') || '—';
  }

  confirmDelete(): void {
    if (!window.confirm(this.i18n.t('warehouses.confirm.delete'))) return;
    this.deleting.set(true);
    this.serverError.set(null);
    this.warehousesService.delete(this.warehouseId).subscribe({
      next: () => {
        void this.router.navigate(['/management']);
      },
      error: err => {
        this.deleting.set(false);
        this.serverError.set(this.extractError(err, 'warehouses.error.deleteFailed'));
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
