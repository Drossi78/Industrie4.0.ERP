import { Routes } from '@angular/router';
import { OrdersList } from './pages/orders-list/orders-list';
import { OrderDetail } from './pages/order-detail/order-detail';

export const ORDERS_ROUTES: Routes = [
  { path: '', component: OrdersList },
  { path: ':id', component: OrderDetail },
];
