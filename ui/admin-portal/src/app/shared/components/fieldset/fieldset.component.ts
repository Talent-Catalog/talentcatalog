import {Component, Input} from '@angular/core';

/**
 * @component FieldsetComponent
 * @selector tc-fieldset
 * @description
 * A reusable wrapper around the native `<fieldset>` element that allows you to group related
 * form controls together and optionally disable them as a group.
 *
 * **Features**
 * - Provides consistent styling for grouped form controls
 * - Uses Angular’s `[disabled]` binding to toggle the entire group
 * - Allows content projection (`<ng-content>`) so you can pass any inputs, labels, or controls inside
 *
 * @inputs
 *  - `disabled: boolean = false` — disables the entire group of controls within the fieldset
 *
 * @example
 * ```html
 * <!-- Basic usage -->
 * <tc-fieldset>
 *   <label for="username">Username</label>
 *   <tc-input id="username" name="username" [(ngModel)]="user.username"></tc-input>
 * </tc-fieldset>
 * ```
 */

@Component({
  selector: 'tc-fieldset',
  templateUrl: './fieldset.component.html',
  styleUrls: ['./fieldset.component.scss']
})
export class FieldsetComponent {
  @Input() disabled = false;
}
