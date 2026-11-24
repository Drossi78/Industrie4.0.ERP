import { Routes } from '@angular/router';
import { CustomersList } from './pages/customers-list/customers-list';
import { CustomerDetail } from './pages/customer-detail/customer-detail';

export const CUSTOMERS_ROUTES: Routes = [
  { path: '', component: CustomersList },
  { path: ':id', component: CustomerDetail },
];
