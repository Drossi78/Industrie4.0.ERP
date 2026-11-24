import { Component, computed, inject, Input, OnInit, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { forkJoin } from 'rxjs';

import { ProductionStepsService } from '../../services/production-steps.service';
import { ProductionStep } from '../../models/items.model';
import { Uuid } from '../../../../shared/models/common.model';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-production-step-list',
  imports: [ReactiveFormsModule, DragDropModule, NgTemplateOutlet, TranslatePipe],
  templateUrl: './production-step-list.html',
})
export class ProductionStepListComponent implements OnInit {
  @Input({ required: true }) itemId!: Uuid;

  private readonly productionStepsService = inject(ProductionStepsService);
  private readonly session = inject(AuthSessionService);
  readonly i18n = inject(I18nService);

  readonly canEdit = computed(() => this.session.hasPermission(PermissionCode.ProductionWrite));
  readonly canDelete = computed(() => this.session.hasPermission(PermissionCode.ProductionDelete));

  readonly steps = signal<ProductionStep[]>([]);
  readonly loading = signal(false);
  readonly reordering = signal(false);
  readonly saving = signal(false);
  readonly editingId = signal<string | null>(null);
  readonly reorderError = signal(false);
  readonly saveError = signal(false);
  readonly newOrder = signal(0);

  readonly editForm = new FormGroup({
    name: new FormControl('', { validators: [Validators.required], nonNullable: true }),
    description: new FormControl<string | null>(null),
  });

  ngOnInit(): void {
    this.loading.set(true);
    this.productionStepsService.getByItem(this.itemId).subscribe({
      next: steps => {
        this.steps.set([...steps].sort((a, b) => a.stepOrder - b.stepOrder));
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  startEdit(step: ProductionStep): void {
    this.editForm.reset({ name: step.name, description: step.description ?? null });
    this.newOrder.set(step.stepOrder);
    this.saveError.set(false);
    this.editingId.set(step.id);
  }

  startNew(): void {
    this.editForm.reset({ name: '', description: null });
    this.newOrder.set(Math.max(0, ...this.steps().map(s => s.stepOrder)) + 1);
    this.saveError.set(false);
    this.editingId.set('new');
  }

  cancelEdit(): void {
    this.editForm.reset({ name: '', description: null });
    this.saveError.set(false);
    this.editingId.set(null);
  }

  onOrderInput(event: Event): void {
    const value = Number((event.target as HTMLInputElement).value);
    this.newOrder.set(Number.isFinite(value) ? value : 0);
  }

  save(): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }
    const editingId = this.editingId();
    if (!editingId) return;

    const { name, description } = this.editForm.getRawValue();

    this.saving.set(true);
    this.saveError.set(false);

    const request$ = editingId === 'new'
      ? this.productionStepsService.create({
          itemId: this.itemId,
          name: name.trim(),
          description: description?.trim() || null,
          stepOrder: this.newOrder(),
        })
      : this.productionStepsService.update(editingId, {
          name: name.trim(),
          description: description?.trim() || null,
          stepOrder: this.newOrder(),
        });

    request$.subscribe({
      next: savedStep => {
        const next = editingId === 'new'
          ? [...this.steps(), savedStep]
          : this.steps().map(step => (step.id === editingId ? savedStep : step));
        this.steps.set(next.sort((a, b) => a.stepOrder - b.stepOrder));
        this.saving.set(false);
        this.editingId.set(null);
      },
      error: () => {
        this.saving.set(false);
        this.saveError.set(true);
      },
    });
  }

  deleteStep(step: ProductionStep): void {
    if (!window.confirm(this.i18n.t('items.steps.confirm.delete'))) return;

    this.productionStepsService.delete(step.id).subscribe({
      next: () => {
        this.steps.set(this.steps().filter(s => s.id !== step.id));
        if (this.editingId() === step.id) {
          this.editingId.set(null);
        }
      },
    });
  }

  onDrop(event: CdkDragDrop<ProductionStep[]>): void {
    if (this.reordering()) return;

    const previous = this.steps();
    const reordered = [...previous];
    moveItemInArray(reordered, event.previousIndex, event.currentIndex);
    const next = reordered.map((step, index) => ({ ...step, stepOrder: index + 1 }));
    this.steps.set(next);

    // No bulk reorder endpoint — persist each step whose position actually changed individually.
    const toPersist = next.filter(step => {
      const before = previous.find(s => s.id === step.id);
      return !before || before.stepOrder !== step.stepOrder;
    });

    if (toPersist.length === 0) {
      return;
    }

    this.reordering.set(true);
    this.reorderError.set(false);
    forkJoin(
      toPersist.map(step =>
        this.productionStepsService.update(step.id, { stepOrder: step.stepOrder }),
      ),
    ).subscribe({
      next: updatedSteps => {
        const byId = new Map(updatedSteps.map(s => [s.id, s]));
        this.steps.set(
          next.map(step => byId.get(step.id) ?? step).sort((a, b) => a.stepOrder - b.stepOrder),
        );
        this.reordering.set(false);
      },
      error: () => {
        this.steps.set(previous);
        this.reordering.set(false);
        this.reorderError.set(true);
      },
    });
  }

  trackById(_index: number, step: ProductionStep): string {
    return step.id;
  }
}
