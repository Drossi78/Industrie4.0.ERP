import { Component, Input } from '@angular/core';
import { AreaIconName } from '../../models/nav-area.model';

@Component({
  selector: 'app-area-icon',
  templateUrl: './area-icon.html',
})
export class AreaIcon {
  @Input({ required: true }) icon!: AreaIconName;
  @Input() sizeClass = 'size-5';
}
