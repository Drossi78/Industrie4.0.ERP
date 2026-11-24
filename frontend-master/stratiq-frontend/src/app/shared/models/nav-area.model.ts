import { PermissionCode } from '../enums/permission-code.enum';

export type AreaIconName = 'items' | 'orders' | 'invoices' | 'production' | 'stock' | 'management';

export interface NavArea {
  labelKey: string;
  descKey: string;
  route: string;
  icon: AreaIconName;
  /** Area is visible if the user has at least one of these permissions. */
  permissions: PermissionCode[];
}

export const NAV_AREAS: readonly NavArea[] = [
  {
    labelKey: 'dashboard.area.items',
    descKey: 'dashboard.area.items.description',
    route: '/items',
    icon: 'items',
    permissions: [PermissionCode.InventoryRead],
  },
  {
    labelKey: 'dashboard.area.orders',
    descKey: 'dashboard.area.orders.description',
    route: '/orders',
    icon: 'orders',
    permissions: [PermissionCode.OrderRead],
  },
  {
    labelKey: 'dashboard.area.invoices',
    descKey: 'dashboard.area.invoices.description',
    route: '/invoices',
    icon: 'invoices',
    permissions: [PermissionCode.OrderRead],
  },
  {
    labelKey: 'dashboard.area.production',
    descKey: 'dashboard.area.production.description',
    route: '/production',
    icon: 'production',
    permissions: [PermissionCode.ProductionRead],
  },
  {
    labelKey: 'dashboard.area.stock',
    descKey: 'dashboard.area.stock.description',
    route: '/stock',
    icon: 'stock',
    permissions: [PermissionCode.WarehouseRead],
  },
  {
    labelKey: 'dashboard.area.management',
    descKey: 'dashboard.area.management.description',
    route: '/management',
    icon: 'management',
    permissions: [
      PermissionCode.UserRead,
      PermissionCode.CustomerRead,
      PermissionCode.WarehouseRead,
      PermissionCode.InventoryRead,
    ],
  },
];
