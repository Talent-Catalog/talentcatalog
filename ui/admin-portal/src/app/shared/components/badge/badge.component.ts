import {Component, Input} from '@angular/core';

export type BadgeColor = 'red' | 'orange' | 'yellow' | 'green' | 'blue' | 'purple' | 'pink' | 'gray';

@Component({
  selector: 'tc-badge',
  templateUrl: './badge.component.html',
  styleUrls: ['./badge.component.scss']
})
export class BadgeComponent {
  @Input() color: BadgeColor = 'gray';
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
