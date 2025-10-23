import {Component, Input} from '@angular/core';

/**
 * @component LabelComponent
 * @selector tc-label
 * @description
 * A reusable wrapper around the native `<label>` element.
 * Provides consistent styling for form labels and supports accessible
 * associations with input controls through the `for` attribute.
 *
 * **Features**
 * - Projects any label text/content inside (`<ng-content>`)
 * - Supports the native `for` attribute to link the label with an input by `id`
 * - Ensures consistent styling with the `form-label` class
 *
 * @example
 * ```html
 * <!-- Basic usage -->
 * <tc-label for="username">Username</tc-label>
 * <tc-input id="username" name="username" [(ngModel)]="user.username"></tc-input>
 * ```
 */

@Component({
  selector: 'tc-label',
  templateUrl: './label.component.html',
  styleUrls: ['./label.component.scss']
})
export class LabelComponent {
  @Input() for?: string; // to associate with input by id
  @Input() size: 'sm' | 'md' = 'md';
}
