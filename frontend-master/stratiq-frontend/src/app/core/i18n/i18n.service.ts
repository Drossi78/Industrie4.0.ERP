import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export type AppLanguage = 'de' | 'en' | 'fr';

const STORAGE_KEY = 'stratiq.lang';

@Injectable({ providedIn: 'root' })
export class I18nService {
  private readonly http = inject(HttpClient);

  private readonly messages = signal<Record<string, string>>({});

  readonly language = signal<AppLanguage>(this.readStoredLanguage());

  constructor() {
    void this.setLanguage(this.language(), false);
  }

  t(key: string): string {
    return this.messages()[key] ?? key;
  }

  async setLanguage(lang: AppLanguage, persist = true): Promise<void> {
    this.language.set(lang);
    if (persist) {
      try {
        localStorage.setItem(STORAGE_KEY, lang);
      } catch {
        /* ignore */
      }
    }
    const data = await firstValueFrom(
      this.http.get<Record<string, string>>(`/i18n/${lang}.json`),
    );
    this.messages.set(data);
  }

  private readStoredLanguage(): AppLanguage {
    try {
      const raw = localStorage.getItem(STORAGE_KEY) as AppLanguage | null;
      if (raw === 'de' || raw === 'en' || raw === 'fr') {
        return raw;
      }
    } catch {
      /* ignore */
    }
    return 'de';
  }
}
