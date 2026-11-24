import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Paginated } from '../../../shared/models/common.model';
import { toHttpParams } from '../../../shared/utils/list-query-params';
import { Category } from '../models/category.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CategoriesService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/categories`;

  /** Fetches all categories (flattened) for use in filter dropdowns. */
  getAll(): Observable<Category[]> {
    return this.http
      .get<Category[] | Paginated<Category>>(this.base, { params: toHttpParams({ size: 500 }) })
      .pipe(map(res => (Array.isArray(res) ? res : res.content)));
  }
}
