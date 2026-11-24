import { CanDeactivateFn } from '@angular/router';
import { ItemDetail } from './item-detail';

export const itemDetailGuard: CanDeactivateFn<ItemDetail> = (component) => {
  if (component.hasUnsavedChanges()) {
    return window.confirm('Ungespeicherte Änderungen verwerfen?');
  }
  return true;
};
