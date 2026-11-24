import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { I18nService, type AppLanguage } from '../../../../core/i18n/i18n.service';
import { AuthService } from '../../services/auth.service';
import { AuthSessionService } from '../../services/auth-session.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, TranslatePipe],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly session = inject(AuthSessionService);
  private readonly router = inject(Router);
  protected readonly i18n = inject(I18nService);

  protected readonly submitting = signal(false);
  protected readonly errorKey = signal<string | null>(null);

  protected readonly languages: { id: AppLanguage; labelKey: string }[] = [
    { id: 'de', labelKey: 'login.lang.de' },
    { id: 'en', labelKey: 'login.lang.en' },
    { id: 'fr', labelKey: 'login.lang.fr' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(1)]],
  });

  protected async onLangChange(lang: AppLanguage): Promise<void> {
    await this.i18n.setLanguage(lang);
  }

  protected submit(): void {
    this.errorKey.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    const { username, password } = this.form.getRawValue();
    this.auth.login({ username, password }).subscribe({
      next: (res) => {
        this.session.applyLogin(res);
        this.submitting.set(false);
        void this.router.navigateByUrl('/');
      },
      error: (err: unknown) => {
        this.submitting.set(false);
        if (err instanceof HttpErrorResponse) {
          if (err.status === 401) {
            this.errorKey.set('login.error.unauthorized');
            return;
          }
          if (err.status === 0) {
            this.errorKey.set('login.error.network');
            return;
          }
        }
        this.errorKey.set('login.error.generic');
      },
    });
  }
}
