import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-progress-ring',
  template: `
    <svg [attr.width]="size()" [attr.height]="size()" [attr.viewBox]="viewBox()">
      <circle
        [attr.cx]="center()" [attr.cy]="center()" [attr.r]="radius()"
        fill="none" stroke="var(--color-border-soft, #e5e7eb)" [attr.stroke-width]="strokeWidth"
      />
      <circle
        [attr.cx]="center()" [attr.cy]="center()" [attr.r]="radius()"
        fill="none"
        [attr.stroke]="done() ? 'var(--color-success, #16a34a)' : 'var(--color-primary, #2563eb)'"
        [attr.stroke-width]="strokeWidth"
        stroke-linecap="round"
        [attr.stroke-dasharray]="circumference()"
        [attr.stroke-dashoffset]="dashOffset()"
        [attr.transform]="rotateTransform()"
      />
    </svg>
  `,
})
export class ProgressRing {
  readonly total     = input(0);
  readonly completed = input(0);
  readonly size      = input(40);

  readonly strokeWidth = 4;

  readonly center      = computed(() => this.size() / 2);
  readonly radius      = computed(() => (this.size() - this.strokeWidth * 2) / 2);
  readonly viewBox     = computed(() => `0 0 ${this.size()} ${this.size()}`);
  readonly circumference  = computed(() => 2 * Math.PI * this.radius());
  readonly dashOffset     = computed(() =>
    this.total() === 0 ? this.circumference() : this.circumference() * (1 - this.completed() / this.total()),
  );
  readonly done           = computed(() => this.total() > 0 && this.completed() >= this.total());
  readonly rotateTransform = computed(() => `rotate(-90, ${this.center()}, ${this.center()})`);
}
