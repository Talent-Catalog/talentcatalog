import {Component, HostBinding, Input} from '@angular/core';

/**
 * @component FieldComponent
 * @selector tc-field
 * @description
 * A structural wrapper around form controls and labels, providing a consistent layout container.
 * Typically used to group a single label–input pair (or related elements) and apply shared styling.
 *
 * **Features**
 * - Provides a `.form-field` wrapper for consistent styling and spacing
 * - Supports content projection (`<ng-content>`) so you can include any combination of label, input, error message, and description.
 * - Useful for composing complex form UIs with consistent alignment
 * - Pass [checkbox]="true" from the parent to adapt alignment for a checkbox and label
 *
 * @inputs
 *   - `checkbox: boolean = false` — adjusts layout for checkbox-style fields (label aligns beside control)
 *
 * @example
 * ```html
 * <tc-field>
 *   <tc-label for="username">Username</tc-label>
 *   <tc-input id="username" name="username" [(ngModel)]="user.username"></tc-input>
 * </tc-field>
 *
 * <tc-field>
 *   <tc-label for="password">Password</tc-label>
 *   <tc-input id="password" type="password" [(ngModel)]="user.password"></tc-input>
 * </tc-field>
 * ```
 */
@Component({
  selector: 'tc-field',
  templateUrl: './field.component.html',
  styleUrls: ['./field.component.scss']
})
export class FieldComponent {
  @Input() checkbox = false;

  @HostBinding('class.checkbox-style') get isCheckbox() {
    return this.checkbox;
  }
}
