import { Routes } from '@angular/router';
import { WarehouseDetail } from './pages/warehouse-detail/warehouse-detail';

export const WAREHOUSES_ROUTES: Routes = [
  { path: ':id', component: WarehouseDetail },
];
