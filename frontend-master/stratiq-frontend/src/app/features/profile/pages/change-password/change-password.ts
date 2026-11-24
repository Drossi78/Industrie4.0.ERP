import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { Navbar } from '../../../../shared/components/navbar/navbar';
import { AuthService } from '../../../auth/services/auth.service';
import { AuthSessionService } from '../../../auth/services/auth-session.service';

// Cross-field validator: newPassword and confirmPassword must match.
function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const next = group.get('newPassword')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return next && confirm && next !== confirm ? { mismatch: true } : null;
}

@Component({
  selector: 'app-change-password',
  imports: [ReactiveFormsModule, TranslatePipe, Navbar],
  templateUrl: './change-password.html',
})
export class ChangePassword {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  protected readonly session = inject(AuthSessionService);

  protected readonly submitting = signal(false);
  protected readonly errorKey = signal<string | null>(null);
  protected readonly success = signal(false);

  protected readonly form = this.fb.nonNullable.group(
    {
      oldPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatch },
  );

  protected submit(): void {
    this.errorKey.set(null);
    this.success.set(false);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const { oldPassword, newPassword } = this.form.getRawValue();
    this.auth.changePassword({ oldPassword, newPassword }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.success.set(true);
        this.form.reset({ oldPassword: '', newPassword: '', confirmPassword: '' });
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        if (err instanceof HttpErrorResponse) {
          // 400 = wrong old password (or failed validation); 0 = no connection.
          if (err.status === 400) {
            this.errorKey.set('profile.password.error.wrongOld');
            return;
          }
          if (err.status === 0) {
            this.errorKey.set('profile.password.error.network');
            return;
          }
        }
        this.errorKey.set('profile.password.error.generic');
      },
    });
  }
}
