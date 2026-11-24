import { Component, ElementRef, HostListener, Input, computed, inject, signal } from '@angular/core';
import { Location } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { I18nService, AppLanguage } from '../../../core/i18n/i18n.service';
import { AuthSessionService } from '../../../features/auth/services/auth-session.service';
import { NAV_AREAS } from '../../models/nav-area.model';
import { AreaIcon } from '../area-icon/area-icon';

const DOCS_URL = 'https://git.thm.de/stratiq/documentation';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, TranslatePipe, AreaIcon],
  templateUrl: './navbar.html',
})
export class Navbar {
  @Input() showBack = false;

  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly session = inject(AuthSessionService);
  private readonly el = inject(ElementRef);
  readonly i18n = inject(I18nService);

  readonly docsUrl = DOCS_URL;
  readonly languages: AppLanguage[] = ['de', 'en', 'fr'];
  readonly areas = computed(() =>
    NAV_AREAS.filter(area => this.session.hasAnyPermission(...area.permissions)),
  );

  readonly launcherOpen = signal(false);
  readonly accountMenuOpen = signal(false);

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.el.nativeElement.contains(event.target as Node)) {
      this.launcherOpen.set(false);
      this.accountMenuOpen.set(false);
    }
  }

  toggleLauncher(): void {
    const next = !this.launcherOpen();
    this.launcherOpen.set(next);
    if (next) {
      this.accountMenuOpen.set(false);
    }
  }

  toggleAccountMenu(): void {
    const next = !this.accountMenuOpen();
    this.accountMenuOpen.set(next);
    if (next) {
      this.launcherOpen.set(false);
    }
  }

  closeLauncher(): void {
    this.launcherOpen.set(false);
  }

  closeAccountMenu(): void {
    this.accountMenuOpen.set(false);
  }

  goBack(): void {
    this.location.back();
  }

  currentUser() {
    return this.session.getUser();
  }

  setLanguage(lang: AppLanguage): void {
    void this.i18n.setLanguage(lang);
  }

  logout(): void {
    this.session.clear();
    void this.router.navigate(['/auth/login']);
  }
}
