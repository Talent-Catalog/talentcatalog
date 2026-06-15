// auth-error.component.ts
import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgIf} from "@angular/common";
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'app-auth-error',
  standalone: true,
  imports: [
    NgIf, TranslateModule,
  ],
  templateUrl: './auth-error.component.html',
  styleUrl: './auth-error.component.scss'
})
export class AuthErrorComponent {
  @Input() error: string | null = null;
  @Output() dismissed = new EventEmitter<void>();

  clear(): void {
    this.dismissed.emit();
  }
}
