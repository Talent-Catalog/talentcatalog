import { Component, Input } from '@angular/core';

@Component({
  selector: 'tc-badge',
  templateUrl: './badge.component.html',
  styleUrls: ['./badge.component.scss']
})
export class BadgeComponent {
  @Input() color: 'red' | 'orange' | 'yellow' | 'green' | 'blue' | 'purple' | 'pink' | 'gray'= 'gray';
  @Input() href?: string;
  @Input() type: 'link' | 'button' | 'span' = 'span';
  @Input() onClick?: (e: MouseEvent) => void;

  get colorClass() {
    return ['badge', `badge-${this.color}`];
  }
  handleClick(event: MouseEvent) {
    if (this.onClick) this.onClick(event);
  }
}
