import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-button',
  templateUrl: './button.component.html',
  styleUrls: ['./button.component.scss'],
})
export class ButtonComponent {
  @Input() size: 'xs' | 'sm' | 'default' | 'lg' | 'xl'  = 'default';
  @Input() btnType: 'primary' | 'secondary' | 'outline' | 'disabled' | 'plain' = 'primary';

  get sizeClass(): string {
    return `btn-${this.size}`;
  }

  get typeClass(): string {
    return `btn-${this.btnType}`;
  }

  get classList(): string[] {
    return [
      this.sizeClass,
      this.typeClass,
    ];
  }
}
