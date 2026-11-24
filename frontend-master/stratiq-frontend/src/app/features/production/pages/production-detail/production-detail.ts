import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { ProgressRing } from '../../components/progress-ring/progress-ring';
import { UsersService } from '../../../management/services/users.service';
import { ProductionService, UpdateStepRequest } from '../../services/production.service';
import { ProductionOrderDetail, ProductionOrderLineStep } from '../../models/production-dashboard.model';
import { ProductionStepStatus } from '../../../../shared/enums/production-step-status.enum';
import { AppUser } from '../../../management/models/app-user.model';
import { WarehousesService } from '../../../warehouses/services/warehouses.service';
import { Warehouse } from '../../../warehouses/models/warehouse.model';
import { Uuid } from '../../../../shared/models/common.model';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-production-detail',
  imports: [TranslatePipe, Navbar, ProgressRing],
  templateUrl: './production-detail.html',
})
export class ProductionDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly productionService = inject(ProductionService);
  private readonly usersService = inject(UsersService);
  private readonly warehousesService = inject(WarehousesService);
  private readonly i18n = inject(I18nService);
  private readonly session = inject(AuthSessionService);

  readonly canEdit = computed(() => this.session.hasPermission(PermissionCode.ProductionWrite));

  private orderId = '';

  readonly order = signal<ProductionOrderDetail | null>(null);
  readonly users = signal<AppUser[]>([]);
  readonly warehouses = signal<Warehouse[]>([]);
  readonly loading = signal(true);

  readonly updating = signal<Set<Uuid>>(new Set());
  readonly updateError = signal<string | null>(null);

  // Warehouse-selection prompt shown when completing the last step of an order line.
  readonly pendingCompletion = signal<{ lineId: Uuid; step: ProductionOrderLineStep } | null>(null);
  readonly pendingWarehouseId = signal('');

  readonly ProductionStepStatus = ProductionStepStatus;

  ngOnInit(): void {
    this.orderId = this.route.snapshot.paramMap.get('id')!;

    // Loaded independently: the assignment dropdown (users) and warehouse-selection
    // prompt (warehouses) are edit-only affordances the current role may not have
    // read access to (e.g. a production-only role lacks user:read) — a 403 on either
    // must not block the order itself from rendering.
    this.productionService.getOrderDetail(this.orderId).subscribe({
      next: order => { this.order.set(order); this.loading.set(false); },
      error: () => this.loading.set(false),
    });

    this.usersService.getActive().subscribe({
      next: users => this.users.set(users),
      error: () => this.users.set([]),
    });

    this.warehousesService.getAll({ size: 100 }).subscribe({
      next: page => this.warehouses.set(page.content),
      error: () => this.warehouses.set([]),
    });
  }

  setStatus(lineId: Uuid, step: ProductionOrderLineStep, status: ProductionStepStatus): void {
    if (status === ProductionStepStatus.DONE && this.isLastStep(lineId, step)) {
      this.pendingWarehouseId.set('');
      this.pendingCompletion.set({ lineId, step });
      return;
    }
    this.patchStep(lineId, step, { status }, { status });
  }

  private isLastStep(lineId: Uuid, step: ProductionOrderLineStep): boolean {
    const line = this.order()?.assets.find(l => l.id === lineId);
    if (!line) return false;
    const maxOrder = Math.max(...line.steps.map(s => s.stepOrder));
    return step.stepOrder === maxOrder;
  }

  confirmCompletion(): void {
    const pending = this.pendingCompletion();
    if (!pending || !this.pendingWarehouseId()) return;
    this.patchStep(
      pending.lineId,
      pending.step,
      { status: ProductionStepStatus.DONE, warehouseId: this.pendingWarehouseId() },
      { status: ProductionStepStatus.DONE },
    );
    this.pendingCompletion.set(null);
  }

  cancelCompletion(): void {
    this.pendingCompletion.set(null);
  }

  setAssignment(lineId: Uuid, step: ProductionOrderLineStep, value: string): void {
    const assignee = value ? this.users().find(u => u.id === value) : undefined;
    this.patchStep(
      lineId,
      step,
      { assignedTo: value || null },
      { assignedTo: assignee ? { id: assignee.id, email: assignee.email } : null },
    );
  }

  // payload: sent to the backend (PATCH /production/steps/{id} takes a plain assignedTo id).
  // optimistic: merged into local state immediately, already in display shape (assignedTo as {id, email}).
  private patchStep(
    lineId: Uuid,
    step: ProductionOrderLineStep,
    payload: UpdateStepRequest,
    optimistic: Partial<ProductionOrderLineStep>,
  ): void {
    if (this.updating().has(step.id)) return;

    const prev = this.getStep(lineId, step.id);
    this.replaceStep(lineId, { ...step, ...optimistic });
    this.updating.update(s => new Set(s).add(step.id));
    this.updateError.set(null);

    this.productionService.updateStep(step.id, payload).subscribe({
      next: updated => {
        // The PATCH response returns assignedTo as a plain id (unlike the nested
        // {id, email} object from GET /production/orders/{id}) — re-hydrate the email locally.
        const assignedTo = updated.assignedTo
          ? { id: updated.assignedTo, email: this.users().find(u => u.id === updated.assignedTo)?.email ?? updated.assignedTo }
          : null;
        this.replaceStep(lineId, {
          id: updated.id,
          name: updated.name,
          stepOrder: updated.stepOrder,
          status: updated.status,
          assignedTo,
          startedAt: updated.startedAt ?? null,
          completedAt: updated.completedAt ?? null,
        });
        this.updating.update(s => { const n = new Set(s); n.delete(step.id); return n; });
      },
      error: err => {
        if (prev) this.replaceStep(lineId, prev);
        this.updating.update(s => { const n = new Set(s); n.delete(step.id); return n; });
        this.updateError.set(this.extractError(err, 'production.error.updateFailed'));
      },
    });
  }

  private getStep(lineId: Uuid, stepId: Uuid): ProductionOrderLineStep | undefined {
    return this.order()?.assets.find(l => l.id === lineId)?.steps.find(s => s.id === stepId);
  }

  private replaceStep(lineId: Uuid, updated: ProductionOrderLineStep): void {
    this.order.update(order => {
      if (!order) return order;
      return {
        ...order,
        assets: order.assets.map(line => line.id !== lineId ? line : {
          ...line,
          steps: line.steps.map(s => s.id === updated.id ? updated : s),
          completedSteps: line.steps.filter(s =>
            s.id === updated.id ? updated.status === ProductionStepStatus.DONE : s.status === ProductionStepStatus.DONE,
          ).length,
        }),
      };
    });
  }

  doneCount(steps: ProductionOrderLineStep[]): number {
    return steps.filter(s => s.status === ProductionStepStatus.DONE).length;
  }

  isUpdating(stepId: Uuid): boolean {
    return this.updating().has(stepId);
  }

  stepBadgeClass(status: ProductionStepStatus): string {
    switch (status) {
      case ProductionStepStatus.IN_PROGRESS: return 'fluent-badge';
      case ProductionStepStatus.DONE:        return 'fluent-badge-success';
      default:                               return 'fluent-badge-neutral';
    }
  }

  stepLabel(status: ProductionStepStatus): string {
    return this.i18n.t(`production.step.${status.toLowerCase()}`);
  }

  private extractError(err: unknown, fallbackKey: string): string {
    if (err instanceof HttpErrorResponse && err.error?.message) {
      return err.error.message;
    }
    return this.i18n.t(fallbackKey);
  }
}
