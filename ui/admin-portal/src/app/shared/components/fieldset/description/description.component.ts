import { Component } from '@angular/core';

/**
 * @component DescriptionComponent
 * @selector tc-description
 * @description
 * A lightweight wrapper component used to display supporting descriptive text
 * for a form control.
 * Typically placed under an input or label to provide context, instructions,
 * or guidance to the user.
 *
 * **Features**
 * - Projects any text or markup using `<ng-content>`
 * - Provides a consistent `.form-description` class for styling
 * - Complements `tc-label` and `tc-input` to enhance accessibility
 *
 * @example
 * ```html
 * <tc-field>
 *   <tc-label for="username">Username</tc-label>
 *   <tc-input id="username" name="username" [(ngModel)]="user.username"></tc-input>
 *   <tc-description>
 *     Must be 6–20 characters and can include letters and numbers.
 *   </tc-description>
 * </tc-field>
 *
 * <tc-field>
 *   <tc-label for="password">Password</tc-label>
 *     <tc-description>
 *     Use at least 8 characters, including one uppercase letter and one number.
 *   </tc-description>
 *   <tc-input id="password" type="password" [(ngModel)]="user.password"></tc-input>
 * </tc-field>
 * ```
 */

@Component({
  selector: 'tc-description',
  templateUrl: './description.component.html',
  styleUrls: ['./description.component.scss']
})
export class DescriptionComponent {

}
