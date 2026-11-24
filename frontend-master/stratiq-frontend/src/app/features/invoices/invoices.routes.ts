import { Routes } from '@angular/router';
import { InvoicesList } from './pages/invoices-list/invoices-list';
import { InvoiceDetail } from './pages/invoice-detail/invoice-detail';

export const INVOICES_ROUTES: Routes = [
  { path: '', component: InvoicesList },
  { path: ':id', component: InvoiceDetail },
];
