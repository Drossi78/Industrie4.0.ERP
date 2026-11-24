import { Component, OnInit, computed, inject, Input, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { BomService } from '../../services/bom.service';
import { BomLine } from '../../models/bom.model';
import { ItemsService } from '../../services/items.service';
import { Item } from '../../models/items.model';
import { Uuid } from '../../../../shared/models/common.model';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-bom-list',
  imports: [ReactiveFormsModule, TranslatePipe],
  templateUrl: './bom-list.html',
})
export class BomListComponent implements OnInit {
  @Input({ required: true }) itemId!: Uuid;

  private readonly bomService = inject(BomService);
  private readonly itemsService = inject(ItemsService);
  private readonly session = inject(AuthSessionService);
  readonly i18n = inject(I18nService);

  readonly canEdit = computed(() => this.session.hasPermission(PermissionCode.InventoryWrite));
  readonly canDelete = computed(() => this.session.hasPermission(PermissionCode.InventoryDelete));

  readonly lines = signal<BomLine[]>([]);
  readonly loading = signal(false);
  readonly components = signal<Item[]>([]);
  readonly adding = signal(false);
  readonly saving = signal(false);
  readonly saveError = signal<string | null>(null);
  readonly deletingId = signal<Uuid | null>(null);

  readonly addForm = new FormGroup({
    componentId: new FormControl('', { validators: [Validators.required], nonNullable: true }),
    quantity: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0.001)] }),
    unit: new FormControl('', { validators: [Validators.required], nonNullable: true }),
    notes: new FormControl<string | null>(null),
  });

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.bomService.getByItem(this.itemId).subscribe({
      next: lines => {
        this.lines.set(lines);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  startAdd(): void {
    this.saveError.set(null);
    this.addForm.reset({ componentId: '', quantity: null, unit: '', notes: null });
    this.adding.set(true);
    if (this.components().length === 0) {
      this.itemsService.getAll({ filters: { kind: 'PURCHASED' }, size: 500 }).subscribe({
        next: page => this.components.set(page.content.filter(i => !i.isDeleted)),
        error: () => this.components.set([]),
      });
    }
  }

  cancelAdd(): void {
    this.adding.set(false);
    this.saveError.set(null);
  }

  submitAdd(): void {
    if (this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      return;
    }
    const v = this.addForm.getRawValue();
    this.saving.set(true);
    this.saveError.set(null);
    this.bomService.create({
      itemId: this.itemId,
      componentId: v.componentId,
      quantity: v.quantity!,
      unit: v.unit,
      notes: v.notes?.trim() || null,
    }).subscribe({
      next: line => {
        this.lines.set([...this.lines(), line]);
        this.saving.set(false);
        this.adding.set(false);
      },
      error: err => {
        this.saving.set(false);
        this.saveError.set(this.extractError(err, 'items.bom.error.save'));
      },
    });
  }

  deleteLine(line: BomLine): void {
    if (!window.confirm(this.i18n.t('items.bom.confirm.delete'))) return;
    this.deletingId.set(line.id);
    this.bomService.delete(line.id).subscribe({
      next: () => {
        this.lines.set(this.lines().filter(l => l.id !== line.id));
        this.deletingId.set(null);
      },
      error: () => this.deletingId.set(null),
    });
  }

  trackById(_index: number, line: BomLine): string {
    return line.id;
  }

  private extractError(err: unknown, fallbackKey: string): string {
    if (err instanceof HttpErrorResponse && err.error?.message) {
      return err.error.message;
    }
    return fallbackKey;
  }
}
