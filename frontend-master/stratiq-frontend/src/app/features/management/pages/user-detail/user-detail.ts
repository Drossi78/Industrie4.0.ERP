import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService } from '../../../../core/i18n/i18n.service';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { UsersService } from '../../services/users.service';
import { AppUser, AppRole } from '../../models/app-user.model';
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

@Component({
  selector: 'app-user-detail',
  imports: [ReactiveFormsModule, TranslatePipe, Navbar],
  templateUrl: './user-detail.html',
})
export class UserDetail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly usersService = inject(UsersService);
  private readonly session = inject(AuthSessionService);
  readonly i18n = inject(I18nService);

  readonly canEdit = computed(() => this.session.hasPermission(PermissionCode.UserWrite));
  readonly canDelete = computed(() => this.session.hasPermission(PermissionCode.UserDelete));

  private userId = '';

  readonly user = signal<AppUser | null>(null);
  readonly roles = signal<AppRole[]>([]);
  readonly loading = signal(true);
  readonly editing = signal(false);
  readonly saving = signal(false);
  readonly deleting = signal(false);
  readonly serverError = signal<string | null>(null);

  readonly form = new FormGroup({
    email: new FormControl('', { validators: [Validators.required, Validators.email], nonNullable: true }),
    roleId: new FormControl('', { validators: [Validators.required], nonNullable: true }),
    preferredLang: new FormControl('de', { nonNullable: true }),
    isActive: new FormControl(true, { nonNullable: true }),
  });

  ngOnInit(): void {
    this.userId = this.route.snapshot.paramMap.get('id') ?? '';
    this.usersService.getRoles().subscribe({
      next: roles => this.roles.set(roles),
    });
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.usersService.getById(this.userId).subscribe({
      next: user => {
        this.user.set(user);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  startEditing(): void {
    const u = this.user();
    if (!u) return;
    this.form.setValue({
      email: u.email,
      roleId: u.roleId,
      preferredLang: u.preferredLang,
      isActive: u.isActive,
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
    this.usersService.update(this.userId, {
      email: v.email,
      roleId: v.roleId,
      preferredLang: v.preferredLang,
      isActive: v.isActive,
    }).subscribe({
      next: updated => {
        this.user.set(updated);
        this.saving.set(false);
        this.form.markAsPristine();
        this.editing.set(false);
      },
      error: err => {
        this.saving.set(false);
        this.serverError.set(this.extractError(err, 'management.error.saveFailed'));
      },
    });
  }

  confirmDelete(): void {
    if (!window.confirm(this.i18n.t('management.confirm.deleteUser'))) return;
    this.deleting.set(true);
    this.serverError.set(null);
    this.usersService.delete(this.userId).subscribe({
      next: () => {
        void this.router.navigate(['/management']);
      },
      error: err => {
        this.deleting.set(false);
        this.serverError.set(this.extractError(err, 'management.error.deleteFailed'));
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
