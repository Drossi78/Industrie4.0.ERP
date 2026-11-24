import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { Dashboard } from './features/dashboard/dashboard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () =>
      import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },
  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [authGuard],
  },
  {
    path: 'inventory',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/inventory/inventory.routes').then(
        (m) => m.INVENTORY_ROUTES,
      ),
  },
  {
    path: 'items',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/items/items.routes').then((m) => m.ITEMS_ROUTES),
  },
  {
    path: 'orders',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/orders/orders.routes').then((m) => m.ORDERS_ROUTES),
  },
  {
    path: 'invoices',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/invoices/invoices.routes').then((m) => m.INVOICES_ROUTES),
  },
  {
    path: 'customers',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/customers/customers.routes').then((m) => m.CUSTOMERS_ROUTES),
  },
  {
    path: 'suppliers',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/suppliers/suppliers.routes').then((m) => m.SUPPLIERS_ROUTES),
  },
  {
    path: 'management',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/management/management.routes').then((m) => m.MANAGEMENT_ROUTES),
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/pages/change-password/change-password').then((m) => m.ChangePassword),
  },
  {
    path: 'users/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/management/pages/user-detail/user-detail').then((m) => m.UserDetail),
  },
  {
    path: 'warehouses',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/warehouses/warehouses.routes').then((m) => m.WAREHOUSES_ROUTES),
  },
  {
    path: 'stock',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/stock/stock.routes').then((m) => m.STOCK_ROUTES),
  },
  {
    path: 'production',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/production/production.routes').then(
        (m) => m.PRODUCTION_ROUTES,
      ),
  },
];
