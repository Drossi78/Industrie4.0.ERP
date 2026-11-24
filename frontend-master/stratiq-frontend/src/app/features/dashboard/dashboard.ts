import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '../../core/i18n/translate.pipe';
import { Navbar } from '../../shared/components/navbar/navbar';
import { NAV_AREAS } from '../../shared/models/nav-area.model';
import { AreaIcon } from '../../shared/components/area-icon/area-icon';
import { AuthSessionService } from '../auth/services/auth-session.service';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, TranslatePipe, Navbar, AreaIcon],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  private readonly session = inject(AuthSessionService);

  readonly areas = computed(() =>
    NAV_AREAS.filter(area => this.session.hasAnyPermission(...area.permissions)),
  );
}
