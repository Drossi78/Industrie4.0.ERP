import { Routes } from '@angular/router';
import { InventoryList } from './pages/inventory-list/inventory-list';
import { InventoryDetail } from './pages/inventory-detail/inventory-detail';

export const INVENTORY_ROUTES: Routes = [
    { path: '', component: InventoryList},
    { path: ':id', component: InventoryDetail},
];