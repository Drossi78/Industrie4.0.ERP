import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { ItemsService } from '../../services/items.service';
import { Item, ItemKind } from '../../models/items.model';

@Component({
  selector: 'app-item-create-dialog',
  imports: [ReactiveFormsModule, TranslatePipe],
  templateUrl: './item-create-dialog.html',
})
export class ItemCreateDialog {
  private readonly itemsService = inject(ItemsService);

  @Output() created = new EventEmitter<Item>();
  @Output() cancelled = new EventEmitter<void>();

  readonly saving = signal(false);
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

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.serverError.set(null);
    const v = this.form.getRawValue();
    this.itemsService.create({
      name: v.name,
      kind: v.kind,
      unit: v.unit,
      categoryId: v.categoryId ?? null,
      price: v.price ?? null,
      description: v.description ?? null,
      notes: v.notes ?? null,
    }).subscribe({
      next: item => {
        this.saving.set(false);
        this.created.emit(item);
      },
      error: () => {
        this.saving.set(false);
        this.serverError.set('items.error.saveFailed');
      },
    });
  }
}
