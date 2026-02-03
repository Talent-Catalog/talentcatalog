import {Component} from '@angular/core';

/**
 * @component ErrorMessageComponent
 * @selector tc-error-message
 * @description
 * A simple, reusable component for displaying form validation error messages.
 * Provides a consistent `.form-error` class for styling and uses content projection
 * to display any error text or markup passed into it.
 *
 * Intended to be used inside a form field context (e.g., within `<tc-field>` and below an input).
 *
 * **Features**
 * - Projects arbitrary error text or elements using `<ng-content>`
 * - Provides a consistent container for styling and layout of validation errors
 * - Intended to be used below inputs or fields in forms
 *
 * @example
 * ```html
 * <!-- Example: reactive form validation -->
 * <tc-field>
 *   <tc-label for="email">Email</tc-label>
 *   <tc-input id="email" formControlName="email"></tc-input>
 *   <tc-error-message *ngIf="form.get('email')?.hasError('required') && form.get('email')?.touched">
 *     Email is required.
 *   </tc-error-message>
 * </tc-field>
 *
 * <!-- Example: template-driven form -->
 * <tc-field>
 *   <tc-label for="password">Password</tc-label>
 *   <tc-input id="password" name="password" [(ngModel)]="user.password" required></tc-input>
 *   <tc-error-message *ngIf="password.invalid && password.touched">
 *     Password is required.
 *   </tc-error-message>
 * </tc-field>
 * ```
 */

@Component({
  selector: 'tc-error-message',
  templateUrl: './error-message.component.html',
  styleUrls: ['./error-message.component.scss']
})
export class ErrorMessageComponent {
}
