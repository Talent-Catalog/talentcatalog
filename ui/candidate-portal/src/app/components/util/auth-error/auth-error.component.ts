// auth-error.component.ts
import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-auth-error',
  templateUrl: './auth-error.component.html',
  styleUrl: './auth-error.component.scss'
})
export class AuthErrorComponent {
  @Input() error: string | null = null;
  @Output() dismissed = new EventEmitter<void>();

  dismiss(): void {
    this.dismissed.emit();
  }
}
