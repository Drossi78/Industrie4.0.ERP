export type IsoDateTime = string;
export type Uuid = string;
export type SortDirection = 'asc' | 'desc';

export interface ListQuery {
  page?: number;
  size?: number;
  sort?: string; // "field,direction" — e.g. "createdAt,desc"
}

/** Emitted by DataTable: pagination + sort + active filter values. */
export interface TableQuery extends ListQuery {
  filters?: Record<string, string>;
}

export interface Paginated<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}
