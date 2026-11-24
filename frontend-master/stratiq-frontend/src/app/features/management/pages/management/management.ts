import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '../../../../core/i18n/translate.pipe';
import { DataTable, ColumnDef } from '../../../../shared/components/data-table/data-table';
import { UsersService } from '../../services/users.service';
import { CustomersService } from '../../../customers/services/customers.service';
import { SuppliersService } from '../../../suppliers/services/suppliers.services';
import { WarehousesService } from '../../../warehouses/services/warehouses.service';
import { AppUser, AppRole } from '../../models/app-user.model';
import { Customer } from '../../../customers/models/customer.model';
import { Supplier } from '../../../suppliers/models/supplier.model';
import { Warehouse } from '../../../warehouses/models/warehouse.model';
import { ListQuery, Paginated, TableQuery } from '../../../../shared/models/common.model';
import { Navbar } from "../../../../shared/components/navbar/navbar";
import { AuthSessionService } from '../../../auth/services/auth-session.service';
import { PermissionCode } from '../../../../shared/enums/permission-code.enum';

type ActiveTab = 'users' | 'customers' | 'suppliers' | 'warehouses';

@Component({
  selector: 'app-management',
  imports: [TranslatePipe, DataTable, ReactiveFormsModule, Navbar],
  templateUrl: './management.html',
  styleUrl: './management.css',
})
export class Management implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly usersService = inject(UsersService);
  private readonly customersService = inject(CustomersService);
  private readonly suppliersService = inject(SuppliersService);
  private readonly warehousesService = inject(WarehousesService);
  private readonly session = inject(AuthSessionService);

  readonly canViewUsers = computed(() => this.session.hasPermission(PermissionCode.UserRead));
  readonly canViewCustomers = computed(() => this.session.hasPermission(PermissionCode.CustomerRead));
  readonly canViewSuppliers = computed(() => this.session.hasPermission(PermissionCode.InventoryRead));
  readonly canViewWarehouses = computed(() => this.session.hasPermission(PermissionCode.WarehouseRead));

  readonly canCreateUsers = computed(() => this.session.hasPermission(PermissionCode.UserWrite));
  readonly canCreateCustomers = computed(() => this.session.hasPermission(PermissionCode.CustomerWrite));
  readonly canCreateSuppliers = computed(() => this.session.hasPermission(PermissionCode.InventoryWrite));
  readonly canCreateWarehouses = computed(() => this.session.hasPermission(PermissionCode.WarehouseWrite));

  readonly canCreateActiveTab = computed(() => {
    switch (this.activeTab()) {
      case 'users':       return this.canCreateUsers();
      case 'customers':   return this.canCreateCustomers();
      case 'suppliers':   return this.canCreateSuppliers();
      case 'warehouses':  return this.canCreateWarehouses();
    }
  });

  readonly activeTab = signal<ActiveTab>(this.firstVisibleTab());
  readonly loading = signal(true);
  readonly showModal = signal(false);
  readonly submitting = signal(false);

  readonly usersPage = signal<Paginated<AppUser> | null>(null);
  readonly customersPage = signal<Paginated<Customer> | null>(null);
  readonly suppliersPage = signal<Paginated<Supplier> | null>(null);
  readonly warehousesPage = signal<Paginated<Warehouse> | null>(null);
  readonly roles = signal<AppRole[]>([]);

  // Forms — one per entity
  readonly userForm = this.fb.nonNullable.group({
    email:         ['', [Validators.required, Validators.email]],
    password:      ['', [Validators.required, Validators.minLength(6)]],
    roleId:        ['', Validators.required],
    preferredLang: ['de', Validators.required],
  });

  readonly customerForm = this.fb.nonNullable.group({
    name:    ['', Validators.required],
    email:   [''],
    phone:   [''],
    taxId:   [''],
    city:    [''],
    country: [''],
    notes:   [''],
  });

  readonly supplierForm = this.fb.nonNullable.group({
    name:    ['', Validators.required],
    email:   [''],
    phone:   [''],
    taxId:   [''],
    city:    [''],
    country: [''],
    notes:   [''],
  });

  readonly warehouseForm = this.fb.nonNullable.group({
    name:        ['', Validators.required],
    description: [''],
    street:      [''],
    houseNumber: [''],
    zipCode:     [''],
    city:        [''],
    state:       [''],
    country:     [''],
    isActive:    [true],
  });

  readonly userColumns: ColumnDef<AppUser>[] = [
    {
      key: 'email',
      labelKey: 'management.col.email',
      sortField: 'email',
      value: row => row.email,
    },
    {
      key: 'role',
      labelKey: 'management.col.role',
      sortField: 'roleName',
      value: row => row.roleName,
      badgeClass: () => 'fluent-badge',
    },
    {
      key: 'lang',
      labelKey: 'management.col.lang',
      value: row => row.preferredLang.toUpperCase(),
    },
    {
      key: 'active',
      labelKey: 'management.col.active',
      value: row => row.isActive ? '✓' : '—',
    },
    {
      key: 'createdAt',
      labelKey: 'management.col.createdAt',
      sortField: 'createdAt',
      value: row => new Date(row.createdAt).toLocaleDateString('de-DE'),
    },
  ];

  readonly customerColumns: ColumnDef<Customer>[] = [
    {
      key: 'name',
      labelKey: 'customers.col.name',
      sortField: 'name',
      value: row => row.name,
      subValue: row => row.email ?? null,
    },
    {
      key: 'phone',
      labelKey: 'customers.col.phone',
      value: row => row.phone ?? '—',
    },
    {
      key: 'city',
      labelKey: 'customers.col.city',
      sortField: 'city',
      value: row => [row.city, row.country].filter(Boolean).join(', ') || '—',
    },
    {
      key: 'taxId',
      labelKey: 'customers.col.taxId',
      value: row => row.taxId ?? '—',
    },
    {
      key: 'createdAt',
      labelKey: 'customers.col.createdAt',
      sortField: 'createdAt',
      value: row => new Date(row.createdAt).toLocaleDateString('de-DE'),
    },
  ];

  readonly supplierColumns: ColumnDef<Supplier>[] = [
    {
      key: 'name',
      labelKey: 'suppliers.col.name',
      sortField: 'name',
      value: row => row.name,
      subValue: row => row.email ?? null,
    },
    {
      key: 'phone',
      labelKey: 'suppliers.col.phone',
      value: row => row.phone ?? '—',
    },
    {
      key: 'city',
      labelKey: 'suppliers.col.city',
      sortField: 'city',
      value: row => [row.city, row.country].filter(Boolean).join(', ') || '—',
    },
    {
      key: 'taxId',
      labelKey: 'suppliers.col.taxId',
      value: row => row.taxId ?? '—',
    },
    {
      key: 'createdAt',
      labelKey: 'suppliers.col.createdAt',
      sortField: 'createdAt',
      value: row => new Date(row.createdAt).toLocaleDateString('de-DE'),
    },
  ];

  readonly warehouseColumns: ColumnDef<Warehouse>[] = [
    {
      key: 'name',
      labelKey: 'warehouses.col.name',
      sortField: 'name',
      value: row => row.name,
      subValue: row => row.description ?? null,
    },
    {
      key: 'city',
      labelKey: 'warehouses.col.city',
      sortField: 'city',
      value: row => [row.city, row.country].filter(Boolean).join(', ') || '—',
    },
    {
      key: 'active',
      labelKey: 'warehouses.col.active',
      value: row => row.isActive ? '✓' : '—',
    },
    {
      key: 'createdAt',
      labelKey: 'warehouses.col.createdAt',
      sortField: 'createdAt',
      value: row => new Date(row.createdAt).toLocaleDateString('de-DE'),
    },
  ];

  ngOnInit(): void {
    this.usersService.getRoles().subscribe({
      next: roles => this.roles.set(roles),
    });
    switch (this.activeTab()) {
      case 'users':      this.loadUsers({ size: 20 }); break;
      case 'customers':  this.loadCustomers({ size: 20 }); break;
      case 'suppliers':  this.loadSuppliers({ size: 20 }); break;
      case 'warehouses': this.loadWarehouses({ size: 20 }); break;
    }
  }

  private firstVisibleTab(): ActiveTab {
    if (this.canViewUsers()) return 'users';
    if (this.canViewCustomers()) return 'customers';
    if (this.canViewSuppliers()) return 'suppliers';
    if (this.canViewWarehouses()) return 'warehouses';
    return 'users';
  }

  switchTab(tab: ActiveTab): void {
    this.activeTab.set(tab);
    if (tab === 'users' && this.usersPage() === null) {
      this.loadUsers({ size: 20 });
    } else if (tab === 'customers' && this.customersPage() === null) {
      this.loadCustomers({ size: 20 });
    } else if (tab === 'suppliers' && this.suppliersPage() === null) {
      this.loadSuppliers({ size: 20 });
    } else if (tab === 'warehouses' && this.warehousesPage() === null) {
      this.loadWarehouses({ size: 20 });
    } else {
      this.loading.set(false);
    }
  }

  onQuery(query: TableQuery): void {
    const tab = this.activeTab();
    if (tab === 'users')       this.loadUsers(query);
    if (tab === 'customers')   this.loadCustomers(query);
    if (tab === 'suppliers')   this.loadSuppliers(query);
    if (tab === 'warehouses')  this.loadWarehouses(query);
  }

  openWarehouse(warehouse: Warehouse): void {
    void this.router.navigate(['/warehouses', warehouse.id]);
  }

  openSupplier(supplier: Supplier): void {
    void this.router.navigate(['/suppliers', supplier.id]);
  }

  openCustomer(customer: Customer): void {
    void this.router.navigate(['/customers', customer.id]);
  }

  openUser(user: AppUser): void {
    void this.router.navigate(['/users', user.id]);
  }

  openModal(): void {
    this.userForm.reset({ preferredLang: 'de' });
    this.customerForm.reset();
    this.supplierForm.reset();
    this.warehouseForm.reset({ isActive: true });
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  submit(): void {
    const tab = this.activeTab();
    if (tab === 'users')       this.submitUser();
    if (tab === 'customers')   this.submitCustomer();
    if (tab === 'suppliers')   this.submitSupplier();
    if (tab === 'warehouses')  this.submitWarehouse();
  }

  private submitUser(): void {
    if (this.userForm.invalid) { this.userForm.markAllAsTouched(); return; }
    this.submitting.set(true);
    const { email, password, roleId, preferredLang } = this.userForm.getRawValue();
    this.usersService.create({ email, password, roleId, preferredLang }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.closeModal();
        this.usersPage.set(null);
        this.loadUsers({ size: 20 });
      },
      error: () => this.submitting.set(false),
    });
  }

  private submitCustomer(): void {
    if (this.customerForm.invalid) { this.customerForm.markAllAsTouched(); return; }
    this.submitting.set(true);
    const v = this.customerForm.getRawValue();
    this.customersService.create({
      name:    v.name,
      email:   v.email   || null,
      phone:   v.phone   || null,
      taxId:   v.taxId   || null,
      city:    v.city    || null,
      country: v.country || null,
      notes:   v.notes   || null,
    }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.closeModal();
        this.customersPage.set(null);
        this.loadCustomers({ size: 20 });
      },
      error: () => this.submitting.set(false),
    });
  }

  private submitSupplier(): void {
    if (this.supplierForm.invalid) { this.supplierForm.markAllAsTouched(); return; }
    this.submitting.set(true);
    const v = this.supplierForm.getRawValue();
    this.suppliersService.create({
      name:    v.name,
      email:   v.email   || null,
      phone:   v.phone   || null,
      taxId:   v.taxId   || null,
      city:    v.city    || null,
      country: v.country || null,
      notes:   v.notes   || null,
    }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.closeModal();
        this.suppliersPage.set(null);
        this.loadSuppliers({ size: 20 });
      },
      error: () => this.submitting.set(false),
    });
  }

  private submitWarehouse(): void {
    if (this.warehouseForm.invalid) { this.warehouseForm.markAllAsTouched(); return; }
    this.submitting.set(true);
    const v = this.warehouseForm.getRawValue();
    this.warehousesService.create({
      name:        v.name,
      description: v.description || null,
      street:      v.street      || null,
      houseNumber: v.houseNumber || null,
      zipCode:     v.zipCode     || null,
      city:        v.city        || null,
      state:       v.state       || null,
      country:     v.country     || null,
      isActive:    v.isActive,
    }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.closeModal();
        this.warehousesPage.set(null);
        this.loadWarehouses({ size: 20 });
      },
      error: () => this.submitting.set(false),
    });
  }

  private loadUsers(query: ListQuery): void {
    this.loading.set(true);
    this.usersService.getAll(query).subscribe({
      next: page => { this.usersPage.set(page); this.loading.set(false); },
      error: ()   => this.loading.set(false),
    });
  }

  private loadCustomers(query: ListQuery): void {
    this.loading.set(true);
    this.customersService.getAll(query).subscribe({
      next: page => { this.customersPage.set(page); this.loading.set(false); },
      error: ()   => this.loading.set(false),
    });
  }

  private loadSuppliers(query: ListQuery): void {
    this.loading.set(true);
    this.suppliersService.getAll(query).subscribe({
      next: page => { this.suppliersPage.set(page); this.loading.set(false); },
      error: ()   => this.loading.set(false),
    });
  }

  private loadWarehouses(query: ListQuery): void {
    this.loading.set(true);
    this.warehousesService.getAll(query).subscribe({
      next: page => { this.warehousesPage.set(page); this.loading.set(false); },
      error: ()   => this.loading.set(false),
    });
  }
}
