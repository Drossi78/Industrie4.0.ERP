import { Routes } from '@angular/router';
import { SuppliersList } from './pages/suppliers-list/suppliers-list';
import { SupplierDetail } from './pages/supplier-detail/supplier-detail';

export const SUPPLIERS_ROUTES: Routes = [
  { path: '', component: SuppliersList },
  { path: ':id', component: SupplierDetail },
];
