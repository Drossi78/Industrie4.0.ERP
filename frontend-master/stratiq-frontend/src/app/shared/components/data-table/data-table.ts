import {
  Component,
  DestroyRef,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, debounceTime } from 'rxjs';
import { TranslatePipe } from '../../../core/i18n/translate.pipe';
import { Paginated, TableQuery } from '../../models/common.model';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type AnyRow = any;

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export interface ColumnDef<T = any> {
  key: string;
  labelKey: string;
  sortField?: string;
  value: (row: T) => string;
  subValue?: (row: T) => string | null;
  badgeClass?: (row: T) => string | null;
}

export interface FilterOption {
  value: string;
  labelKey: string;
}

export interface FilterDef {
  key: string; // query param name, e.g. 'status'
  labelKey: string; // i18n key for the label
  type: 'text' | 'select';
  options?: FilterOption[]; // only for type: 'select'
}

@Component({
  selector: 'app-data-table',
  imports: [TranslatePipe],
  templateUrl: './data-table.html',
})
export class DataTable implements OnChanges {
  @Input({ required: true }) columns: ColumnDef<AnyRow>[] = [];
  @Input() page: Paginated<AnyRow> | null = null;
  @Input() loading = false;
  @Input() defaultSize = 20;
  @Input() selectable = false;

  @Output() queryChange = new EventEmitter<TableQuery>();
  @Output() selectionChange = new EventEmitter<string[]>();
  @Output() rowClick = new EventEmitter<AnyRow>();
  @Input() filters: FilterDef[] = [];

  readonly currentPage = signal(0);
  readonly pageSize = signal(20);
  readonly sortField = signal<string | null>(null);
  readonly sortDir = signal<'asc' | 'desc'>('asc');
  readonly selected = signal(new Set<string>());
  readonly filterValues = signal<Record<string, string>>({});

  readonly pageSizeOptions = [10, 20, 50];

  private readonly textInput$ = new Subject<void>();

  constructor() {
    // Debounce text filters; emit once typing settles (300 ms).
    this.textInput$
      .pipe(debounceTime(300), takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(() => {
        this.currentPage.set(0);
        this.emit();
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['defaultSize']?.firstChange) {
      this.pageSize.set(this.defaultSize);
    }
    if (changes['filters']) {
      // Seed a value for every filter key so the controls stay defined strings
      // (preserving any value already entered).
      const current = this.filterValues();
      const seeded: Record<string, string> = {};
      for (const f of this.filters) {
        seeded[f.key] = current[f.key] ?? '';
      }
      this.filterValues.set(seeded);
    }
     if (changes['page'] && !changes['page'].firstChange) {
      this.selected.set(new Set());
      this.selectionChange.emit([]);
    }
  }

  onTextFilter(key: string, value: string): void {
    this.filterValues.update(v => ({ ...v, [key]: value }));
    this.textInput$.next();
  }

  onSelectFilter(key: string, value: string): void {
    this.filterValues.update(v => ({ ...v, [key]: value }));
    this.currentPage.set(0);
    this.emit();
  }

  clearFilters(): void {
    const cleared: Record<string, string> = {};
    for (const f of this.filters) cleared[f.key] = '';
    this.filterValues.set(cleared);
    this.currentPage.set(0);
    this.emit();
  }

  hasActiveFilters(): boolean {
    return Object.values(this.filterValues()).some(v => !!v);
  }
  
    isSelected(id: string): boolean {
    return this.selected().has(id);
  }

  isAllSelected(): boolean {
    const rows = this.page?.content ?? [];
    return rows.length > 0 && rows.every(r => this.selected().has(r['id']));
  }

  toggleRow(id: string): void {
    const next = new Set(this.selected());
    if (next.has(id)) {
      next.delete(id);
    } else {
      next.add(id);
    }
    this.selected.set(next);
    this.selectionChange.emit([...next]);
  }

  toggleAll(checked: boolean): void {
    const rows = this.page?.content ?? [];
    const next = checked ? new Set(rows.map((r: AnyRow) => r['id'] as string)) : new Set<string>();
    this.selected.set(next);
    this.selectionChange.emit([...next]);






  }

  onSort(col: ColumnDef<AnyRow>): void {
    if (!col.sortField) return;
    if (this.sortField() === col.sortField) {
      this.sortDir.set(this.sortDir() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortField.set(col.sortField);
      this.sortDir.set('asc');
    }
    this.currentPage.set(0);
    this.emit();
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
    this.emit();
  }

  onSizeChange(size: number): void {
    this.pageSize.set(size);
    this.currentPage.set(0);
    this.emit();
  }

  sortIndicator(col: ColumnDef<AnyRow>): 'asc' | 'desc' | null {
    if (!col.sortField || this.sortField() !== col.sortField) return null;
    return this.sortDir();
  }

  rangeStart(): number {
    return this.currentPage() * this.pageSize() + 1;
  }

  rangeEnd(): number {
    if (!this.page) return 0;
    return this.currentPage() * this.pageSize() + this.page.numberOfElements;
  }

  private emit(): void {
    const filters: Record<string, string> = {};
    for (const [key, value] of Object.entries(this.filterValues())) {
      if (value) filters[key] = value;
    }
    const query: TableQuery = {
      page: this.currentPage(),
      size: this.pageSize(),
      filters,
    };
    const field = this.sortField();
    if (field) {
      query.sort = `${field},${this.sortDir()}`;
    }
    this.queryChange.emit(query);
  }
}
