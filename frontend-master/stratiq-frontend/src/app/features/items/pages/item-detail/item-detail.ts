import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { ItemsService } from '../../services/items.service';
import { Item, ItemKind, UpdateItemRequest } from '../../models/items.model';
import { ProductionStepListComponent } from '../../components/production-step-list/production-step-list';
import { BomListComponent } from '../../components/bom-list/bom-list';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-item-detail',
  imports: [ReactiveFormsModule, TranslatePipe, Navbar, DatePipe, ProductionStepListComponent, BomListComponent],
  templateUrl: './item-detail.html',
})
export class ItemDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly itemsService = inject(ItemsService);
  private readonly session = inject(AuthSessionService);
  readonly i18n = inject(I18nService);

  readonly canEdit = computed(() => this.session.hasPermission(PermissionCode.InventoryWrite));
  readonly canDelete = computed(() => this.session.hasPermission(PermissionCode.InventoryDelete));

  readonly item = signal<Item | null>(null);
  readonly loading = signal(true);
  readonly editing = signal(false);
  readonly saving = signal(false);
  readonly deleting = signal(false);
  readonly serverError = signal<string | null>(null);

  readonly form = new FormGroup({
    name: new FormControl('', { validators: [Validators.required], nonNullable: true }),
    kind: new FormControl<ItemKind>('PURCHASED', { validators: [Validators.required], nonNullable: true }),
    unit: new FormControl('', { validators: [Validators.required], nonNullable: true }),
    categoryId: new FormControl<string | null>(null),
    price: new FormControl<number | null>(null),
    description: new FormControl<string | null>(null),
    notes: new FormControl<string | null>(null),
  });

  private itemId = '';

  ngOnInit(): void {
    this.itemId = this.route.snapshot.paramMap.get('id') ?? '';
    this.loadItem();
  }

  private loadItem(): void {
    this.loading.set(true);
    this.itemsService.getById(this.itemId).subscribe({
      next: item => {
        this.item.set(item);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  startEditing(): void {
    const item = this.item();
    if (!item) return;
    this.form.setValue({
      name: item.name,
      kind: item.kind,
      unit: item.unit,
      categoryId: item.categoryId ?? null,
      price: item.price ?? null,
      description: item.description ?? null,
      notes: item.notes ?? null,
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
    const payload: UpdateItemRequest = {
      name: v.name,
      kind: v.kind,
      unit: v.unit,
      categoryId: v.categoryId ?? null,
      price: v.price ?? null,
      description: v.description ?? null,
      notes: v.notes ?? null,
    };
    this.itemsService.update(this.itemId, payload).subscribe({
      next: updated => {
        this.item.set(updated);
        this.saving.set(false);
        this.form.markAsPristine();
        this.editing.set(false);
      },
      error: () => {
        this.saving.set(false);
        this.serverError.set('items.error.saveFailed');
      },
    });
  }

  confirmDelete(): void {
    const item = this.item();
    if (!item) return;
    if (!window.confirm(this.i18n.t('items.confirm.delete'))) return;
    this.deleting.set(true);
    this.itemsService.delete(this.itemId).subscribe({
      next: () => {
        void this.router.navigate(['/items']);
      },
      error: () => {
        this.deleting.set(false);
      },
    });
  }

  hasUnsavedChanges(): boolean {
    return this.editing() && this.form.dirty;
  }

  kindLabel(kind: ItemKind): string {
    return this.i18n.t(kind === 'OWN_PRODUCT' ? 'items.kind.ownProduct' : 'items.kind.purchased');
  }
}
