import { HttpParams } from '@angular/common/http';
import { ListQuery, TableQuery } from '../models/common.model';

export function toHttpParams(query?: ListQuery | TableQuery): HttpParams {
  let params = new HttpParams();
  if (!query) return params;

  for (const [key, value] of Object.entries(query)) {
    if (value === undefined || value === null) continue;

    // Flatten the nested `filters` object into top-level query params,
    // dropping empty values (e.g. the blank "All" select option).
    if (key === 'filters' && typeof value === 'object') {
      for (const [filterKey, filterValue] of Object.entries(value as Record<string, unknown>)) {
        if (filterValue !== undefined && filterValue !== null && filterValue !== '') {
          params = params.set(filterKey, String(filterValue));
        }
      }
      continue;
    }

    params = params.set(key, String(value));
  }

  return params;
}
