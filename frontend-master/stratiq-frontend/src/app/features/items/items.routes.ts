import { Routes } from '@angular/router';
import { ItemsList } from './pages/items-list/items-list';
import { ItemDetail } from './pages/item-detail/item-detail';
import { itemDetailGuard } from './pages/item-detail/item-detail.guard';

export const ITEMS_ROUTES: Routes = [
  { path: '', component: ItemsList },
  { path: ':id', component: ItemDetail, canDeactivate: [itemDetailGuard] },
];
